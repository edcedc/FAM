package com.spit.fam;

import android.util.Log;

import com.orhanobut.hawk.Hawk;

public class InternalStorage {
    public static String PUSH_TOKEN = "PUSH_TOKEN";
    public static String PIC_SITE = "PIC_SITE";

    public static class Setting {
        public static String LANGUAGE = "LANGUAGE";
        public static String HOST_ADDRESS = "HOST_ADDRESS";
        public static String COMPANY_ID = "COMPANY_ID";
    }

    public static class Login {
        public static String USER_ID = "USER_ID";
        public static String PREVIOUS_ID = "PREVIOUS_ID";

        public static String USER = "USER";
        public static String PASSWORD = "PASSWORD";
        public static String CARD_NUMBER = "CARD_NUMBER";
    }

    public static class LocalStockTake {
        public static String LOCAL_STOCK_TAKE_RECORD = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "LOCAL_STOCK_TAKE_RECORD";
    }

    public static class Search {
        public static String LOCATION = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "LOCATION";
        public static String CATEGORY = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  "CATEGORY";
    }

    public static class Application {
        public static String CATEGORY = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "CATEGORY";
        public static String LOCATION = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "LOCATION";
        public static String ASSET = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  "ASSET";
    }

    public static class OFFLINE_CACHE {
        public static String USER_LIST_LAST_MODIFIED_DATE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  "USER_LIST_LAST_MODIFIED_DATE";

        public static String LOCAL_REGISTRATION = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") +  "LOCAL_REGISTRATION";
        public static String LOCAL_RETURN = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "LOCAL_RETURN";

        public static String SP_USER_CALLED_DATE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") +  "SP_USER_CALLED_DATE";
        public static String SP_ASSET_CALLED_DATE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_ASSET_CALLED_DATE";

        public static String SP_CERT_PATH = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")+  "SP_CERT_PATH";

        public static String SP_USER = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  "SP_USER";
        public static String USER = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "USER";
        public static String USER_ID =  Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "USER_ID";
        public static String USER_LIST =  Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "USER_LIST";

        public static String ASSET = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "ASSET";
        public static String SP_ASSET_ALL = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")+  "SP_ASSET_ALL";
        public static String SP_ASSET_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_ASSET_LIST";
        public static String REGISTRATION = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "REGISTRATION";
        public static String BORROW = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "BORROW";
        public static String RETURN = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "RETURN";

        public static String SP_BORROW_NO_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_BORROW_NO_";
        public static String SP_DISPOSAL_NO_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DISPOSAL_NO_";
        public static String SP_STOCK_TAKE_LIST_NO_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_STOCK_TAKE_LIST_NO_";
        public static String SP_STOCK_TAKE_LIST_NO_SAVED_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_STOCK_TAKE_LIST_NO_SAVED_";

        public static String SP_ASSET = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") +  "SP_ASSET_";

        public static String SP_BORROW_1 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_BORROW_1";
        public static String SP_BORROW_2 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_BORROW_2";
        public static String SP_BORROW_3 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_BORROW_3";

        public static String SP_DISPOSAL_1 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DISPOSAL_1";
        public static String SP_DISPOSAL_2 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DISPOSAL_2";
        public static String SP_DISPOSAL_3 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DISPOSAL_3";
        public static String SP_STOCK_TAKE_LIST_P2 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_STOCK_TAKE_LIST_P2";

        public static String SP_STOCK_TAKE_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_STOCK_TAKE_LIST";

        public static String SP_PENDING_BORROW_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_PENDING_BORROW_REQUEST";
        public static String SP_PENDING_DISPOSAL_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_PENDING_DISPOSAL_REQUEST";

        public static String STOCK_TAKE_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "STOCK_TAKE_LIST";
        public static String DISPOSAL_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "DISPOSAL_LIST";
        public static String STOCK_TAKE_REMARK_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "STOCK_TAKE_REMARK_LIST";

