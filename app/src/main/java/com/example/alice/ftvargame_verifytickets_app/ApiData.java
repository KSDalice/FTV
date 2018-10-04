package com.example.alice.ftvargame_verifytickets_app;

/**
 * Created by alice on 2018/8/14.
 */

public class ApiData {
    private static ApiData apiData;

    public String status_code;
    public String code;
    public String message;
    public String data;

    public ApiData() {
    }
    public static ApiData newInstance() {
        if (apiData == null) {
            apiData = new ApiData();
        }
        return apiData;
    }
    // 3.10 驗證QRcode
    public ObjectData objectData;
    public class ObjectData {
        public boolean isEffective;
        public String message;
    }

    // 3.10 驗證QRcode
    public UploadObjectData uploadObjectData;
    public class UploadObjectData {
        public boolean isSuccess;
    }
}
