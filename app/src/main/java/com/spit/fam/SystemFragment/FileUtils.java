package com.spit.fam.SystemFragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

public class FileUtils {
    public static Object jsonStringToObject(String rawString, Class name){
        Gson gson = new Gson();
        return gson.fromJson(rawString, name);
    }

    public static boolean checkFileExist(String fileName) {
        File file = new File("/sdcard/emsd/Download/" + fileName + ".json");

        Log.i("checkFileExist", "checkFileExist "  +fileName + " " + file.exists());

        if(file.exists())
            return true;

        return false;
    }

    public static boolean isFileExist(String fileName) {

        File file = new File(fileName);

        if (file.exists()) {
            return true;
        }

        return false;
    }

    public static String readFromFile(String fileName) {
        String ret = "";

        try {
            FileInputStream inputStream = new FileInputStream("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" + fileName + ".json");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");

            Log.e("login activity", "File not found: " + e.toString() + " " + serverId);
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        Log.e("raw data", "raw data : " + fileName + " "  +ret);

        return ret;
    }

    public static String getLastModified(String fileName) {
        String ret = "";

        File file = new File(fileName);

        if (file.exists()) {
            Date lastModified = new Date(file.lastModified());
            ret = lastModified.toString();
        }

        return ret;
    }

    public static boolean deleteFile(String fileName) {
        File fdelete = new File("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" + fileName + ".json");
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + fileName);

                return true;
            } else {
                System.out.println("file not Deleted :" + fileName);
            }
        }
        return false;
    }

    public static boolean deleteFileByRawPath(String fileName) {
        File fdelete = new File(fileName);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + fileName);
                return true;
            } else {
                System.out.println("file not Deleted :" + fileName);
            }
        }

        return false;
    }

    public static String readFromFile(Context context, String fileName) {

        String ret = "";

        try {
            FileInputStream inputStream = new FileInputStream("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" + fileName + ".json");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        Log.e("raw data", "raw data : " + fileName + " "  +ret);

        return ret;
    }


    private static String readFromUploadFile(Context context, String fileName) {

        String ret = "";

        try {
            FileInputStream inputStream = new FileInputStream("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Upload/" + fileName + ".json");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        Log.e("login activity", "ret: " +ret);

        return ret;
    }


    public static void writeToFile(String raw, String fileName) {
        try {
            Log.i("raw", "raw " + raw + " " + fileName);

            File file = new File("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/Upload/", fileName + ".json");


            FileWriter out = new FileWriter(file);
            out.write(raw);
            out.close();

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file); //out is your output file
            mediaScanIntent.setData(contentUri);
            (MainActivity.mContext).sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void writeFromFile(String raw, String fullPath, String fileName) {
        try {
            Log.i("raw", "raw " + raw + " " + fileName);

            File file = new File(fullPath, fileName + ".json");

            if(!file.exists()) {
                file.createNewFile();
                Log.i("create", "createNewFile");
            } else {
                Log.i("noneed", "createNewFile");
            }

            FileWriter out = new FileWriter(file);
            out.write(raw);
            out.close();

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file); //out is your output file
            mediaScanIntent.setData(contentUri);
            (MainActivity.mContext).sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            Log.i("create", "exceptionReading");
            e.printStackTrace();
        }
    }

    public static String readFromFile(String readFullPath, String fileName) {
        String ret = "";

        try {
            FileInputStream inputStream = new FileInputStream(readFullPath/*"/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" +*/ + fileName + ".json");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        Log.e("raw data", "raw data : " + fileName + " "  +ret);

        return ret;
    }

}
