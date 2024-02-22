package com.spit.fam.SystemFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.spit.fam.Entity.Photo;
import com.spit.fam.Entity.SPEntityP2.PhotoUploadRequest;
import com.spit.fam.Entity.StockTakeListItemRemark;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.DeletePosition;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class GalleryViewPagerFragment extends BaseFragment {
    ViewPager viewpager;
    public ArrayList<String> myPhoto = new ArrayList<>();
    public ArrayList<Photo> remarkPhoto = new ArrayList<Photo>();

    public static int POSITION = -1;
    public static boolean DELETE = false;

    public void onDestroy() {
        super.onDestroy();
        ((MainActivity)getActivity()).setBadgeVisibility(View.VISIBLE);
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.gallery_view_pager_fragment, null);

        ((MainActivity)getActivity()).setBadgeVisibility(View.GONE);

        viewpager = view.findViewById(R.id.viewpager);

        viewpager.setAdapter(new ViewPagerAdapter(GalleryViewPagerFragment.this.getActivity()));

        if(POSITION >= 0) {
            viewpager.setCurrentItem(POSITION);
        }

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GalleryViewPagerFragment.this.getActivity() != null)
                    GalleryViewPagerFragment.this.getActivity().onBackPressed();
            }
        });

        return view;
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

    public class ViewPagerAdapter extends PagerAdapter {

        private Context context;
        private LayoutInflater layoutInflater;
        //private Integer [] images = {R.drawable.slide1,R.drawable.slide2,R.drawable.slide3};

        public ViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return myPhoto.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.gallery_view_pager_item, null);



            view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new DeletePosition(position));
                        }
                    };

                    Handler handler = new Handler();
                    handler.postDelayed(runnable, 400);

                    ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

                    Log.i("arrayList", "arrayList " + arrayList.size() + " " + position);


                    StockTakeListItemRemark remarkData = null;

                    if(remarkData == null) {
                        remarkData = new StockTakeListItemRemark();
                    }
                    remarkData.remarkPhoto.clear();

                    int count = 0;

                    for(int i = 0; i < arrayList.size(); i++) {
                        if(arrayList.get(i).getAssetNo() != null && arrayList.get(i).getAssetNo().equals(AssetsDetailWithTabFragment.asset.getAssetno())) {
                            if(count == position - AssetsDetailWithTabFragment.PICTURE_LIST.size()) {
                                arrayList.remove(i);
                                break;
                            }
                            count++;
                        }
                    }
                    Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, arrayList);
                    getActivity().onBackPressed();
                    //Log.i("photo id", "photo id " + remarkPhoto.get(position).getId());
                    //RetrofitClient.getService().deletePhoto(remarkPhoto.get(position).getId()).enqueue(new DeletePhotoCallback());
                }
            });

            ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
            Button delete = (Button) view.findViewById(R.id.delete);

            //imageView.setImageResource(myPhoto.get(position));
            Glide.with(GalleryViewPagerFragment.this.getActivity()).load((myPhoto.get(position)).trim()).into(imageView);

            ((TextView)view.findViewById(R.id.title)).setText((position + 1) + " / " + myPhoto.size());

            if(myPhoto != null) {
                if(myPhoto.get(position).startsWith("http://") || myPhoto.get(position).startsWith("https://")) {
                    delete.setVisibility(View.GONE);
                } else {
                    delete.setVisibility(View.VISIBLE);
                }
            }

            ViewPager vp = (ViewPager) container;
            vp.addView(view, 0);
            return view;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            ViewPager vp = (ViewPager) container;
            View view = (View) object;
            vp.removeView(view);

        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        if(event.getResponse() != null && event.getResponse() instanceof Photo) {
            getActivity().onBackPressed();
        }
    }
}

