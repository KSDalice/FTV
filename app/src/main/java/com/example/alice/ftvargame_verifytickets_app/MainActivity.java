package com.example.alice.ftvargame_verifytickets_app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.text.RandomStringGenerator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.media.AudioManager.STREAM_ALARM;
import static android.media.AudioManager.STREAM_MUSIC;

public class MainActivity extends AppCompatActivity {


    public static boolean offline;
    Button datePick_Button,scan_online_Button,scan_offline_Button;
    String dateString;
    Button download_Button,update_Button;
    //設定離線資料的KEY
    public static final String KEY = "offlineData";
    TextView showDebug_TextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        datePick_Button = (Button)findViewById(R.id.datePick_Button);
        scan_online_Button = (Button)findViewById(R.id.scan_online_Button);
        scan_offline_Button = (Button)findViewById(R.id.scan_offline_Button);
//        download_Button = (Button)findViewById(R.id.download_Button);
        update_Button = (Button)findViewById(R.id.update_Button);
        showDebug_TextView = (TextView)findViewById(R.id.showDebug_TextView);
        scan_online_Button.setOnClickListener(getButtonListner());
        scan_offline_Button.setOnClickListener(getButtonListner());
//        download_Button.setOnClickListener(getButtonListner());
        update_Button.setOnClickListener(getButtonListner());
        //離線模式開關
        offline=false;

    }

    public void datePicker(View view){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                dateString = String.format("%02d%02d", month+1, day);
                Log.e("show Today",dateString);
                datePick_Button.setText((month+1)+"/"+day);
            }
        },year,month,day).show();
    }

        @Override
    protected void onResume() {
        super.onResume();
        if(ApiTool.Debug){
            showDebug_TextView.setVisibility(View.VISIBLE);
        }else {
            showDebug_TextView.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener getButtonListner (){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dateString==null){
                    NotStackedToast.showToast(MainActivity.this, "請先選擇核銷日期");
                    return;
                }
                SharedPreferences sharedPreferences  = getApplication().getSharedPreferences(dateString+KEY,Context.MODE_PRIVATE);
                switch (view.getId()){

                    case R.id.scan_online_Button:
                        offline = false;
                        Intent onlineIntent = new Intent(MainActivity.this,ScanActivity.class);
                        onlineIntent.putExtra("sessionCode",dateString);
                        startActivity(onlineIntent);
                        break;
                    case R.id.scan_offline_Button:
                        if(sharedPreferences.getAll().size()==0){
                            NotStackedToast.showToast(MainActivity.this, "請先更新離線資料");
                            break;
                        }
                        offline = true;
                        Intent offlineIntent = new Intent(MainActivity.this,ScanActivity.class);
                        offlineIntent.putExtra("sessionCode",dateString);
                        startActivity(offlineIntent);
                        break;
//                    case R.id.download_Button:

//                        break;
                    case R.id.update_Button:
                        if(sharedPreferences.getAll().size()==0){//若沒有離線資料 先下載
                            Log.e("download","沒有離線資料,先下載");
                            getAllqrcode(dateString);
                            break;
                        }
                        Map<String, ?> prefsMap = sharedPreferences.getAll();
                        String uploadString = "";
                        for (Map.Entry<String, ?> entry: prefsMap.entrySet()) {
                            Log.v("uploadSharedPreferences", entry.getKey() + ":" + entry.getValue().toString());//查看sharedPreference所有資料
                            if(entry.getValue().toString().equals("1")){
                                uploadString += (entry.getKey()+",");
                            }
                        }
                        if(uploadString.length()>0){//上傳更新後下載
                            Log.e("show uploadString",uploadString.substring(0,uploadString.length()-1));//檢查上傳資料
                            upload(uploadString.substring(0,uploadString.length()-1));//最後一個逗號去掉
                        }else {
                            getAllqrcode(dateString);//沒有資料需上傳，僅下載更新後台資料
                        }
                        break;
                }
            }
        };
    }

    /**下載所有QRCode*/
    private void getAllqrcode(String sessionCode){
        ApiTool.getAllqrcode(this,sessionCode, new ApiTool.ApiCallback() {
            @Override
            public void success(ApiData apiData) {
                if(apiData.data!=null){
                    try {
                        JSONObject dataObject = new JSONObject(apiData.data);
                        JSONArray concertsArray = dataObject.getJSONArray("concerts");
                        ArrayList<String> Concerts = new ArrayList<>();
                        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(dateString+KEY,Context.MODE_PRIVATE);
                        //由 SharedPreferences 中取出 Editor 物件，透過 Editor 物件將資料存入
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if(sharedPreferences !=null)
                        {
                            editor.clear();
                        }
                        for(int i = 0;i<concertsArray.length();i++){
                            JSONObject concertObject = concertsArray.getJSONObject(i);
                            //加入sharedPreferences qrcode為key used為value
                            editor.putInt(concertObject.getString("qrcode"),concertObject.getInt("used"));
                            //印log取資料用
                            Log.e(dateString+"Concerts",concertObject.getString("qrcode")+"  "+concertObject.getInt("used"));
                            Concerts.add(concertObject.getString("qrcode"));
                        }
                        editor.commit();
                        Log.e("sharedPreferenceSize",sharedPreferences.getAll().size()+"");//檢查儲存資料

                        Log.e("getConcertsSize",dateString+":"+Concerts.size());//doubleCheck下載筆數

                        NotStackedToast.showToast(MainActivity.this, "下載成功!共"+sharedPreferences.getAll().size()+"筆");
                    } catch (JSONException e) {
                        NotStackedToast.showToast(MainActivity.this, "JSONException!"+e.toString());
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void error(String error) {
                Log.e("APIerror",error);
                NotStackedToast.showToast(MainActivity.this, "網路不穩，請再重試一次!");
            }
        });
    }

    private void upload(String qrcodes) {
        ApiTool.upload(this, qrcodes, new ApiTool.ApiCallback() {
            @Override
            public void success(ApiData apiData) {
                if (apiData.code.equals("00")) {
                    NotStackedToast.showToast(MainActivity.this, "上傳更新成功，沒有異常紀錄。");
                    getAllqrcode(dateString);
                }
            }

            @Override
            public void error(String error) {
                NotStackedToast.showToast(MainActivity.this, "上傳中發生錯誤，請再重新上傳。 " + error);
            }
        });
    }

    /**
     *     Save and get ArrayList in SharedPreference
     */

    public void saveArrayList(ArrayList<String> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public ArrayList<String> getArrayList(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
