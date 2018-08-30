package com.example.alice.ftvargame_verifytickets_app;

import android.app.Activity;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    IntentIntegrator scanIntegrator;
    Vibrator vibrator;
    static MediaPlayer mMediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE); //獲取系統的Vibrator服務
        AudioManager audioManager = (AudioManager)getSystemService(this.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
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
                    verify(scanContent);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
            Toast.makeText(MainActivity.this, "掃描失敗，請重新掃描", Toast.LENGTH_LONG).show();
            startScan();
        }

    }
    private void startScan(){
        scanIntegrator = new IntentIntegrator(MainActivity.this);
        scanIntegrator.setPrompt("請掃描");
        scanIntegrator.setTimeout(300000);
        scanIntegrator.setOrientationLocked(false);
        scanIntegrator.initiateScan();
    }

    private void verify(String qrcode){
        ApiTool.qrcode(this, qrcode, new ApiTool.ApiCallback() {
            @Override
            public void success(ApiData apiData) {
                if(apiData.objectData.isEffective){
                    Toast.makeText(MainActivity.this, "核銷成功!", Toast.LENGTH_LONG).show();
                    if (vibrator.hasVibrator()) {
                        playRing(MainActivity.this);
                        vibrator.vibrate(500);
                        startScan();
                        stopRing();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "核銷結果失敗!!!請用戶重新確認。", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void error(String error) {
                Toast.makeText(MainActivity.this,"APIVerifyError，please try again!",Toast.LENGTH_LONG).show();
            }
        });
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


    //    作者：Ed1SoNJ
//    链接：https://www.jianshu.com/p/7cf9972b4fc6
//    來源：简书
//    简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
}
