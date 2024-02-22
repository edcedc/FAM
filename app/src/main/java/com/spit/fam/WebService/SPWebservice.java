package com.spit.fam.WebService;

import android.os.AsyncTask;

public class SPWebservice {
    public static String NAMESPACE = "http://tempuri.org/";
    public static String URL =  "http://icloud.securepro.com.hk/AMSWebService_Template/MobileWebService.asmx";
    public static String SOAP_ACTION, METHOD_NAME;
    public static Object object = null;

    public static String GetCheckLogin = "GetCheckLogin";

    public static void callAPI() {

    }

    private static class AsyncCallWS extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            callAPI();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Toast.makeText(MainActivity.this, "Response" + resultString.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void callAPI(String METHOD_NAME, Object object) {
        SOAP_ACTION = "http://tempuri.org/" + METHOD_NAME;
        SPWebservice.METHOD_NAME = METHOD_NAME;
        SPWebservice.object = object;

        AsyncCallWS task = new AsyncCallWS();
        task.execute();
    }
}
