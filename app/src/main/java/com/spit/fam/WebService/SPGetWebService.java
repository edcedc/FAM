package com.spit.fam.WebService;

import com.spit.fam.Entity.ImageReturn;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.SPEntityP2.BriefBorrowedList;
import com.spit.fam.Entity.SPEntityP3.ReturnAsset;
import com.spit.fam.Entity.SPEntityP2.StockTakeListData;
import com.spit.fam.Entity.SPEntityP3.BorrowDetailResponse;
import com.spit.fam.Entity.SPEntityP3.BorrowListItem;
import com.spit.fam.Entity.SPEntityP3.DisposalDetailResponse;
import com.spit.fam.Entity.SPEntityP3.DisposalListItem;
import com.spit.fam.Entity.SPEntityP3.SearchNoEpcItem;
import com.spit.fam.Entity.SPEntityP3.StocktakeList;
import com.spit.fam.Entity.SpEntity.StockTakeDetail;
import com.spit.fam.Entity.SpEntity.StockTakeNoList;
import com.spit.fam.Entity.StockTakeList;
import com.spit.fam.Entity.Tray;
import com.spit.fam.Event.BorrowListAssets;
import com.spit.fam.Request.SPUpdateWaitingListRequest;
import com.spit.fam.Response.APIResponse;
import com.spit.fam.Response.LevelData;
import com.spit.fam.Response.ListingResponse;
import com.spit.fam.Response.UserListResponse;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface SPGetWebService {
    //http://47.52.129.17/AMSWebService
    //AMSWebService_Template/MobileWebService.asmx
    //GET /MobileWebService.asmx/UploadStockTake?companyID=string&strJson=string HTTP/1.1
    @GET("MobileWebService.asmx/GetCheckLogin")
    Call<List<APIResponse>> login(@Query("companyID") String companyID, @Query("loginID") String loginID, @Query("userPwd") String userPwd);

    @GET("MobileWebService.asmx/GetStockTakeNoList")
    Call<StockTakeNoList> stockTakeList(@Query("companyID") String companyID, @Query("loginID") String loginID);

    @GET("MobileWebService.asmx/GetStockTakeDetails")
    Call<StockTakeDetail> stockTakeListDetail(@Query("companyID") String companyID, @Query("loginID") String loginID, @Query("orderNo") String orderNo);

    @FormUrlEncoded
    @POST("MobileWebService.asmx/UploadStockTake")
    Call<List<APIResponse>> UploadStockTake(@Field("companyID") String companyID, @Field(value = "strJson", encoded = false) String data);

    @GET("MobileWebService.asmx/GetAssetInfo")
    Call<StockTakeDetail> GetAssetInfo(@Query("companyID") String companyID, @Query("codeNo") String assetId);

    //GET FileToByte?companyID=string&str=string&Suffix=string&iCode=string&fileLoc=string&loginID=string&passCode=string HTTP/1.1
    @FormUrlEncoded
    @POST("MobileWebService.asmx/FileToByte")
    Call<ArrayList<ImageReturn>> uploadFileToByte(@Field("companyID") String companyID, @Field("str") String str, @Field("Suffix") String Suffix, @Field("iCode") String iCode, @Field("fileLoc") int fileLoc, @Field("loginID") String loginID, @Field("passCode") String passCode);

    @GET("MobileWebService.asmx/search")
    Call<List<BriefAsset>> search(@Query("companyid") String companyID, @Query("userid") String loginID, @Query("assetno") String assetNo, @Query("name") String name, @Query("type") String type, @Query("brand") String brand, @Query("model") String model, @Query("firstcat") String firstcat, @Query("lastcat") String lastcat, @Query("firstloc") String firstlocation, @Query("lastloc") String lastlocation);

    @GET("MobileWebService.asmx/assetsList")
    Call<List<BriefAsset>> assetsList(@Query("companyID") String companyID, @Query("userid") String userID);

    @GET("MobileWebService.asmx/userList")
    Call<UserListResponse> userList(@Query("companyID") String companyID, @Query("userid") String userID, @Query("lastcalldate") String thiscalldate);

    @GET("MobileWebService.asmx/searchnoepc")
    Call<List<BriefAsset>> searchnoepc(@Query("companyID") String companyID);//

    @GET("MobileWebService.asmx/searchnoepc")
    Call<List<SearchNoEpcItem>> newSearchnoepc(@Query("companyID") String companyID);//

    @GET("MobileWebService.asmx/assetsDetail")
    Call<List<AssetsDetail>> assetDetail(@Query("companyid") String companyID, @Query("userid") String userid, @Query("assetno") String assetno);//

    @GET("MobileWebService.asmx/assetsDetail")
    Call<JsonElement> newAssetDetail(@Query("companyid") String companyID, @Query("userid") String userid, @Query("assetno") String assetno, @Query("lastcalldate") String lastcalldate);//

    @Streaming
    @GET("MobileWebService.asmx/assetsDetail")
    Call<JsonElement> assetDetail(@Query("companyid") String companyID, @Query("userid") String userid, @Query("assetno") String assetno, @Query("lastcalldate") String lastcalldate);//

    @GET("MobileWebService.asmx/changeEpc")
    Call<List<APIResponse>> changeEpc(@Query("companyid") String companyID, @Query("userid") String userid, @Query("assetno") String assetno, @Query("epc") String epc);//

    @GET("MobileWebService.asmx/setEpc")
    Call<List<APIResponse>> setEpc(@Query("companyid") String companyID, @Query("userid") String userid, @Query("assetno") String assetno, @Query("epc") String epc);//

    @GET("MobileWebService.asmx/stockTakeList")
    Call<List<StockTakeList>> newStockTakeList(@Query("companyid") String companyID, @Query("userid") String userid);//

    @GET("MobileWebService.asmx/stockTakeList")
    Call<List<StocktakeList>> renewStockTakeList(@Query("companyid") String companyID, @Query("userid") String userid);//

    @GET("MobileWebService.asmx/stockTakeListAsset")
    Call<BorrowListAssets> stockTakeListAsset(@Query("companyid") String companyID, @Query("userid") String userid, @Query("orderno") String orderno );//

    @GET("MobileWebService.asmx/stockTakeListAsset")
    Call<StockTakeListData> stockTakeListAsset2(@Query("companyid") String companyID, @Query("userid") String userid, @Query("orderno") String orderno );//


    @GET("MobileWebService.asmx/borrowList")
    Call<List<BriefBorrowedList>> borrowList(@Query("companyid") String companyID, @Query("userid") String userid, @Query("type") int type);//

    @GET("MobileWebService.asmx/borrowList")
    Call<List<BorrowListItem>> newBorrowList(@Query("companyid") String companyID, @Query("userid") String userid, @Query("type") int type);//

    @GET("MobileWebService.asmx/borrowListAssets")
    Call<BorrowListAssets> borrowListAssets(@Query("companyid") String companyID, @Query("userid") String userid, @Query("borrowno") String blid);//

    @GET("MobileWebService.asmx/borrowListAssets")
    Call<BorrowDetailResponse> newBorrowListAssets(@Query("companyid") String companyID, @Query("userid") String userid, @Query("borrowno") String blid);//

    @GET("MobileWebService.asmx/getBriefAssetInfo")
    Call<BriefAsset> getBriefAssetInfo(@Query("companyid") String companyID, @Query("userid") String userid, @Query("epc") String epc);//

    @GET("MobileWebService.asmx/borrowAssets")
    Call<APIResponse> borrowAssets(@Query("companyid") String companyID, @Query("userid") String userid, @Query("borrowList") String assetno, @Query("borrowno") String borrowno);//

    @GET("MobileWebService.asmx/disposalAssets")
    Call<APIResponse> disposalAssets(@Query("companyid") String companyID, @Query("userid") String userid, @Query("disposalList") String disposalList, @Query("disposalNo") String disposalNo);//

    @GET("MobileWebService.asmx/borrowAssets")
    Call<APIResponse> borrowAssets(@Body SPUpdateWaitingListRequest body);//

    @GET("MobileWebService.asmx/returnList")
    Call<List<BriefAsset>> returnList(@Query("companyid") String companyID, @Query("userid") String userid);//

    @GET("MobileWebService.asmx/returnList")
    Call<List<ReturnAsset>> newReturnList(@Query("companyid") String companyID, @Query("userid") String userid);//

    @GET("MobileWebService.asmx/returnAssets")
    Call<APIResponse> returnAsset(@Query("companyid") String companyID, @Query("userid") String userid, @Query("firstlocation") String firstLocation, @Query("lastlocation") String lastLocation, @Query("returnList") String returnList);//

    @GET("MobileWebService.asmx/disposalList")
    Call<List<BriefBorrowedList>> disposalList(@Query("companyid") String companyID, @Query("userid") String userid, @Query("type") int type);//

    @GET("MobileWebService.asmx/disposalList")
    Call<List<DisposalListItem>> newDisposalList(@Query("companyid") String companyID, @Query("userid") String userid, @Query("type") int type);//

    @GET("MobileWebService.asmx/disposalListAssets")
    Call<BorrowListAssets> disposalListAssets(@Query("companyID") String companyID, @Query("userid") String userid, @Query("disposalNo") String disposalListId);//

    @GET("MobileWebService.asmx/disposalListAssets")
    Call<DisposalDetailResponse> newDisposalListAssets(@Query("companyID") String companyID, @Query("userid") String userid, @Query("disposalNo") String disposalListId);//

    @GET("MobileWebService.asmx/disposal")
    Call<APIResponse> disposal(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("assetno") List<String> assetno);//

    @GET("MobileWebService.asmx/listingLevel")
    Call<ListingResponse> listingLevel(@Query("companyID") String companyID);

    @GET("MobileWebService.asmx/listing")
    Call<List<LevelData>> listing(@Query("companyID") String companyID, @Query("fatherrono") String fatherrono, @Query("type") String type);

    @GET("MobileWebService.asmx/trayList")
    Call<List<Tray>> trayList(@Query("companyID") String companyID, @Query("userid") String userID);

    @GET("MobileWebService.asmx/deposit")
    Call<APIResponse> deposit(@Query("userId") String userid, @Query("companyID") String companyID , @Query("borrowedCurrentLocation") String trayId, @Query("data") String data);

    @GET("MobileWebService.asmx/receive")
    Call<APIResponse> receive(@Query("userId") String userid, @Query("companyID") String companyID, @Query("data") String data);

    @GET("MobileWebService.asmx/addToken")
    Call<List<APIResponse>> addToken(@Query("companyID") String companyID, @Query("loginID") String loginID, @Query("token") String token);

    @GET("MobileWebService.asmx/removeToken")
    Call<List<APIResponse>> removeToken(@Query("companyID") String companyID, @Query("loginID") String loginID, @Query("token") String token);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);

/*
    @POST("borrowAssets.php")
    Call<APIResponse> borrowAsset(@Body UpdateWaitingListRequest body);

    @POST("returnBorrowedAssets.php")
    Call<APIResponse> returnAsset(@Body ReturnBorrowedAssetRequest body);
*/
}
