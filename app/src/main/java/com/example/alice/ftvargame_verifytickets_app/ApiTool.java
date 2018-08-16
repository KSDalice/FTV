package com.example.alice.ftvargame_verifytickets_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by alice on 2018/8/15.
 */

public class ApiTool extends ApiData {
    private static final String ApiURL = "http://104.215.159.93";
    protected static Retrofit retrofit = new Retrofit.Builder().baseUrl(ApiURL).client(setOkHttpClient()).addConverterFactory(GsonConverterFactory.create()).build();

    private static ProgressDialog progressDialog;
    protected static Gson gson;

    protected static OkHttpClient setOkHttpClient(){
        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();
        return okHttpClient;
    }
    protected static void initProgressDialog(Activity activity){
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("資料接收中，請稍候 ...");
        progressDialog.show();
    }

    protected static void dismissProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    protected static void showThrowable(Activity activity, Throwable throwable) {
        NotStackedToast.showToast(activity, "網路錯誤或伺服器無反應，請檢查網路狀態或重試");
        Log.e("showThrowable", throwable.toString());
        dismissProgressDialog();
    }

    public static ApiData getApiData(Activity activity, Response<ResponseBody> response, ApiTool.ApiCallback callback) {
        gson = new Gson();
        ApiData apiData = ApiData.newInstance();
        try {
            if (response == null) {
                apiData.message = "沒有資料";
                if (callback != null) {
                    callback.error(apiData.message);
                }
                return apiData;
            }
            Log.e("Response_code",String.valueOf(response.code()));
            if (200 <= response.code() && response.code() < 300) {
                String body = response.body().string();
                Log.v("body",body);
                JSONObject jsonObject = new JSONObject(body);
                apiData.code = getJsonValue(jsonObject, "code");
                apiData.message = getJsonValue(jsonObject, "message");
                apiData.data = getJsonValue(jsonObject, "data");
                apiData.status_code = getJsonValue(jsonObject, "status_code");
                ApiCode(apiData,callback);
            }else {
                String body = response.errorBody().string();
                Log.e("getApiData@@",body);
                JSONObject jsonObject = new JSONObject(body);
                apiData.code = response.code() + "";
                apiData.message = response.code() + getJsonValue(jsonObject, "message");
                if (callback != null) {
                    callback.error(apiData.message);
                }
            }
        } catch (Exception e) {
            apiData.message = e.toString();
            if (callback != null) {
                callback.error(apiData.message);
            }
        }
        return apiData;
    }
    public static void ApiCode(ApiData apiData, ApiTool.ApiCallback callback){
        if (apiData.code.equals("00")){
            return;
        }
        callback.error(apiData.message);
    }

    protected static String getJsonValue(JSONObject jsonObject, String key) {
        try {
            return jsonObject.optString(key, "");
        } catch (Exception e) {
            return "";
        }
    }
    protected static void unknownParams(Map<String,String> params, String key, String value){
        if (!StringContentJudgment.contentJudgmentToBoolean(value)){
            params.put(key,value);
        }
    }

    public interface ApiCallback {
        void success(ApiData apiData);
        void error(String error);
    }

    // 3.10. 驗證票券
    private interface qrcode {
        @POST("/api/v1/concert/verify")
        @FormUrlEncoded
        Call<ResponseBody> setValue(@FieldMap Map<String,String> params);
    }
    public static void qrcode(final Activity activity, String qrcode, final ApiCallback apiCallback){
        initProgressDialog(activity);
        Map<String,String> params = new HashMap<String, String>();
        params.put("adminPassword","ksdwithftvar");//後台設定密碼
        params.put("qrcode",qrcode);
        retrofit.create(qrcode.class).setValue(params).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ApiData apiData = getApiData(activity,response, apiCallback);
                    Log.e("code",apiData.code);
                    if (apiData.code.equals("00")) {
                            Log.e("onResponse","codeEquals00");
//                        Type type = new TypeToken<ApiData.ObjectData>() {
//                        }.getType();
                        apiData.objectData = gson.fromJson(apiData.data,ObjectData.class);
                        Log.e("isEffective=",String.valueOf(apiData.objectData.isEffective));
                        apiCallback.success(apiData);
                    }else {
                        dismissProgressDialog();
                        return;
                    }
                } catch (Exception e) {
                    Log.e("API_3.10_e",e.toString());
                    apiCallback.error(String.valueOf(e));
                }
                dismissProgressDialog();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                apiCallback.error(String.valueOf(t));
                showThrowable(activity,t);
                Log.e("code","apiOnfail");
            }
        });
    }
}
