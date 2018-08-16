package com.example.alice.ftvargame_verifytickets_app;

import android.content.DialogInterface;
import android.content.Intent;
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

    Button scan_Button,summitButton;
    EditText input_EditText;
    IntentIntegrator scanIntegrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scan_Button = (Button)findViewById(R.id.scan_Button);
        summitButton = (Button)findViewById(R.id.summit_Button);
        input_EditText = (EditText)findViewById(R.id.input_EditText);
        scan_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanIntegrator = new IntentIntegrator(MainActivity.this);
                scanIntegrator.setPrompt("請掃描");
                scanIntegrator.setTimeout(300000);
                scanIntegrator.setOrientationLocked(false);
                scanIntegrator.initiateScan();
            }
        });
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null)
        {
            if(scanningResult.getContents() != null)
            {
                String scanContent = scanningResult.getContents();
                if (!scanContent.equals(""))
                {
                    verify(scanContent);
                }
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, intent);
            Toast.makeText(MainActivity.this,"掃描失敗，請重新掃描或改手核銷",Toast.LENGTH_LONG).show();
        }

    }

    private void verify(String qrcode){
        ApiTool.qrcode(this, qrcode, new ApiTool.ApiCallback() {
            @Override
            public void success(ApiData apiData) {
                if(apiData.objectData.isEffective){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.verify_title)
                            .setMessage(R.string.verify_content_success)
                            .setPositiveButton(R.string.verify_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.verify_title)
                            .setMessage(R.string.verify_content_fail)
                            .setPositiveButton(R.string.verify_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }
            }

            @Override
            public void error(String error) {
                Toast.makeText(MainActivity.this,"VerifyError，please try again!",Toast.LENGTH_LONG).show();
            }
        });
    }
}