        public static String STOCK_TAKE_STATUS = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "STOCK_TAKE_STATUS";

        public static String PENDING_CHANGE_EPC_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_CHANGE_EPC_REQUEST";
        public static String PENDING_BIND_EPC_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_BIND_EPC_REQUEST";

        public static String PENDING_BORROW_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_BORROW_REQUEST";
        public static String PENDING_RETURN_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_RETURN_REQUEST";
        public static String PENDING_REMARK_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_REMARK_REQUEST";
        public static String PENDING_STOCK_TAKE_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_STOCK_TAKE_REQUEST";
        public static String PENDING_PHOTO_UPLOAD = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_PHOTO_UPLOAD";
        public static String PENDING_REMARK_CREATE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_REMARK_CREATE";
        public static String PENDING_DISPOSAL = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_DISPOSAL";

        public static String PENDING_STOCK_TAKE_REMARK_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") +"PENDING_STOCK_TAKE_REMARK_";
        public static String PENDING_STOCK_TAKE_STATUS_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") +"PENDING_STOCK_TAKE_STATUS_";

        public static String PENDING_PHOTO_UPLOAD_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_PHOTO_UPLOAD_REQUEST";

        public static String SAVED_PHOTO_UPLOAD_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SAVED_PHOTO_UPLOAD_REQUEST_";
        public static String SAVE_STOCK_TAKE_REMARK_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SAVE_STOCK_TAKE_REMARK_";
        public static String SAVE_STOCK_TAKE_STATUS_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SAVE_STOCK_TAKE_STATUS_";

        public static String SP_LOCATION_CACHE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_LOCATION_CACHE";
        public static String SP_CATEGORY_CACHE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_CATEGORY_CACHE";
        public static String SP_LISTING_LEVEL = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_LISTING_LEVEL";
        public static String SP_LISTING_LEVEL_CACHE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_LISTING_LEVEL_CACHE";

        public static String SP_DOWNLOAD_AFTER_LOGIN = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DOWNLOAD_AFTER_LOGIN";
    }

