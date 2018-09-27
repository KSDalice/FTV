package com.example.alice.ftvargame_verifytickets_app;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.text.RandomStringGenerator;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.media.AudioManager.STREAM_MUSIC;

public class ScanActivity extends AppCompatActivity {

    IntentIntegrator scanIntegrator;
    Vibrator vibrator;
    static MediaPlayer mMediaPlayer;
    SoundPool soundPool;
    int sound;
    int streamID;
    String sessionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Intent intent  = getIntent();
        sessionCode = intent.getStringExtra("sessionCode");
        soundPool = new SoundPool(5,STREAM_MUSIC,0);
        sound = soundPool.load(this,R.raw.beep2,1);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE); //獲取系統的Vibrator服務


        //控制系統音量
//        AudioManager audioManager = (AudioManager)getSystemService(this.AUDIO_SERVICE);
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
    }

    private void getSessionCode() {
        Calendar mCal = Calendar.getInstance();
        String dateformat = "MMdd";
        SimpleDateFormat df = new SimpleDateFormat(dateformat);
        String today = df.format(mCal.getTime());
        Log.d("getSeesionCode", "today is " + today);
    }

    @Override
    protected void onResume() {
        super.onResume();
            startScan();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            if (scanningResult.getContents() != null) {
                String scanContent = scanningResult.getContents();
                if (!scanContent.equals("")) {
                    if(!MainActivity.offline){
                        verify(sessionCode,scanContent);
                    }else {
                        ReadJason(scanContent);
                    }

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
            Toast.makeText(ScanActivity.this, "掃描失敗，請重新掃描", Toast.LENGTH_LONG).show();
        }

    }


    String scanMode = "";
    private void startScan(){
//        scanIntegrator = new IntentIntegrator(ScanActivity.this);
//        scanIntegrator =IntentIntegrator.forSupportFragment();
        if(!MainActivity.offline){
            scanMode = "網路連線掃描";
        }else {
            scanMode = "離線模式";
        }
        scanIntegrator.setPrompt(scanMode);
        scanIntegrator.setTimeout(300000);
        scanIntegrator.setBeepEnabled(false);
        scanIntegrator.setOrientationLocked(false);
        scanIntegrator.initiateScan();
    }

    private void verify(String sessionCode,String qrcode){
        ApiTool.qrcode(this,sessionCode, qrcode, new ApiTool.ApiCallback() {
            @Override
            public void success(ApiData apiData) {
                if(apiData.objectData.isEffective){
                    SuccessAction();
                }else {
                    FailureAction();

                }
            }

            @Override
            public void error(String error) {
                Log.e("APIerror",error);
                Toast.makeText(getApplicationContext(),"網路不穩，請再重試一次!",Toast.LENGTH_LONG).show();
            }
        });
    }

    //隨機產生字串方法
    private void generaterRandomString() {
        char[][] pairs = {{'a','z'},{'A','Z'},{'0','9'}};
        int length = 36;
        for (int i = 0;i<800;i++){
            RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange(pairs).build();
            String randomString = generator.generate(length);
            Log.e("ramdom",randomString+"\","+"\n");
        }
    }

    //讀取json文件資料比對掃描結果(離線模式)
    private void ReadJason(String scanContent) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(getAssets().open("FTVData_text.json"), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            JSONArray jsonArray = new JSONArray(builder.toString());
            for(int i=0;i<jsonArray.length(); i++){
                if(scanContent.equals(jsonArray.getString(i)))
                {
                    SuccessAction();
                    return;
                }else {
                    FailureAction();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //掃描成功回傳成功後執行
    private void SuccessAction() {
        try {
            if(mtoast!=null){
                mtoast.cancel();
            }
            streamID = soundPool.play(sound,1,1,1,0,1);
            Toast.makeText(getApplicationContext(), "核銷成功!", Toast.LENGTH_SHORT).show();
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(500);
            }
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startScan();
//            }
//        },5000);
        }catch (Exception e){
            Log.e("Exception",e.toString());
        }

    }
    Toast mtoast;
    //掃描成功回傳失敗後執行
    private void FailureAction() {
        if(mtoast==null){
            mtoast = new Toast(getApplicationContext());
        }
        LayoutInflater layoutInflater = getLayoutInflater();
        View custToast = layoutInflater.inflate(R.layout.toast_layout, null);
        mtoast.setGravity(Gravity.CENTER, 0, 0);
        mtoast.setView(custToast);
        TextView toastTextView = (TextView) mtoast.getView().findViewById(R.id.toastTextView);
        toastTextView.setText("核銷結果失敗!!!請用戶重新確認。");
        mtoast.show();


//        Toast.makeText(MainActivity.this, "核銷結果失敗!!!請用戶重新確認。", Toast.LENGTH_LONG).show();
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startScan();
//            }
//        },5000);
    }

    //开始播放
    public static void playRing(final Activity activity){
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);//用于获取手机默认铃声的Uri
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(activity, alert);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);//告诉mediaPlayer播放的是铃声流
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.setVolume(1,1);//控制播放音量
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //停止播放
    public static void stopRing(){
        if (mMediaPlayer!=null){
            if (mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("OnBackPressed","按下返回鍵");
    }
    //    作者：Ed1SoNJ
//    链接：https://www.jianshu.com/p/7cf9972b4fc6
//    來源：简书
//    简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。

    //https://blog.csdn.net/Jason_Fish/article/details/67632727    (SoundPool)
    //https://www.cnblogs.com/rustfisher/p/4600711.html(读取JSON文件数据)
}
