package com.spit.fam;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);
        getSupportActionBar().hide();

        WebView browser=(WebView)findViewById(R.id.webChart);
        browser.setWebViewClient(new WebViewClient(){

            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                //browser.loadUrl("javascript:loadChartData()");

                browser.evaluateJavascript("document.getElementById('labLoginName').innerHTML", new ValueCallback<String>(){

                    @Override
                    public void onReceiveValue(String s) {
                        Log.i("onReceiveValue", "onReceiveValue labLoginName " + s);
                    }
                });
            }

            @SuppressWarnings("deprecation") @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {

                browser.evaluateJavascript("document.getElementById('username').value", new ValueCallback<String>(){

                    @Override
                    public void onReceiveValue(String s) {
                        Log.i("onReceiveValue", "onReceiveValue username " + s + " " + url);
                    }
                });

                view.loadUrl(url);
                Log.i("url", "url " + url);

                return true;
            }

        });
        browser.getSettings().setJavaScriptEnabled(true);
        browser.addJavascriptInterface(new WebAppInterface(this), "Android");
        browser.loadUrl("http://icloud.securepro.com.hk/RFIDCSWSDWeb_V02/view/login.aspx");

    }

    public class WebAppInterface {
        Context mContext;
        String data;

        WebAppInterface(Context ctx){
            this.mContext=ctx;
        }


        @JavascriptInterface
        public void sendData(String data) {
            this.data=data;
            Log.i("data", "data " + data);
        }


        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

}