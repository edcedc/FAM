package com.spit.fam.WebService;

import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.BorrowList;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.Photo;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.StockTakeList;
import com.spit.fam.Entity.StockTakeListItem;
import com.spit.fam.Entity.StockTakeListItemRemark;
import com.spit.fam.Entity.StockTakeListItemRequest;
import com.spit.fam.Request.LoginRequest;
import com.spit.fam.Request.StockTakeListItemRemarkRequest;
import com.spit.fam.Request.UploadPhotoRequest;
import com.spit.fam.Response.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StrapiService {

    @Headers("Content-Type: application/json")
    @POST("/auth/local")
    Call<LoginResponse> login(@Body LoginRequest body);

    @GET("/assets?EPC_gt=\"\"")
    Call<List<Asset>> getAssetList(@Query("user_groups.id") int userGroupId);

    //http://34.123.213.205:1337/assets?assetno_contains=2&name_contains=razer&brand_contains=Ra&Model_contains=2020&categories.Name=cat2&locations.name=Shatin&categories.Name=cat1
    @GET("/assets")
    Call<List<Asset>> getAssetList(@Query("user_groups.id") int userGroupId, @Query("assetno_contains") String assetno, @Query("name_contains") String name, @Query("brand_contains") String brand, @Query("Model_contains") String modelbrand, @Query("categories.Name") String cat1, @Query("categories.Name") String cat2, @Query("categories.Name") String cat3, @Query("locations.name") String location);

    @GET("/assets")
    Call<List<Asset>> getAssetList(@Query("user_groups.id") int userGroupId, @Query("assetno_contains") String assetno, @Query("name_contains") String name, @Query("brand_contains") String brand, @Query("Model_contains") String modelbrand, @Query("categories.Name") String cat1, @Query("categories.Name") String cat2, @Query("categories.Name") String cat3, @Query("locations.name") String location, @Query("EPC") String EPC);

    @GET("/assets")
    Call<List<Asset>> getAssetList(@Query("user_groups.id") int userGroupId, @Query("EPC") String EPC);

    @GET("/assets?EPC_lt=\"\"")
    Call<List<Asset>> getAssetListEPCEmpty(@Query("user_groups.id") int userGroupId);

    @GET("/assets/{id}")
    Call<Asset> getAsset(@Path("id") int id);

    @GET("/assets/")
    Call<List<Asset>> getAssets(@Query("user_groups.id") int userGroupId, @Query("id") String ids);

    @GET("/assets/")
    Call<List<Asset>> getAssets(@Query("id") List<Long> taskIds);

    @GET("/locations")
    Call<List<Location>> getLocationList();

    @GET("/categories")
    Call<List<Category>> getCategoryList();

    @GET("/statuses")
    Call<List<Status>> getStatusList();

    @GET("borrow-lists")
    Call<List<BorrowList>> getBorrowLists(@Query("applied_by.id") int userid);

    @GET("disposal-lists")
    Call<List<BorrowList>> getDisposalLists(@Query("applied_by.id") int userid);

    @GET("stock-take-lists")
    Call<List<StockTakeList>> getStockTakeLists(@Query("user_groups.id") int user_groupsid);

    @GET("stock-take-lists/{id}")
    Call<StockTakeList> getStockTakeList(@Path("id") int id);

    @GET("stock-take-asset-item-remarks/{id}")
    Call<StockTakeListItemRemark> getRemark(@Path("id") int id);

    @PUT("stock-take-asset-item-remarks/{id}")
    Call<StockTakeListItemRemark> updateRemark(@Path("id") int id, @Body StockTakeListItemRemarkRequest stockTakeListItemRemark);

    @POST("stock-take-asset-item-remarks/")
    Call<StockTakeListItemRemark> createMark(@Body StockTakeListItemRemarkRequest stockTakeListItemRemark);

    @POST("stock-take-list-items/")
    Call<StockTakeListItem> createStockTakeListItem(@Body StockTakeListItemRequest stockTakeListItemRequest);

    @PUT("assets/{id}")
    Call<Asset> updateAsset(@Path("id") int id, @Body Asset asset);


    @POST("stock-take-asset-item-remark-photos")
    Call<StockTakeListItemRemarkRequest> uploadRemarkPhoto(@Body UploadPhotoRequest stockTakeListItemRemarkRequest);

    @DELETE("stock-take-asset-item-remark-photos/{id}")
    Call<Photo> deletePhoto(@Path("id") int id);

    //addremark
    //@Headers("Content-Type: application/json")
    //@PUT("/assets/{id}")
    //Call<Asset> modifyAsset(@Path("id") String id, @Body Asset body);
}