    public static void resetStaticPath() {
        LocalStockTake.LOCAL_STOCK_TAKE_RECORD = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "LOCAL_STOCK_TAKE_RECORD";
        Search.LOCATION = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "LOCATION";
        Search.CATEGORY = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  "CATEGORY";
        Application.CATEGORY = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "CATEGORY";
        Application.LOCATION = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "LOCATION";
        Application.ASSET = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  "ASSET";

        OFFLINE_CACHE.SP_USER_CALLED_DATE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") +  "SP_USER_CALLED_DATE";
        OFFLINE_CACHE.SP_ASSET_CALLED_DATE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_ASSET_CALLED_DATE";

        Log.i("SP_ASSET_CALLED_DATE", "SP_ASSET_CALLED_DATE" + OFFLINE_CACHE.SP_ASSET_CALLED_DATE + " " + Hawk.get(OFFLINE_CACHE.SP_ASSET_CALLED_DATE));

        OFFLINE_CACHE.  SP_CERT_PATH = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")+  "SP_CERT_PATH";

        OFFLINE_CACHE.  SP_USER = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  "SP_USER";
        OFFLINE_CACHE.  USER = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "USER";
        OFFLINE_CACHE.  USER_ID =  Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + "USER_ID";

        OFFLINE_CACHE.  ASSET = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  +  Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "ASSET";
        OFFLINE_CACHE.  SP_ASSET_ALL = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")+  "SP_ASSET_ALL";
        OFFLINE_CACHE.  SP_ASSET_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_ASSET_LIST";
        OFFLINE_CACHE.  REGISTRATION = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "REGISTRATION";
        OFFLINE_CACHE.  BORROW = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "BORROW";
        OFFLINE_CACHE.  RETURN = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "RETURN";

        OFFLINE_CACHE.  SP_BORROW_NO_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_BORROW_NO_";
        OFFLINE_CACHE.  SP_DISPOSAL_NO_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DISPOSAL_NO_";
        OFFLINE_CACHE.  SP_STOCK_TAKE_LIST_NO_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_STOCK_TAKE_LIST_NO_";
        OFFLINE_CACHE.  SP_STOCK_TAKE_LIST_NO_SAVED_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_STOCK_TAKE_LIST_NO_SAVED_";

        OFFLINE_CACHE.  SP_ASSET = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") +  "SP_ASSET_";

        OFFLINE_CACHE.  SP_BORROW_1 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_BORROW_1";
        OFFLINE_CACHE.  SP_BORROW_2 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_BORROW_2";
        OFFLINE_CACHE.  SP_BORROW_3 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_BORROW_3";

        OFFLINE_CACHE.  SP_DISPOSAL_1 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DISPOSAL_1";
        OFFLINE_CACHE.  SP_DISPOSAL_2 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DISPOSAL_2";
        OFFLINE_CACHE.  SP_DISPOSAL_3 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DISPOSAL_3";
        OFFLINE_CACHE.  SP_STOCK_TAKE_LIST_P2 = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_STOCK_TAKE_LIST_P2";

        OFFLINE_CACHE.  SP_STOCK_TAKE_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_STOCK_TAKE_LIST";

        OFFLINE_CACHE.  SP_PENDING_BORROW_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_PENDING_BORROW_REQUEST";
        OFFLINE_CACHE.  SP_PENDING_DISPOSAL_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_PENDING_DISPOSAL_REQUEST";

        OFFLINE_CACHE.  STOCK_TAKE_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "STOCK_TAKE_LIST";
        OFFLINE_CACHE.  DISPOSAL_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "DISPOSAL_LIST";
        OFFLINE_CACHE.  STOCK_TAKE_REMARK_LIST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "STOCK_TAKE_REMARK_LIST";

        OFFLINE_CACHE.  STOCK_TAKE_STATUS = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "STOCK_TAKE_STATUS";

        OFFLINE_CACHE.  PENDING_CHANGE_EPC_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_CHANGE_EPC_REQUEST";
        OFFLINE_CACHE.  PENDING_BIND_EPC_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_BIND_EPC_REQUEST";

        OFFLINE_CACHE.  PENDING_BORROW_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_BORROW_REQUEST";
        OFFLINE_CACHE.  PENDING_RETURN_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_RETURN_REQUEST";
        OFFLINE_CACHE.  PENDING_REMARK_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_REMARK_REQUEST";
        OFFLINE_CACHE.  PENDING_STOCK_TAKE_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_STOCK_TAKE_REQUEST";
        OFFLINE_CACHE.  PENDING_PHOTO_UPLOAD = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_PHOTO_UPLOAD";
        OFFLINE_CACHE.  PENDING_REMARK_CREATE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_REMARK_CREATE";
        OFFLINE_CACHE.  PENDING_DISPOSAL = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_DISPOSAL";

        OFFLINE_CACHE.  PENDING_STOCK_TAKE_REMARK_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") +"PENDING_STOCK_TAKE_REMARK_";
        OFFLINE_CACHE.  PENDING_STOCK_TAKE_STATUS_ = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") +"PENDING_STOCK_TAKE_STATUS_";

        OFFLINE_CACHE.  PENDING_PHOTO_UPLOAD_REQUEST = Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "PENDING_PHOTO_UPLOAD_REQUEST";

        OFFLINE_CACHE.  SP_LOCATION_CACHE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_LOCATION_CACHE";
        OFFLINE_CACHE.  SP_CATEGORY_CACHE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_CATEGORY_CACHE";
        OFFLINE_CACHE.  SP_LISTING_LEVEL = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_LISTING_LEVEL";
        OFFLINE_CACHE.  SP_LISTING_LEVEL_CACHE = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_LISTING_LEVEL_CACHE";

        OFFLINE_CACHE.  SP_DOWNLOAD_AFTER_LOGIN = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "SP_DOWNLOAD_AFTER_LOGIN";
    }
}
