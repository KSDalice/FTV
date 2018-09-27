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

import static android.media.AudioManager.STREAM_ALARM;
import static android.media.AudioManager.STREAM_MUSIC;

public class MainActivity extends AppCompatActivity {


    public static boolean offline;
    Button datePickButton,scan_onlineButton,scan_offlineButton;
    String dateString;
    Button downloadButton,uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        datePickButton = (Button)findViewById(R.id.datePickButton);
        scan_onlineButton = (Button)findViewById(R.id.scan_onlineButton);
        scan_offlineButton = (Button)findViewById(R.id.scan_offlineButton);
        downloadButton = (Button)findViewById(R.id.downloadButton);
        uploadButton = (Button)findViewById(R.id.uploadButton);
        scan_onlineButton.setOnClickListener(getButtonListner());
        scan_offlineButton.setOnClickListener(getButtonListner());
        downloadButton.setOnClickListener(getButtonListner());
        uploadButton.setOnClickListener(getButtonListner());
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
                datePickButton.setText((month+1)+"/"+day);
            }
        },year,month,day).show();
    }

        @Override
    protected void onResume() {
        super.onResume();
    }

    private View.OnClickListener getButtonListner (){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dateString==null){
                    Toast.makeText(MainActivity.this,"請先選擇核銷日期",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(view == scan_onlineButton){
                    offline = false;
                    Intent intent = new Intent(MainActivity.this,ScanActivity.class);
                    intent.putExtra("sessionCode",dateString);
                    startActivity(intent);
                }else if(view == scan_offlineButton){
                    offline = true;
                    Intent intent = new Intent(MainActivity.this,ScanActivity.class);
                    intent.putExtra("sessionCode",dateString);
                    startActivity(intent);
                }else if(view == downloadButton){
                    getAllqrcode(dateString);
                }else if(view == uploadButton){
                    upload("287e36c0-ba2e-11e8-ac85-490b347118cc,295c0200-ba2e-11e8-a56b-933dd5b91cd7");
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
                        for(int i = 0;i<concertsArray.length();i++){
                            JSONObject concertObject = concertsArray.getJSONObject(i);
                            if(concertObject.getInt("used")==0){
                                Concerts.add(concertObject.getString("qrcode"));
                            }
                        }
                        saveArrayList(Concerts,"downloadConcerts");
                        ArrayList<String> getConcerts = getArrayList("downloadConcerts");

                        for (String s:getConcerts) {
                            Log.e("Concerts",s);
                        }
                        Log.e("getConcertsSize",getConcerts.size()+"");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void error(String error) {
                Log.e("APIerror",error);
                Toast.makeText(getApplicationContext(),"網路不穩，請再重試一次!",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void upload(String qrcodes) {
        ApiTool.upload(this, qrcodes, new ApiTool.ApiCallback() {
            @Override
            public void success(ApiData apiData) {
                if (apiData.code.equals("00")) {
                    NotStackedToast.showToast(MainActivity.this, "上傳成功，沒有異常紀錄");
                }
            }

            @Override
            public void error(String error) {
                NotStackedToast.showToast(MainActivity.this, "上傳紀錄錯誤 " + error);
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
