package com.example.alice.ftvargame_verifytickets_app;

import android.support.annotation.NonNull;

/**
 * Created by alice on 2018/8/15.
 */

public class StringContentJudgment {
    @NonNull
    public static Boolean contentJudgmentToBoolean(String str){
        if (str == null || str.length() == 0 || str.trim().equals("") || str.equals("null")){
            return true;
        }
        return false;
    }

    @NonNull
    public static String contentJudgmentToString(String str){
        if (str == null || str.length() == 0 || str.trim().equals("") || str.equals("null")){
            return "";
        }
        return str;
    }
}
