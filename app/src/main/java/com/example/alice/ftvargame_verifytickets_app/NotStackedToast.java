package com.example.alice.ftvargame_verifytickets_app;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by alice on 2018/8/15.
 */

public class NotStackedToast {
    static Toast toast;

    public static void showToast(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }
}
