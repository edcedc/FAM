package com.spit.fam.SystemFragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.spit.fam.Entity.RenewEntity.ManualUpdateEvent;
import com.spit.fam.Entity.SPEntityP2.PhotoUploadRequest;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.StockTakeListItemRemark;
import com.spit.fam.Entity.User;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.DeletePosition;
import com.spit.fam.Event.RFIDDataUpdateEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Request.StockTakeListItemRemarkRequest;
import com.spit.fam.Request.UploadPhotoRequest;
import com.spit.fam.Response.LoginResponse;
import com.spit.fam.WebService.Callback.GetRemarkCallback;
import com.spit.fam.WebService.Callback.GetStatusCallback;
import com.spit.fam.WebService.Callback.UploadStockTakeListRemarkCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class AssetDetailStockTakeItemRemarkFragment extends BaseFragment {
    public static String REMARK_STRING = "";

    TextView status;
    EditText remark;
    StockTakeListItemRemark remarkData;
    ImageView photo_1, photo_2, dummy_1, dummy_2;
    ArrayList<Status> statusArrayList = new ArrayList<>();

    final int REQUEST_GALLERY = 9555;

    private int selectedStatusPosition = 0;
     int selected = -1;

     private ArrayList<String> photoList = new ArrayList<>();

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if(AssetsDetailWithTabFragment.realmStockTakeListAsset.getPic() != null && AssetsDetailWithTabFragment.realmStockTakeListAsset.getPic().length() > 0) {

            String[] picArray = AssetsDetailWithTabFragment.realmStockTakeListAsset.getPic().split(",");
            Log.i("pic", "pic " + AssetsDetailWithTabFragment.realmStockTakeListAsset.getPic() + " " + picArray);

            for(int i = 0; i < picArray.length; i++) {
                //if(picArray[i].startsWith("/storage")) {
                if( (picArray[i]).trim() .length() > 0) {
                    if(picArray[i].startsWith("/storage")) {
                        photoList.add( picArray[i].trim());
                    } else {
                        if(!picArray[i].startsWith((Hawk.get(InternalStorage.PIC_SITE, "")))) {
                            photoList.add((Hawk.get(InternalStorage.PIC_SITE, "") + picArray[i]).trim());
                        } else {
                            photoList.add( picArray[i].trim());
                        }
                    }
                    Log.i("asd", "Asd " + photoList.get(i));
                }

                //} else {
                    //photoList.add("http://icloud.securepro.com.hk/GS1AMS_Second/" + picArray[i]);
               // }
            }
        }
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.stock_take_list_item_asset_remark, null);
        photo_1 = view.findViewById(R.id.photo_1);
        photo_2 = view.findViewById(R.id.photo_2);

        dummy_1 = view.findViewById(R.id.dummy_1);
        dummy_2 = view.findViewById(R.id.dummy_2);

        /*ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

        if(remarkData == null) {
            remarkData = new StockTakeListItemRemark();
            try {

                for(int i = 0; i < StockTakeListItemFragment.stockTakeList.getAssets().size(); i++) {
                    if(StockTakeListItemFragment.stockTakeList.getAssets().get(i).getAssetno().equals(AssetsDetailWithTabFragment.asset.getAssetno())) {
                        Log.i("StockTakeListItemFragment", "StockTakeListItemFragment" + StockTakeListItemFragment.stockTakeList.getAssets().get(i).getRemarks());
                        remarkData.setRemark( StockTakeListItemFragment.stockTakeList.getAssets().get(i).getRemarks());
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        remarkData.remarkPhoto.clear();

        Log.i("SIZE", "SIZE " + AssetsDetailWithTabFragment.PICTURE_LIST.size());

        for(int i = 0; i < AssetsDetailWithTabFragment.PICTURE_LIST.size(); i++) {
            Photo photo = new Photo();
            Log.i("photo", "photo " + AssetsDetailWithTabFragment.PIC_SITE + AssetsDetailWithTabFragment.PICTURE_LIST.get(i));
            photo.setPhoto(AssetsDetailWithTabFragment.PIC_SITE + AssetsDetailWithTabFragment.PICTURE_LIST.get(i));
            photo.setDeletable(false);
            remarkData.remarkPhoto.add(photo);
        }

        for(int i = 0; i < arrayList.size(); i++) {
            Log.i("arrayList", "arrayList " + arrayList.get(i).getAssetNo() + " " + arrayList.get(i).getOrderNo());
            if(arrayList.get(i).getAssetNo() != null && arrayList.get(i).getAssetNo().equals(AssetsDetailWithTabFragment.asset.getAssetno())) {
                if(arrayList.get(i).getOrderNo() != null && arrayList.get(i).getOrderNo().equals( AssetsDetailWithTabFragment.realmStockTakeListAsset.getStocktakeno())) {
                    Photo photo = new Photo();
                    photo.setPhoto(arrayList.get(i).getFilePath());
                    photo.setDeletable(true);
                    remarkData.remarkPhoto.add(photo);
                }
            }
        }
*/
        photo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryViewPagerFragment galleryViewPagerFragment = new GalleryViewPagerFragment();
                //galleryViewPagerFragment.myPhoto
                if(remarkData != null && remarkData.remarkPhoto != null) {
                    ArrayList<String> data = new ArrayList<>();

                    Log.i("remarkData", "remarkData " + remarkData.remarkPhoto.size());

                    for(int i = 0; i < remarkData.remarkPhoto.size(); i++) {
                        data.add(remarkData.remarkPhoto.get(i).getPhoto());
                    }

                    galleryViewPagerFragment.remarkPhoto = remarkData.remarkPhoto;
                    galleryViewPagerFragment.myPhoto = data;//remarkData.getPhotos();

                }
                replaceFragment(galleryViewPagerFragment);
            }
        });

        photo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryViewPagerFragment galleryViewPagerFragment = new GalleryViewPagerFragment();
                ArrayList<String> data = new ArrayList<>();

                for(int i = 0; i < remarkData.remarkPhoto.size(); i++) {
                    data.add(remarkData.remarkPhoto.get(i).getPhoto());
                }

                galleryViewPagerFragment.myPhoto = data;
                galleryViewPagerFragment.remarkPhoto = remarkData.remarkPhoto;

                replaceFragment(galleryViewPagerFragment);
            }
        });

        status = view.findViewById(R.id.status_value);
        remark = (EditText)view.findViewById(R.id.remark_input);


        view.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);

                arrayAdapter.add(getString(R.string.camera));
                arrayAdapter.add(getString(R.string.gallery));


                builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            if(which == 1) {
                                MainActivity.SKIP_DOWNLOAD_ONCE = true;

                                pd = new ProgressDialog(AssetDetailStockTakeItemRemarkFragment.this.getActivity());
                                pd.setMessage("loading ... ");

                                selected = 1;

                                if (ContextCompat.checkSelfPermission(AssetDetailStockTakeItemRemarkFragment.this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(AssetDetailStockTakeItemRemarkFragment.this.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    Log.i("hihi", "hihi 1");
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(AssetDetailStockTakeItemRemarkFragment.this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        Log.i("hihi", "hihi 2");

                                    } else {
                                        Log.i("hihi", "hihi 3");

                                        requestPermissions(  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
                                    }
                                } else {
                                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    getActivity().startActivityForResult(Intent.createChooser(intent,"open gallery"),REQUEST_GALLERY);
                                }
                            } else {
                                MainActivity.SKIP_DOWNLOAD_ONCE = true;

                                selected = 0;

                                pd = new ProgressDialog(AssetDetailStockTakeItemRemarkFragment.this.getActivity());
                                pd.setMessage("loading ... ");

                                if (ContextCompat.checkSelfPermission(AssetDetailStockTakeItemRemarkFragment.this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(AssetDetailStockTakeItemRemarkFragment.this.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(AssetDetailStockTakeItemRemarkFragment.this.getActivity(),
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    } else {
                                        requestPermissions(  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
                                    }
                                } else {
                                    //Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                   // getActivity().startActivityForResult(Intent.createChooser(intent,"open gallery"),REQUEST_GALLERY);
                                }
                                // Create global camera reference in an activity or fragment
                                try {
                                    MainActivity.camera.takePicture();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                    }
                });
                builderSingle.show();
            }
        });

        view.findViewById(R.id.asset_remark_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i("hihi", "hihi " + (AssetsDetailWithTabFragment.IN_STOCK) + " " + (AssetsDetailWithTabFragment.SOURCE != null) + " "+ AssetsDetailWithTabFragment.SOURCE.equals("FORCE"));

                /*if(AssetsDetailWithTabFragment.IN_STOCK && AssetsDetailWithTabFragment.SOURCE != null && AssetsDetailWithTabFragment.SOURCE.equals("FORCE")) {
                    return;
                }*/
                Log.i("data","data " + AssetsDetailWithTabFragment.realmStockTakeListAsset);

                if(AssetsDetailWithTabFragment.realmStockTakeListAsset != null) {
                    Log.i("data","data " + AssetsDetailWithTabFragment.realmStockTakeListAsset.getStatusid() + " " + AssetsDetailWithTabFragment.realmStockTakeListAsset.getFindType());

                    if(AssetsDetailWithTabFragment.realmStockTakeListAsset.getFindType() != null && AssetsDetailWithTabFragment.realmStockTakeListAsset.getFindType().equals("rfid")) {
                        return;
                    }

                    if(AssetsDetailWithTabFragment.realmStockTakeListAsset.getStatusid() == 2 && AssetsDetailWithTabFragment.realmStockTakeListAsset.getTempStockTake() == false) {
                        return;
                    }


                    if(AssetsDetailWithTabFragment.realmStockTakeListAsset.getFindType() != null && (AssetsDetailWithTabFragment.realmStockTakeListAsset.getFindType().equals("barcode") || AssetsDetailWithTabFragment.realmStockTakeListAsset.getFindType().equals("rfid"))) {
                        return;
                    }
                }

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);

                if(LoginFragment.SP_API) {
                    arrayAdapter.add(getString(R.string.missing));
                    arrayAdapter.add(getString(R.string.in_library));
                } else {
                    for(int i = 0; i < statusArrayList.size(); i ++) {
                        arrayAdapter.add(statusArrayList.get(i).getName());
                    }
                }

                builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(LoginFragment.SP_API) {
                            selectedStatusPosition = which;
                            //remarkData.setStatus(statusArrayList.get(which));
                            if(which == 0) {
                                status.setText(getString(R.string.missing));
                            } else {
                                status.setText(getString(R.string.in_library));
                            }
                        } else {
                            selectedStatusPosition = which;
                            remarkData.setStatus(statusArrayList.get(which));
                            status.setText(statusArrayList.get(which).getName());
                        }
                    }
                });
                builderSingle.show();
            }
        });

        view.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        view.findViewById(R.id.remark_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

                        for(int i = 0; i < arrayList.size(); i++) {
                            if(arrayList.get(i).getOrderNo() != null && arrayList.get(i).getOrderNo().equals(StockTakeListItemFragment.stockTakeList.getOrderNo())) {
                                if(arrayList.get(i).getAssetNo().equals(AssetsDetailWithTabFragment.asset.getAssetno())) {
                                    arrayList.get(i).setConfirm(true);
                                }
                            }
                        }

                        Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, arrayList);


                        String key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + RenewStockTakeFragment.stocktakeno + "_" + AssetsDetailWithTabFragment.asset.getAssetno();

                        Log.i("data","data key " + remark.getText().toString());

                        Hawk.put(key, remark.getText().toString());
                    }
                });

                //String status_key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + AssetsDetailWithTabFragment.asset.getAssetno();

                if (status.getText().toString().equals(getString(R.string.missing))) {
                    Log.i("put case", "put case missing");
                    //Hawk.put(status_key, 0);
                } else if (status.getText().toString().equals(getString(R.string.in_library))) {
                    Log.i("put case", "put case in library");
                    //Hawk.put(status_key, 1);
                }


                /*
                for(int i = 0; i < StockTakeListItemFragment.stockTakeList.getAssets().size(); i++) {
                    if(StockTakeListItemFragment.stockTakeList.getAssets().get(i).getAssetno().equals(AssetsDetailWithTabFragment.asset.getAssetno())) {
                        if (status.getText().toString().equals(getString(R.string.missing))) {
                            StockTakeListItemFragment.stockTakeList.getAssets().get(i).setStatus(null);
                            StockTakeListItemFragment.stockTakeList.getAssets().get(i).setScanDateTime(null);
                            StockTakeListItemFragment.stockTakeList.getAssets().get(i).setFound(false);
                            StockTakeListItemFragment.stockTakeList.getAssets().get(i).setFindType("");
                        } else if (status.getText().toString().equals(getString(R.string.in_library))) {
                            StockTakeListItemFragment.stockTakeList.getAssets().get(i).getStatus().id = 2;
                            StockTakeListItemFragment.stockTakeList.getAssets().get(i).setScanDateTime(new Date());
                            StockTakeListItemFragment.stockTakeList.getAssets().get(i).setFound(true);
                            StockTakeListItemFragment.stockTakeList.getAssets().get(i).setFoundByManual();
                        }
                        break;
                    }
                }*/


                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(AssetsDetailWithTabFragment.asset.getEPC());

                        RFIDDataUpdateEvent rfidDataUpdateEvent = new RFIDDataUpdateEvent(arrayList);
                        rfidDataUpdateEvent.setManually(true);
                        rfidDataUpdateEvent.setFound(false);

                        if (status.getText().toString().equals(MainActivity.mContext.getString(R.string.missing))) {
                            rfidDataUpdateEvent.setFound(false);
                        } else if (status.getText().toString().equals(MainActivity.mContext.getString(R.string.in_library))) {
                            rfidDataUpdateEvent.setFound(true);
                        }
                        //EventBus.getDefault().post(rfidDataUpdateEvent);

                        ManualUpdateEvent manualUpdateEvent = new ManualUpdateEvent();
                        manualUpdateEvent.setAssetNo(AssetsDetailWithTabFragment.asset.getAssetno());
                        manualUpdateEvent.setRemark(((EditText)view.findViewById(R.id.remark_input)).getText().toString() );
                        Log.i("heyheyyoyo", "heyheyyoyoyo" + AssetsDetailWithTabFragment.asset.getAssetno());

                        if (status.getText().toString().equals(MainActivity.mContext.getString(R.string.missing))) {
                            manualUpdateEvent.setStatudID(10);
                        } else if (status.getText().toString().equals(MainActivity.mContext.getString(R.string.in_library))) {
                            manualUpdateEvent.setStatudID(2);
                        }

                        String result = "";//new ArrayList<>();

                        for(int i = 0; i < photoList.size(); i ++) {
                            //if(!photoList.get(i).startsWith("http://") && !photoList.get(i).startsWith("https://")) {
                            result += photoList.get(i) + ",";//.add(photoList.get(i));
                            //}
                        }

                        Log.i("result", "result " + result);

                        manualUpdateEvent.setPic(result);

                        EventBus.getDefault().post(manualUpdateEvent);
                    }
                };

                Handler handler = new Handler();
                handler.postDelayed(runnable, 300);

                getActivity().onBackPressed();
                /*
                if (AssetsDetailWithTabFragment.assetRemark != null && AssetsDetailWithTabFragment.assetRemark > 0) {
                    Log.i("case 1", "case 1");

                    StockTakeListItemRemarkRequest stockTakeListItemRemark = new StockTakeListItemRemarkRequest();
                    stockTakeListItemRemark.setRemark(remark.getText().toString());
                    stockTakeListItemRemark.setStatus(selectedStatusPosition + 1 );
                    stockTakeListItemRemark.setRemarkPhoto(remarkData.getStock_take_asset_item_remark_photos());
                    //stockTakeListItemRemark.setStock_take_asset_item_remark_photos(remarkData.getStock_take_asset_item_remark_photos());
                    Gson gson = new Gson();

                    Log.i("gson", "gson " + gson.toJson(stockTakeListItemRemark));

                    callUpdateAPI(stockTakeListItemRemark);
                } else {
                    Log.i("case 2", "case 2");

                    String key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + AssetsDetailWithTabFragment.asset.getAssetno();
                    Hawk.put(key, remark.getText().toString());
                }*/
            }
        });

        //String status_key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + AssetsDetailWithTabFragment.asset.getAssetno();

        int value = -1;//Hawk.get(status_key, -1);

       // setRemark(remarkData);

        /*
        if(AssetsDetailWithTabFragment.asset.getStatus() != null) {
            status.setText(AssetsDetailWithTabFragment.asset.getStatus().getName());
        }

        Log.i("status", "status " + AssetsDetailWithTabFragment.realmStockTakeListAsset.getStatusid());

        Log.i("IN_STOCK", "IN_STOCK " + AssetsDetailWithTabFragment.IN_STOCK + " " + value + " " + AssetsDetailWithTabFragment.asset.isFound());


        if(value != -1) {
            if(value == 0) {
                Log.i("remark", "remark case 1");
                status.setText(getString(R.string.missing));
            } else if(value == 1){
                Log.i("remark", "remark case 2");
                status.setText(getString(R.string.in_library));
            }
        } else if(AssetsDetailWithTabFragment.IN_STOCK) {
            value = 1;
            status.setText(getString(R.string.in_library));

            Log.i("remark", "remark case 3");
        } else {
            value = 0;
            status.setText(getString(R.string.missing));

            Log.i("remark", "remark case 4");
        }


        if(AssetsDetailWithTabFragment.asset.isFound()) {
            value = 1;
            status.setText(getString(R.string.in_library));

            Log.i("remark", "remark case 5");
        }


         */


        Log.i("statusstatus", "statusstatus" + AssetsDetailWithTabFragment.realmStockTakeListAsset.getStatusid());
        if(AssetsDetailWithTabFragment.realmStockTakeListAsset.getStatusid() == 9) {
            status.setText(getString(R.string.abnormal));

            view.findViewById(R.id.gallery_circle).setVisibility(View.GONE);
        } else if(AssetsDetailWithTabFragment.realmStockTakeListAsset.getStatusid() == 2) {
            status.setText(getString(R.string.in_library));
        } else {
            status.setText(getString(R.string.missing));
        }
        remark.setText(AssetsDetailWithTabFragment.realmStockTakeListAsset.getRemarks());

        Log.i("remark", "remark " + AssetsDetailWithTabFragment.realmStockTakeListAsset.getRemarks());

       /* String key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + AssetsDetailWithTabFragment.asset.getAssetno();
        remark.setText(Hawk.get(key, ""));

        Log.i("value", "value " + value + " " + AssetsDetailWithTabFragment.asset.isFound() + " " + REMARK_STRING + " " + Hawk.get(key, ""));

        //remark.setSelection(Hawk.get(key, "").length());
        if(REMARK_STRING != null && remark.getText().toString().length() > 0) {
            remark.setText(REMARK_STRING);
        }*/

        if(photoList.size() > 0)
            setImage(photoList);

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.i("hihi", "hihi" + requestCode + " " +  " " + permissions + " " + grantResults);
/*
        if(selected == 0) {
            try {
                MainActivity.camera.takePicture();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if(selected == 1) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getActivity().startActivityForResult(Intent.createChooser(intent,"open gallery"),REQUEST_GALLERY);
        }*/
        // Permission has already been granted
        //Intent intent = new Intent();
        //intent.setType("image/*");
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        //getActivity().startActivityForResult(Intent.createChooser(intent,"open gallery"),REQUEST_GALLERY);
    }

    public void callAPI(){
        if(!LoginFragment.SP_API) {
            RetrofitClient.getService().getStatusList().enqueue(new GetStatusCallback());

            Log.i("callAPI", "callAPI " + AssetsDetailWithTabFragment.assetRemark + " ");

            if (AssetsDetailWithTabFragment.assetRemark != null && AssetsDetailWithTabFragment.assetRemark > 0)
                RetrofitClient.getService().getRemark(AssetsDetailWithTabFragment.assetRemark).enqueue(new GetRemarkCallback());
        }
    }

    public static boolean updateCase;
    public void callUpdateAPI(StockTakeListItemRemarkRequest stockTakeListItemRemark) {
        if(!LoginFragment.SP_API) {

            updateCase = true;
            RetrofitClient.getService().updateRemark(AssetsDetailWithTabFragment.assetRemark, stockTakeListItemRemark).enqueue(new GetRemarkCallback());
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeletePosition event) {
        photoList.remove(event.getDeletePosition());
        Log.i("photoList", "photoList " + photoList.size() + " " + event.getDeletePosition());
        setImage(photoList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
/*      Legacy Coding
        if(AssetsDetailWithTabFragment.assetRemark != null && AssetsDetailWithTabFragment.assetRemark > 0) {
            ((TextView) view.findViewById(R.id.remark_save)).setText(getString(R.string.update));
        } else {
            ((TextView) view.findViewById(R.id.remark_save)).setText(getString(R.string.save));
        }

        if(event.getResponse() instanceof ArrayList && ((ArrayList)event.getResponse()).size() > 0) {
            Log.i("ImageReturn","imageReturn case 1");
            ArrayList<Photo> newData = remarkData.remarkPhoto;
            Photo photo = new Photo();

            try {
                BorrowListAssets borrowListAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + StockTakeListItemFragment.stockTakeList.getStocktakeno());//, event.getResponse());

                photo.setPhoto(borrowListAssets.getPicsite() + ((ImageReturn) ((ArrayList) event.getResponse()).get(0)).getVersion());
                photo.setDeletable(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //newData.add(photo);

            if (remarkData.remarkPhoto.size() < 5) {
                remarkData.remarkPhoto.add(photo);
            }
            Log.i("ImageReturn","imageReturn case 1.2 " + remarkData.remarkPhoto.size());


            setRemark(remarkData);
        } else {
            Log.i("ImageReturn","imageReturn case 2 ");
        }
        //Log.i("remark 0 ", "remark 0 " + event.getResponse().getClass() );

        if(event.getResponse() instanceof StockTakeListItemRemarkRequest) {
            //Log.i("remark 1 ", "remark 1 " + AssetsDetailWithTabFragment.assetRemark);

            if(!LoginFragment.SP_API) {
                if (AssetsDetailWithTabFragment.assetRemark != null && AssetsDetailWithTabFragment.assetRemark > 0)
                    RetrofitClient.getService().getRemark(AssetsDetailWithTabFragment.assetRemark).enqueue(new GetRemarkCallback());
            }
        }

        if(event.getResponse() instanceof StockTakeListItemRemark) {
            remarkData = (StockTakeListItemRemark)event.getResponse();

            ArrayList<StockTakeListItemRemark> stockTakeListItemRemarks = Hawk.get(InternalStorage.OFFLINE_CACHE.STOCK_TAKE_REMARK_LIST, new ArrayList<StockTakeListItemRemark>());
            stockTakeListItemRemarks.add(remarkData);
            Hawk.put(InternalStorage.OFFLINE_CACHE.STOCK_TAKE_REMARK_LIST, stockTakeListItemRemarks);//new ArrayList<StockTakeListItemRemark>());

            Log.i("remark", "remark 2 " + remarkData + " " + remarkData.getStock_take_asset_item_remark_photos().size());

            //if(remarkData != null)
            //    Log.i("remark", "remark " + remarkData.getRemark());


            setRemark(remarkData);

            if(updateCase) {
                //Update Success
                updateCase = false;
                getActivity().onBackPressed();
            }

            if(photoPath != null) {
                AssetsDetailWithTabFragment.assetRemark = remarkData.getId();
                uploadPhoto(photoPath);
                photoPath = null;
            }

        }



        if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Status.class) {
            statusArrayList = (ArrayList<Status>) event.getResponse();
            status.setText(statusArrayList.get(0).getName());

            Hawk.put(InternalStorage.OFFLINE_CACHE.STOCK_TAKE_STATUS,  (ArrayList<Status>) event.getResponse());
        }*/
    }

    StockTakeListItemRemark localRemarkData;
    public void setRemark (StockTakeListItemRemark remarkData) {
        if(remarkData == null)
            return;

        localRemarkData = remarkData;

        Log.i("status", "status " + remarkData.remarkPhoto.size() + " " + remarkData.getStatus().id);
        if(!UPDATE_PHOTO) {
            try {
                status.setText(statusArrayList.get(remarkData.getStatus().id - 1).getName());
            } catch (Exception e) {
                statusArrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.STOCK_TAKE_STATUS,  new ArrayList<>());//(ArrayList<Status>) event.getResponse());
                if(statusArrayList.size() > 0)
                    status.setText(statusArrayList.get(remarkData.getStatus().id - 1).getName());
            }

            remark.setText(Html.fromHtml(remarkData.getRemark().replace("\\n", "<br>")));
            //remark.setSelection(remarkData.getRemark().length());

            if(REMARK_STRING != null && REMARK_STRING.length() > 0) {
                remark.setText(Html.fromHtml(REMARK_STRING.replace("\\n", "<br>")));
                //remark.setSelection((Hawk.get(key, "")).length());
            }

            Log.i("remarkData", "remarkData 1 " + remark.getText().toString());

        }
        //Log.i("size", "size " + remarkData.getStock_take_asset_item_remark_photos());

        // 9
        int rowCount = (int)Math.ceil((double)remarkData.remarkPhoto.size() / 3f);

        Log.i("rowCount", "rowCount " + rowCount + " " + remarkData.remarkPhoto.size() + " " + remarkData.remarkPhoto.size() / 3 + " " + remarkData.remarkPhoto.size());

        ((ViewGroup)view.findViewById(R.id.image_wrapper)).removeAllViews();

        ((LinearLayout)view.findViewById(R.id.image_wrapper)).setOrientation(LinearLayout.VERTICAL);

        for(int i = 0; i < rowCount; i++) {
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.image_row, null);
            ImageView p1 = linearLayout.findViewById(R.id.dummy_1);
            ImageView p2 = linearLayout.findViewById(R.id.dummy_2);
            ImageView p3 = linearLayout.findViewById(R.id.dummy_3);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.app_logo);
            requestOptions.error(R.drawable.app_logo);
            requestOptions.fallback(R.drawable.app_logo);

            //6 7 8

            for(int x = i * 3; x < i * 3 + 3 && x < remarkData.remarkPhoto.size(); x++) {
                //int
                int finalI = i;

                int position = x % 3;
                Log.i("position", "position " + position + " " + remarkData.getStock_take_asset_item_remark_photos().get(i * 3 + position).getPhoto() + " " + AssetsDetailWithTabFragment.PIC_SITE);
                if(position == 0) {
                    Glide.with(this).setDefaultRequestOptions(requestOptions).load(remarkData.getStock_take_asset_item_remark_photos().get(i * 3 + position).getPhoto().trim()).into(p1);
                    p1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            GalleryViewPagerFragment galleryViewPagerFragment = new GalleryViewPagerFragment();
                            //galleryViewPagerFragment.myPhoto
                            if(remarkData != null && remarkData.remarkPhoto != null) {
                                ArrayList<String> data = new ArrayList<>();

                                Log.i("remarkData", "remarkData " + remarkData.remarkPhoto.size());

                                for(int i = 0; i < remarkData.remarkPhoto.size(); i++) {
                                    data.add(remarkData.remarkPhoto.get(i).getPhoto());
                                }

                                galleryViewPagerFragment.remarkPhoto = remarkData.remarkPhoto;
                                galleryViewPagerFragment.myPhoto = data;//remarkData.getPhotos();
                                GalleryViewPagerFragment.POSITION = finalI * 3 + position;
                            }
                            replaceFragment(galleryViewPagerFragment);
                        }
                    });
                } else if(position == 1) {
                    Glide.with(this).setDefaultRequestOptions(requestOptions).load(remarkData.getStock_take_asset_item_remark_photos().get(i * 3 + position).getPhoto().trim()).into(p2);
                    p2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            GalleryViewPagerFragment galleryViewPagerFragment = new GalleryViewPagerFragment();
                            //galleryViewPagerFragment.myPhoto
                            if(remarkData != null && remarkData.remarkPhoto != null) {
                                ArrayList<String> data = new ArrayList<>();

                                Log.i("remarkData", "remarkData " + remarkData.remarkPhoto.size());

                                for(int i = 0; i < remarkData.remarkPhoto.size(); i++) {
                                    data.add(remarkData.remarkPhoto.get(i).getPhoto());
                                }

                                galleryViewPagerFragment.remarkPhoto = remarkData.remarkPhoto;
                                galleryViewPagerFragment.myPhoto = data;//remarkData.getPhotos();
                                GalleryViewPagerFragment.POSITION = finalI * 3 + position;


                            }
                            replaceFragment(galleryViewPagerFragment);
                        }
                    });
                } else if(position == 2) {
                    Glide.with(this).setDefaultRequestOptions(requestOptions).load(remarkData.getStock_take_asset_item_remark_photos().get(i * 3 + position).getPhoto().trim()).into(p3);

                    p3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            GalleryViewPagerFragment galleryViewPagerFragment = new GalleryViewPagerFragment();
                            //galleryViewPagerFragment.myPhoto
                            if(remarkData != null && remarkData.remarkPhoto != null) {
                                ArrayList<String> data = new ArrayList<>();

                                Log.i("remarkData", "remarkData " + remarkData.remarkPhoto.size());

                                for(int i = 0; i < remarkData.remarkPhoto.size(); i++) {
                                    data.add(remarkData.remarkPhoto.get(i).getPhoto());
                                }

                                galleryViewPagerFragment.remarkPhoto = remarkData.remarkPhoto;
                                galleryViewPagerFragment.myPhoto = data;//remarkData.getPhotos();
                                GalleryViewPagerFragment.POSITION = finalI * 3 + position;


                            }
                            replaceFragment(galleryViewPagerFragment);
                        }
                    });
                }
            }
            Log.i("addView", "addView " + i);
            ((ViewGroup)view.findViewById(R.id.image_wrapper)).addView(linearLayout);
        }

        ((ViewGroup)view.findViewById(R.id.image_wrapper)).invalidate();

        if(remarkData.remarkPhoto != null && remarkData.remarkPhoto.size() == 5) {
            view.findViewById(R.id.gallery).setVisibility(View.GONE);
        }


        UPDATE_PHOTO = false;

        String key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + AssetsDetailWithTabFragment.asset.getAssetno();

        if(remarkData.getRemark() != null && remarkData.getRemark().length() > 0) {
            Log.i("remarkData", "remarkData 2001 " + remarkData.getRemark().replace("\\n", "<br>") );
            remark.setText(Html.fromHtml(remarkData.getRemark().replace("\\n", "<br>")));
        } else if((Hawk.get(key, null) != null)) {
            Log.i("remarkData", "remarkData 2002 " + Hawk.get(key, null));
            remark.setText(Html.fromHtml(Hawk.get(key, "").replace("\\n", "<br>")));
        } else if(REMARK_STRING != null && REMARK_STRING.length() > 0) {
            Log.i("remarkData", "remarkData 2003 " + REMARK_STRING);
            remark.setText(Html.fromHtml(REMARK_STRING.replace("\\n", "<br>")));
        }
        Log.i("remarkData", "remarkData 2 " + remark.getText().toString());

    }

    public void setImage(ArrayList<String> data) {
        if(data == null) {
            return;
        }

        int rowCount = (int)Math.ceil((double)data.size() / 3f);

        Log.i("rowCount", "rowCount " + rowCount + " " + data.size() + " " + data.size() / 3 + " " + data.size());

        ((ViewGroup)view.findViewById(R.id.image_wrapper)).removeAllViews();

        ((LinearLayout)view.findViewById(R.id.image_wrapper)).setOrientation(LinearLayout.VERTICAL);

        for(int i = 0; i < rowCount; i++) {
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.image_row, null);
            ImageView p1 = linearLayout.findViewById(R.id.dummy_1);
            ImageView p2 = linearLayout.findViewById(R.id.dummy_2);
            ImageView p3 = linearLayout.findViewById(R.id.dummy_3);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.app_logo);
            requestOptions.error(R.drawable.app_logo);
            requestOptions.fallback(R.drawable.app_logo);

            //6 7 8

            for(int x = i * 3; x < i * 3 + 3 && x < data.size(); x++) {
                //int
                int finalI = i;

                int position = x % 3;
                Log.i("position", "position " + position + " " + data.get(i * 3 + position) );
                if(position == 0) {
                    Glide.with(this).setDefaultRequestOptions(requestOptions).load(data.get(i * 3 + position)).into(p1);
                    p1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            GalleryViewPagerFragment galleryViewPagerFragment = new GalleryViewPagerFragment();

                            if(photoList != null) {
                                ArrayList<String> data = new ArrayList<>();

                                for(int i = 0; i < photoList.size(); i++) {
                                    data.add(photoList.get(i));
                                }

                                //galleryViewPagerFragment.remarkPhoto = remarkData.remarkPhoto;
                                galleryViewPagerFragment.myPhoto = data;//remarkData.getPhotos();
                                GalleryViewPagerFragment.POSITION = finalI * 3 + position;
                            }
                            replaceFragment(galleryViewPagerFragment);

                        }
                    });
                } else if(position == 1) {
                    Glide.with(this).setDefaultRequestOptions(requestOptions).load(data.get(i * 3 + position)).into(p2);
                    p2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            GalleryViewPagerFragment galleryViewPagerFragment = new GalleryViewPagerFragment();

                            if(photoList != null) {
                                ArrayList<String> data = new ArrayList<>();

                                for(int i = 0; i < photoList.size(); i++) {
                                    data.add(photoList.get(i));
                                }

                                //galleryViewPagerFragment.remarkPhoto = remarkData.remarkPhoto;
                                galleryViewPagerFragment.myPhoto = data;//remarkData.getPhotos();
                                GalleryViewPagerFragment.POSITION = finalI * 3 + position;
                            }
                            replaceFragment(galleryViewPagerFragment);
                        }
                    });
                } else if(position == 2) {
                    Glide.with(this).setDefaultRequestOptions(requestOptions).load(data.get(i * 3 + position)).into(p3);

                    p3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            GalleryViewPagerFragment galleryViewPagerFragment = new GalleryViewPagerFragment();

                            if(photoList != null) {
                                ArrayList<String> data = new ArrayList<>();

                                for(int i = 0; i < photoList.size(); i++) {
                                    data.add(photoList.get(i));
                                }

                                //galleryViewPagerFragment.remarkPhoto = remarkData.remarkPhoto;
                                galleryViewPagerFragment.myPhoto = data;//remarkData.getPhotos();
                                GalleryViewPagerFragment.POSITION = finalI * 3 + position;
                            }
                            replaceFragment(galleryViewPagerFragment);
                        }
                    });
                }
            }
            Log.i("addView", "addView " + i);
            ((ViewGroup)view.findViewById(R.id.image_wrapper)).addView(linearLayout);
        }

        ((ViewGroup)view.findViewById(R.id.image_wrapper)).invalidate();

        if(photoList != null && photoList.size() == 5) {
            view.findViewById(R.id.gallery).setVisibility(View.GONE);
        }
    }

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    public File bitmapToFile(Context context, Bitmap bitmap, String fileNameToSave) {
        File file = null;
        try {
            ContextWrapper cw = new ContextWrapper(AssetDetailStockTakeItemRemarkFragment.this.getActivity().getApplicationContext()    );

            file = new File(cw.getCacheDir() + File.separator + fileNameToSave);
            file.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50 , bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e){
            e.printStackTrace();
            return file; // it will return null
        }
    }

    public String photoPath = null;
    public boolean UPDATE_PHOTO = false;

    public void uploadPhoto(String photoPath) {

        if(!LoginFragment.SP_API) {
            try {
                UPDATE_PHOTO = true;

                UploadPhotoRequest uploadPhotoRequest = new UploadPhotoRequest();
                uploadPhotoRequest.setPhoto(photoPath);
                User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
                uploadPhotoRequest.setCreated_by(user.getId());
                uploadPhotoRequest.setUpdated_by(user.getId());

                StockTakeListItemRemarkRequest stockTakeListItemRemarkRequest = new StockTakeListItemRemarkRequest();
                stockTakeListItemRemarkRequest.setId(AssetsDetailWithTabFragment.assetRemark);
                stockTakeListItemRemarkRequest.setRemark(remark.getText().toString());

                stockTakeListItemRemarkRequest.setStatus(selectedStatusPosition + 1);

                uploadPhotoRequest.setStock_take_asset_item_remark(stockTakeListItemRemarkRequest);

                Gson gson = new Gson();
                Log.i("uplaodJson", "uploadJson " + gson.toJson(uploadPhotoRequest));


                RetrofitClient.getService().uploadRemarkPhoto(uploadPhotoRequest).enqueue(new UploadStockTakeListRemarkCallback());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        //RetrofitClient.getService().getRemark(AssetsDetailWithTabFragment.assetRemark).enqueue(new GetRemarkCallback());
                    }
                };
                Handler handler = new Handler();
                handler.postDelayed(runnable, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onResume() {
        super.onResume();
        if(((MainActivity)getActivity()).isNetworkAvailable()) {
            callAPI();
        } else {
            EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.STOCK_TAKE_STATUS,  new ArrayList<Status>())));
            ArrayList<StockTakeListItemRemark> stockTakeListItemRemarkList = (Hawk.get(InternalStorage.OFFLINE_CACHE.STOCK_TAKE_REMARK_LIST,  new ArrayList<StockTakeListItemRemark>()));

            for(int i = 0; i < stockTakeListItemRemarkList.size(); i++) {
                // for(int y = 0; y < StockTakeListItemFragment.stockTakeList.getStockTakeListItems().size(); y++) {
                Log.i("data", "data " + stockTakeListItemRemarkList.get(i).getId() + " " + AssetsDetailWithTabFragment.assetRemark + " " + AssetsDetailWithTabFragment.asset.getStock_take_asset_item_remark() );//StockTakeListItemFragment.stockTakeList.getStockTakeListItems().get(y).getStock_take_asset_item_remark());

                if (stockTakeListItemRemarkList.get(i).getId() == AssetsDetailWithTabFragment.assetRemark ) {
                    EventBus.getDefault().post(new CallbackResponseEvent(stockTakeListItemRemarkList.get(i)));
                    break;
                }
                //}
            }
        }
    }

    static String encodedString = "";
    static String fileExtension = "";

    public void onPause() {
        super.onPause();
        try {
            //String key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + AssetsDetailWithTabFragment.asset.getAssetno();
            //Hawk.put(key, remark.getText().toString());
            REMARK_STRING = remark.getText().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("onPause", "onPause assetDetail " + REMARK_STRING + " " + remark.getText().toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FileChosenEvent fileEvent) {
        Log.i("FileChosenEvent", "FileChosenEvent ");

        if(photoList.size() < 5)
            photoList.add(fileEvent.filepath);
        setImage(photoList);

/*
        encodedString = null;
        Glide.with(this.getActivity())
                .asBitmap().load(fileEvent.filepath).into(new SimpleTarget<Bitmap>(100,100) {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                File f = new File(AssetDetailStockTakeItemRemarkFragment.this.getActivity().getCacheDir(), "asd.jpg");
                try {
                    f.createNewFile();

                    Bitmap bitmap = resource;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70 , bos);
                    byte[] bitmapdata = bos.toByteArray();

                    encodedString = Base64.encodeToString(bitmapdata, Base64.DEFAULT);

                    String cloneString = encodedString;

                    int i = 3000;
                    while (cloneString.length() > i) {
                        Log.e("encodedString", "Substring: "+ cloneString.substring(0, i));
                        cloneString = cloneString.substring(i);
                    }
                    Log.e("encodedString", "Substring: "+ cloneString);
                    FileOutputStream fos = null;

                    fos = new FileOutputStream(f);

                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();

                    if(photoList.size() < 5)
                        photoList.add(fileEvent.filepath);
                    setImage(photoList);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                pd.hide();

                /*
                Legacy Coding
                if(!((MainActivity)getActivity()).isNetworkAvailable()) {


                    ArrayList<Photo> newData = remarkData.remarkPhoto;
                    Photo photo = new Photo();
                    photo.setPhoto(fileEvent.filepath);
                    newData.add(photo);

                    remarkData.remarkPhoto = (newData);

                    Log.i("remarkData" , "remarkData " +  remarkData.remarkPhoto.size());

                    setRemark(remarkData);
                    return;
                }
*/
                /*
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                String userId = Hawk.get(InternalStorage.Login.USER_ID, "");
                String assetno = AssetsDetailWithTabFragment.asset.getAssetno();
                String rono = AssetsDetailWithTabFragment.asset.getRono();
                String stockTakeListId = StockTakeListItemFragment.stockTakeList.getOrderNo();

                Log.i("companyId" ,"companyId " + companyId);
                Log.i("rono" ,"rono " + rono);
                Log.i("stockTakeListId" ,"stockTakeListId " + stockTakeListId);
                Log.i("data", "data " + encodedString);

                if(false) {
                    RetrofitClient.getSPGetWebService().uploadFileToByte(companyId, encodedString, "jpg", rono, 1, userId, stockTakeListId).enqueue(new ImageReturnCallback());
                } else {
                    Log.i("MainActivity.OFFLINE_MODE", "MainActivity.OFFLINE_MODE");
                    if(remarkData == null) {
                        remarkData = new StockTakeListItemRemark();
                    }

                    ArrayList<Photo> newData = remarkData.remarkPhoto;
                    Photo photo = new Photo();
                    //photo.setEncodedString(encodedString);
                    photo.setPhoto(fileEvent.filepath);
                    photo.setDeletable(true);
                    //newData.add(photo);

                    if(remarkData.remarkPhoto.size() < 5) {
                        remarkData.remarkPhoto.add(photo);

                        setRemark(remarkData);

                        PhotoUploadRequest photoUploadRequest = new PhotoUploadRequest();
                        photoUploadRequest.setCompanyId(companyId);
                        photoUploadRequest.setAssetNo(assetno);
                        photoUploadRequest.setRono(rono);
                        //photoUploadRequest.setEncodedString(encodedString);
                        photoUploadRequest.setFilePath(fileEvent.filepath);
                        photoUploadRequest.setUserId(userId);
                        photoUploadRequest.setOrderNo(AssetsDetailWithTabFragment.realmStockTakeListAsset.getStocktakeno());

                        int count = 0;

                        ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());
                        for (int i = 0; i < arrayList.size(); i++) {
                            if (arrayList.get(i).getAssetNo() != null && arrayList.get(i).getAssetNo().equals(assetno)) {
                                if (arrayList.get(i).getOrderNo() != null && arrayList.get(i).getOrderNo().equals(stockTakeListId)) {
                                    count++;
                                }
                            }
                        }
                        photoUploadRequest.setFileLoc(AssetsDetailWithTabFragment.PICTURE_LIST.size() + count);

                        arrayList.add(photoUploadRequest);

                        Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, arrayList);
                    }

                }*/
           // }
       // }) ;



/*
        Bitmap bitmap = BitmapFactory.decodeFile(fileEvent.filepath);

        ((ImageView)view.findViewById(R.id.dummy)).setImageBitmap(bitmap);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ((BitmapDrawable)((ImageView)view.findViewById(R.id.dummy)).getDrawable()).getBitmap().compress(Bitmap.CompressFormat.PNG, 30, out);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        File newFile = bitmapToFile(AssetDetailStockTakeItemRemarkFragment.this.getActivity(),     decoded, "asd.png");
*/

    }

    ProgressDialog pd;

}
