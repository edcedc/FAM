package com.spit.fam.WebService;

import com.spit.fam.Entity.SPEntityP2.AssetListFromBorrowList;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.SPEntityP2.BriefBorrowedList;
import com.spit.fam.Response.APIResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SPGetWebServiceP2 {
    @GET("MobileWebService.asmx/search")
    Call<List<BriefAsset>> search(@Query("companyID") String companyID, @Query("loginID") String loginID, @Query("assetNo") String assetNo, @Query("name") String name, @Query("type") String type, @Query("brand") String brand, @Query("model") String model, @Query("firstcat") String firstcat, @Query("lastcat") String lastcat, @Query("firstlocation") String firstlocation, @Query("lastlocation") String lastlocation);

    @GET("MobileWebService.asmx/searchnoepc")
    Call<List<BriefAsset>> searchnoepc(@Query("companyID") String companyID);//

    @GET("MobileWebService.asmx/assetDetail")
    Call<AssetsDetail> assetDetail(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("assetno") String assetno);//

    @GET("MobileWebService.asmx/changeEpc")
    Call<APIResponse> changeEpc(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("assetno") String assetno, @Query("epc") String epc);//

    @GET("MobileWebService.asmx/setEpc")
    Call<APIResponse> setEpc(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("assetno") String assetno, @Query("epc") String epc);//

    @GET("MobileWebService.asmx/borrowList")
    Call<BriefBorrowedList> borrowList(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("type") int type);//

    @GET("MobileWebService.asmx/borrowListAssets")
    Call<AssetListFromBorrowList> borrowListAssets(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("borrowListId") String blid);//

    @GET("MobileWebService.asmx/getBriefAssetInfo")
    Call<BriefAsset> getBriefAssetInfo(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("epc") int epc);//

    @GET("MobileWebService.asmx/borrowAssets")
    Call<APIResponse> borrowAssets(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("assetno") List<String> assetno);//

    @GET("MobileWebService.asmx/returnList")
    Call<List<BriefAsset>> returnList(@Query("companyID") String companyID, @Query("loginID") String userid);//

    @GET("MobileWebService.asmx/return")
    Call<APIResponse> returnAsset(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("locationID") String locationID, @Query("assetno") List<String> assetno);//

    @GET("MobileWebService.asmx/disposalList")
    Call<BriefBorrowedList> disposalList(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("type") int type);//

    @GET("MobileWebService.asmx/disposalListAssets")
    Call<AssetListFromBorrowList> disposalListAssets(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("disposalListId") String disposalListId);//

    @GET("MobileWebService.asmx/disposal")
    Call<APIResponse> disposal(@Query("companyID") String companyID, @Query("loginID") String userid, @Query("assetno") List<String> assetno);//

}
