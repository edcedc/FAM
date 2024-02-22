package com.spit.fam.NewHandHeld;

import android.widget.EditText;

import rfid.uhfapi_y2007.core.Util;
import rfid.uhfapi_y2007.entities.InventoryConfig;
import rfid.uhfapi_y2007.entities.TagParameter;
import rfid.uhfapi_y2007.protocol.vrp.Reader;

public class MyUtil {
    public static Reader reader;
    public static TagParameter selectParam;
    public static TagParameter selectEpcParam;
    public static InventoryConfig inventoryConfig;

    public static boolean checkTagPwd(EditText et){
        String pwd = et.getText().toString();
        if(pwd.equals("") || pwd.length() != 8 || !Util.IsHexString(pwd)){
            return false;
        }
        return true;
    }
    public static boolean checkEtInfo(EditText et){
        String pwd = et.getText().toString();
        if(pwd.equals("") || pwd.length()%4 == 1 || !Util.IsHexString(pwd)){
            return false;
        }
        return true;
    }
}
