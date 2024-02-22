package com.spit.fam.WebService

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.spit.fam.BaseUtils
import com.spit.fam.Event.CallbackResponseEvent
import com.spit.fam.Event.LoginDownloadProgressEvent
import com.spit.fam.InternalStorage
import com.spit.fam.MainActivity
import com.spit.fam.SystemFragment.DownloadFragment
import com.github.kittinunf.fuel.Fuel
import com.orhanobut.hawk.Hawk
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

public class APIUtils {
    companion object {
        @JvmStatic
        public fun download() {

            val companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")
            val serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") //response.body().getData().get(i).getUserid());


            var apiRoot = Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "");

            if (apiRoot.endsWith("/")) {

            } else {
                apiRoot = apiRoot + "/"
            }

            Log.i("raw", "raw " + apiRoot + "MobileWebService.asmx/assetsDetail?userid=" + serverId + "&companyid=" + companyId + "&assetno=&lastcalldate=" + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, ""))

            Fuel.download(apiRoot + "MobileWebService.asmx/assetsDetail?userid=" + serverId + "&companyid=" + companyId + "&assetno=&lastcalldate=" + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).destination { response, url ->
                File.createTempFile("temp", ".tmp")
            }.timeoutRead(600000)
                    .responseProgress{ readBytes, totalBytes ->
                        val progress = readBytes.toFloat() / totalBytes.toFloat()

                        if (progress > 0) {
                            EventBus.getDefault().post(LoginDownloadProgressEvent(progress))
                        }

                    }.response { request, response, result ->
                        val (data, error) = result
                        if (error != null) {
                            Log.e("download", "error: ${error}")
                        } else {
                            result.fold({ bytes ->

                                try {
                                    val outputStreamWriter = OutputStreamWriter(MainActivity.mContext!!.openFileOutput("master.json", Context.MODE_PRIVATE))
                                    outputStreamWriter.write(java.lang.String(response.data, StandardCharsets.UTF_8).toString())
                                    outputStreamWriter.close()

                                    AsyncTask.execute {

                                        //Realm.getDefaultInstance().beginTransaction()
                                        //Realm.getDefaultInstance().delete(AssetsDetail::class.java)
                                        //Realm.getDefaultInstance().commitTransaction()

                                        BaseUtils.parseLargeJson(MainActivity.mContext!!.getFilesDir().toString() + "/" + ("master.json"));
                                    }

                                } catch (e: Exception) {
                                }

                            }, { err ->
                            })
                        }
                    }

        }

        @JvmStatic
        public fun download2(orderno: String) {

            val companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")
            val serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") //response.body().getData().get(i).getUserid());


            var apiRoot = if (Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").endsWith("/")) Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "") else Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").toString() + "/";//RetrofitClient.api;//(if (RetrofitClient.api.startsWith("http://")) RetrofitClient.api else "http://" + RetrofitClient.api) +
            if (apiRoot.endsWith("/")) {

            } else {
                apiRoot = apiRoot + "/"
            }

            Log.i("raw", "raw " + apiRoot + "MobileWebService.asmx/stockTakeListAsset?userid=" + serverId + "&companyid=" + companyId + "&orderno=" + orderno)

            //http://icloud.securepro.com.hk/GS1AMSWebService_Second/MobileWebService.asmx/stockTakeListAsset?companyid=gs1&userid=&orderno=0000000111
            Fuel.download(apiRoot + "MobileWebService.asmx/stockTakeListAsset?userid=" + serverId + "&companyid=" + companyId + "&orderno=" + orderno).destination { response, url ->
                File.createTempFile("temp", ".tmp")
            }.timeoutRead(600000)
                    .responseProgress{ readBytes, totalBytes ->
                        val progress = readBytes.toFloat() / totalBytes.toFloat()

                        Log.i("progress", "progress " + progress);

                        if (progress > 0) {
                            EventBus.getDefault().post(LoginDownloadProgressEvent(progress))
                        }

                    }.response { request, response, result ->
                        val (data, error) = result
                        if (error != null) {
                            Log.e("download", "error: ${error}")
                        } else {
                            //Log.i("result" , "result " +  String(result.get(), StandardCharsets.UTF_8));

                            result.fold({ bytes ->
                                try {
                                    val outputStreamWriter = OutputStreamWriter(MainActivity.mContext!!.openFileOutput("stockTakeListAsset" + orderno + ".json", Context.MODE_PRIVATE))
                                    outputStreamWriter.write(java.lang.String(response.data, StandardCharsets.UTF_8).toString())
                                    outputStreamWriter.close()

                                    AsyncTask.execute {

                                        //Realm.getDefaultInstance().beginTransaction()
                                        //Realm.getDefaultInstance().delete(AssetsDetail::class.java)
                                        //Realm.getDefaultInstance().commitTransaction()

                                        BaseUtils.parseStockTakeJson(MainActivity.mContext!!.getFilesDir().toString() + "/" + ("stockTakeListAsset" + orderno + ".json"));

                                        if (DownloadFragment.stockTakeListData.size > 0) {
                                            DownloadFragment.stockTakeListData.removeAt(0);
                                            EventBus.getDefault().post(CallbackResponseEvent(""));
                                        }
                                    }
                                } catch (e: Exception) {
                                }

                            }, { err ->
                            })
                        }
                    }

        }
    }
}