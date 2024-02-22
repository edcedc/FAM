package com.spit.fam.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.StockTakeList;
import com.spit.fam.Event.BorrowListAssets;
import com.spit.fam.InternalStorage;
import com.spit.fam.SystemFragment.Adapter.SearchListAdapter;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler extends SQLiteOpenHelper {
    static String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    static String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = companyId + serverId + "AMS";
    private static final String TABLE_ASSETS_DETAIL = companyId + serverId + "assetsDetail";

    private static final String KEY_ASSET_NO = "assetNo";
    private static final String KEY_NAME = "name";
    private static final String KEY_STATUS_ID = "statusid";
    private static final String KEY_STATUS_NANE = "statusName";
    private static final String KEY_BRAND = "brand";
    private static final String KEY_MODEL = "model";
    private static final String KEY_SERIAL_NO = "serialno";
    private static final String KEY_UNIT = "unit";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_LAST_STOCK_DATE = "lastStockDate";
    private static final String KEY_CREATED_BY_ID = "createdById";
    private static final String KEY_CREATED_BY_NAME = "createdByName";
    private static final String KEY_CREATED_DATE = "createdDate";
    private static final String KEY_CREATED_PURCHASE_DATE = "purchaseDate";
    private static final String KEY_INVOICE_DATE = "invoiceDate";
    private static final String KEY_INVOIVE_NO = "invoiceNo";
    private static final String KEY_FUNDING_SOURCE_ID = "fundingSourceId";
    private static final String KEY_FUNDING_SOURCE_NAME = "fundingSourceName";
    private static final String KEY_SUPPLIER = "supplier";
    private static final String KEY_MAINTENANCE_DATE = "maintenanceDate";
    private static final String KEY_COST = "cost";
    private static final String KEY_PRACTICAL_VALUE = "praticalValue";
    private static final String KEY_ESTIMATED_LIFE_TIME = "estimatedLifeTime";
    private static final String KEY_TYPE_OF_TAG = "typeOfTag";
    private static final String KEY_BARCODE = "barcode";
    private static final String KEY_CREATED_POSSESSOR = "possessor";
    private static final String KEY_EPC = "epc";
    private static final String KEY_NEW_EPC = "newEpc";
    private static final String KEY_CERT_TYPE = "certType";
    private static final String KEY_CERT_URL = "certUrl";
    private static final String KEY_CERT_STATUS = "cerstatus";
    private static final String KEY_START_DATE = "startdate";
    private static final String KEY_END_DATE = "enddate";

    //exhibitsource
    //lastassetno
    private static final String KEY_SOURCE = "source";
    private static final String KEY_WITNESS = "witness";
    private static final String KEY_PROSECUTION_NO = "prosecution_no";

    private static final String TABLE_STOCK_TAKE= companyId + serverId + "stockTake";
    private static final String KEY_STOCK_TAKE_ORDER_NO = "orderNo";
    private static final String KEY_STOCK_TAKE_ORDER_NAME = "orderName";
    private static final String KEY_STOCK_TAKE_ID = "id";

    private static final String TABLE_STOCK_TAKE_DETAIL = companyId + serverId + "stockTakeDetail";
    private static final String KEY_STOCK_TAKE_DETAIL_ID = "sid";
    private static final String KEY_STOCK_TAKE_DETAIL_ASSET_NO = "assetNo";

    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ASSETS_DETAIL + "("
                + KEY_ASSET_NO + " TEXT PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_STATUS_ID + " TEXT,"
                + KEY_STATUS_NANE + " TEXT,"
                + KEY_BRAND + " TEXT,"
                + KEY_MODEL + " TEXT,"
                + KEY_SERIAL_NO + " TEXT,"
                + KEY_UNIT + " TEXT,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LAST_STOCK_DATE + " TEXT,"
                + KEY_CREATED_BY_ID + " TEXT,"
                + KEY_CREATED_BY_NAME + " TEXT,"
                + KEY_CREATED_DATE + " TEXT,"
                + KEY_CREATED_PURCHASE_DATE + " TEXT,"
                + KEY_CREATED_POSSESSOR + " TEXT,"
                + KEY_EPC + " TEXT,"
                + KEY_NEW_EPC + " TEXT,"
                + KEY_BARCODE + " TEXT,"
                + KEY_INVOICE_DATE + " TEXT,"
                + KEY_INVOIVE_NO + " TEXT,"
                + KEY_FUNDING_SOURCE_ID + " TEXT,"
                + KEY_FUNDING_SOURCE_NAME + " TEXT,"
                + KEY_SUPPLIER + " TEXT,"
                + KEY_MAINTENANCE_DATE + " TEXT,"
                + KEY_COST + " TEXT,"
                + KEY_PRACTICAL_VALUE + " TEXT,"
                + KEY_ESTIMATED_LIFE_TIME + " TEXT,"
                + KEY_TYPE_OF_TAG + " TEXT,"
                + KEY_CERT_TYPE + " TEXT,"
                + KEY_CERT_URL + " TEXT,"
                + KEY_CERT_STATUS + " TEXT,"
                + KEY_START_DATE + " TEXT,"
                + KEY_END_DATE + " TEXT,"
                + KEY_SOURCE + " TEXT,"
                + KEY_WITNESS + " TEXT,"
                + KEY_PROSECUTION_NO + " TEXT"
                + ")";

        String CREATE_STOCK_TAKE_TABLE = "CREATE TABLE " + TABLE_STOCK_TAKE + "("
                + KEY_STOCK_TAKE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_STOCK_TAKE_ORDER_NAME + " TEXT UNIQUE,"
                + KEY_STOCK_TAKE_ORDER_NO + " TEXT"
                + ")";;

        String CREATE_STOCK_TAKE_DETAIL_TABLE = "CREATE TABLE " + TABLE_STOCK_TAKE_DETAIL + "("
                + KEY_STOCK_TAKE_DETAIL_ASSET_NO + " TEXT PRIMARY KEY,"
                + KEY_STOCK_TAKE_DETAIL_ID + " INTEGER"
                //+ "PRIMARY KEY (" + KEY_STOCK_TAKE_DETAIL_ASSET_NO + ", " + KEY_STOCK_TAKE_DETAIL_ID + ")"
                + ")";;

        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_STOCK_TAKE_TABLE);
        db.execSQL(CREATE_STOCK_TAKE_DETAIL_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSETS_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK_TAKE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK_TAKE_DETAIL);

        // Create tables again
        onCreate(db);
    }

    public Long addStockTake(StockTakeList briefBorrowedList) {
        Long rowId = new Long(-1);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_STOCK_TAKE_ORDER_NAME, briefBorrowedList.getName());
        values.put(KEY_STOCK_TAKE_ORDER_NO, briefBorrowedList.getStocktakeno());

        int id = (int) db.insertWithOnConflict(TABLE_STOCK_TAKE, "assetNo", values, SQLiteDatabase.CONFLICT_IGNORE);

        Log.i("addStockTake", "addStockTake " + id);

        if (id == -1) {
            //db.update(TABLE_ASSETS_DETAIL, values, "assetNo=?", new String[]{contact.getAssetNo()});  // number 1 is the _id here, update to variable for your code
        } else {
            //rowId =db.insert(TABLE_ASSETS_DETAIL, null, values);
        }

        db.close(); // Closing database connection

        return rowId;
    }

    public void addStockTakeDetailBy(BorrowListAssets borrowListAssets) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_STOCK_TAKE + " WHERE " + KEY_STOCK_TAKE_ORDER_NO + " = '" + borrowListAssets.getStocktakeno() + "'", null);

        int column1 = -1;

        if (c.moveToFirst()){
            do {
                // Passing values
                column1 = c.getInt(0);
                // Do something Here with values
            } while(c.moveToNext());
        }
        c.close();

        Log.i("column1" , "column1 " + column1 + " " + borrowListAssets.getData().size());

        if(column1 >= 0) {
            for (int i = 0; i < borrowListAssets.getData().size(); i++) {
                addStockTakeDetail(column1, borrowListAssets.getData().get(i).getAssetNo());
            }
        }

        db.close();
    }

    public Long addStockTakeDetail(int sid, String assetNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        Long rowId = new Long(-1);

        values.put(KEY_STOCK_TAKE_DETAIL_ID, sid);
        values.put(KEY_STOCK_TAKE_DETAIL_ASSET_NO, assetNo);

        int id = (int) db.insertWithOnConflict(TABLE_STOCK_TAKE_DETAIL, "assetNo", values, SQLiteDatabase.CONFLICT_IGNORE);

        Log.i("addStockTakeDetail", "addStockTakeDetail " + id);

        if (id == -1) {
            //db.update(TABLE_ASSETS_DETAIL, values, "assetNo=?", new String[]{contact.getAssetNo()});  // number 1 is the _id here, update to variable for your code
        } else {
            //rowId = db.insert(TABLE_STOCK_TAKE_DETAIL, null, values);
        }

        db.close(); // Closing database connection

        return rowId;
    }

    public void deleteAssets() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_ASSETS_DETAIL);
    }

    public void addAssetsDetail(AssetsDetail contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_ASSET_NO, contact.getAssetNo()); //1
        values.put(KEY_NAME, contact.getName()); //2
        values.put(KEY_STATUS_ID, contact.getStatusid()); //3
        values.put(KEY_STATUS_NANE, contact.getStatusname());//4
        values.put(KEY_BRAND, contact.getBrand());//5
        values.put(KEY_MODEL, contact.getModel());//6
        values.put(KEY_SERIAL_NO, contact.getSerialno());//7
        values.put(KEY_UNIT, contact.getUnit()); //8
        values.put(KEY_CATEGORY, contact.getCategory());//8
        values.put(KEY_LOCATION, contact.getLocation());//9
        values.put(KEY_LAST_STOCK_DATE, contact.getLastStockDate());//10
        values.put(KEY_CREATED_BY_ID, contact.getCreatedById());//11
        values.put(KEY_CREATED_BY_NAME, contact.getCreatedByName());//12
        values.put(KEY_CREATED_DATE, contact.getCreatedDate());//13
        values.put(KEY_CREATED_PURCHASE_DATE, contact.getPurchaseDate());//14
        values.put(KEY_CREATED_POSSESSOR, contact.getPossessor());//15
        values.put(KEY_NEW_EPC, contact.getNewEpc());//16
        values.put(KEY_EPC, contact.getEpc());//17

        values.put(KEY_BARCODE, contact.getBarcode());//18
        values.put(KEY_INVOICE_DATE, contact.getInvoiceDate());//19
        values.put(KEY_INVOIVE_NO, contact.getInvoiceNo());//20
        values.put(KEY_FUNDING_SOURCE_ID, contact.getFundingSourceid());//21
        values.put(KEY_FUNDING_SOURCE_NAME, contact.getFundingSourcename());//21
        values.put(KEY_SUPPLIER, contact.getSupplier());//22
        values.put(KEY_MAINTENANCE_DATE, contact.getMaintenanceDate());//23
        values.put(KEY_COST, contact.getCost());//24
        values.put(KEY_PRACTICAL_VALUE, contact.getPraticalValue());//25
        values.put(KEY_ESTIMATED_LIFE_TIME, contact.getEstimatedLifetime());//26
        values.put(KEY_TYPE_OF_TAG, contact.getTypeOfTag());//27
        values.put(KEY_CERT_TYPE, contact.getCertType());//28
        values.put(KEY_CERT_URL, contact.getCertUrl());//29
        values.put(KEY_CERT_STATUS, contact.getCerstatus());//30
        values.put(KEY_START_DATE, contact.getStartdate());//31
        values.put(KEY_END_DATE, contact.getEnddate());//32
        values.put(KEY_SOURCE, contact.getExhibitsource());//33
        values.put(KEY_WITNESS, contact.getExhibitwitness());//34
        values.put(KEY_PROSECUTION_NO, contact.getLastassetno());//35

;;        int id = (int) db.insertWithOnConflict(TABLE_ASSETS_DETAIL, "assetNo", values, SQLiteDatabase.CONFLICT_IGNORE);

        Log.i("update", "update " + id);

        if (id == -1) {
            db.update(TABLE_ASSETS_DETAIL, values, "assetNo=?", new String[]{contact.getAssetNo()});  // number 1 is the _id here, update to variable for your code
        } else {
            //db.insert(TABLE_ASSETS_DETAIL, null, values);
        }

        db.close(); // Closing database connection
    }


    // code to get all contacts in a list view
    public List<AssetsDetail> getAllAssetsDetail() {
        List<AssetsDetail> assetsDetails = new ArrayList<AssetsDetail>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();
                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setBrand((cursor.getString(3)));
                assetsDetail.setModel((cursor.getString(4)));
                assetsDetail.setSerialno((cursor.getString(5)));
                assetsDetail.setUnit((cursor.getString(6)));
                assetsDetail.setCategory((cursor.getString(7)));
                assetsDetail.setLocation((cursor.getString(8)));
                assetsDetail.setLastStockDate((cursor.getString(9)));
                assetsDetail.setCreatedById((cursor.getString(10)));
                assetsDetail.setCreatedByName((cursor.getString(11)));
                assetsDetail.setCreatedDate((cursor.getString(12)));
                assetsDetail.setPurchaseDate((cursor.getString(13)));
                assetsDetail.setPossessor((cursor.getString(14)));
                assetsDetail.setNewEpc((cursor.getString(16)));
                assetsDetail.setEpc((cursor.getString(15)));
                //TODO

                // Adding contact to list
                assetsDetails.add(assetsDetail);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }


    public Asset convertBriefAssetToAsset(BriefAsset briefAsset) {
        Asset asset = new Asset();
        asset.setId(briefAsset.getId() + "");

        asset.setName(briefAsset.getName());
        asset.setAssetno(briefAsset.getAssetNo());
        asset.setBrand(briefAsset.getBrand());
        asset.setModel(briefAsset.getModel());
        asset.setEPC(briefAsset.getEpc());
        asset.setFound(briefAsset.getFound());

        Status status = new Status();

        if(briefAsset.getOverdue() != null && briefAsset.getOverdue()) {
            status.id = 9999;
        } else if(briefAsset.getStatusid() != null){
            try {
                status.id = Integer.parseInt(briefAsset.getStatusid());
            } catch (Exception e) {
            }
            //status.id = -1;
        }
        asset.setStatus(status);

        if(briefAsset.getBorrowed() != null)
            asset.setFound(briefAsset.getBorrowed());
        else if(briefAsset.getDisposed() != null)
            asset.setFound(briefAsset.getDisposed());

        asset.setReturndate("");

        ArrayList<Category> categoryArrayList = new ArrayList<>();

        if(briefAsset.getCategorys() != null) {
            for (int i = 0; i < briefAsset.getCategorys().size(); i++) {
                Category category = new Category();
                category.setName(briefAsset.getCategorys().get(i));
                categoryArrayList.add(category);
            }
        }
        asset.setCategories(categoryArrayList);


        ArrayList<Location> locationArrayList = new ArrayList<>();

        if(briefAsset.getLocations() != null) {
            for (int i = 0; i < briefAsset.getLocations().size(); i++) {
                Location location = new Location();
                location.setName(briefAsset.getLocations().get(i));
                locationArrayList.add(location);
            }
        }
        asset.setLocations(locationArrayList);
        return asset;
    }

    public List<Asset> getAssetWithoutEPC() {
        List<Asset> assetsDetails = new ArrayList<Asset>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE epc is null OR TRIM(epc,\" \") == \"\"" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();
                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setStatusname((cursor.getString(3)));

                assetsDetail.setBrand((cursor.getString(4)));
                assetsDetail.setModel((cursor.getString(5)));
                assetsDetail.setSerialno((cursor.getString(6)));
                assetsDetail.setUnit((cursor.getString(7)));
                assetsDetail.setCategory((cursor.getString(8)));
                assetsDetail.setLocation((cursor.getString(9)));
                assetsDetail.setLastStockDate((cursor.getString(10)));
                assetsDetail.setCreatedById((cursor.getString(11)));
                assetsDetail.setCreatedByName((cursor.getString(12)));
                assetsDetail.setCreatedDate((cursor.getString(13)));
                assetsDetail.setPurchaseDate((cursor.getString(14)));
                assetsDetail.setPossessor((cursor.getString(15)));
                assetsDetail.setNewEpc((cursor.getString(17)));
                assetsDetail.setEpc((cursor.getString(16)));


                assetsDetail.setBarcode((cursor.getString(18)));
                assetsDetail.setInvoiceDate((cursor.getString(19)));
                assetsDetail.setInvoiceNo((cursor.getString(20)));
                assetsDetail.setFundingSourceid((cursor.getString(21)));
                assetsDetail.setFundingSourcename((cursor.getString(22)));
                assetsDetail.setSupplier((cursor.getString(23)));
                assetsDetail.setMaintenanceDate((cursor.getString(24)));
                assetsDetail.setCost((cursor.getString(25)));
                assetsDetail.setPraticalValue((cursor.getString(26)));
                assetsDetail.setEstimatedLifetime((cursor.getString(27)));
                assetsDetail.setTypeOfTag((cursor.getString(28)));
                assetsDetail.setCertType((cursor.getString(29)));
                assetsDetail.setCertUrl((cursor.getString(30)));
                assetsDetail.setCerstatus((cursor.getString(31)));
                assetsDetail.setStartdate((cursor.getString(32)));
                assetsDetail.setEnddate((cursor.getString(33)));


                //TODO

                Asset asset = new Asset();
                asset.setAssetno(assetsDetail.getAssetNo());
                asset.setName(assetsDetail.getName());
                asset.setBrand(assetsDetail.getBrand());
                asset.setModel(assetsDetail.getModel());
                asset.setEPC(assetsDetail.getEpc());

                ArrayList<String> categoryList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getCategory().split("->").length; i++){
                    categoryList.add(assetsDetail.getCategory().split("->")[i]);
                }
                ArrayList<Category> categoryArrayList = new ArrayList<>();

                if(categoryList != null) {
                    for (int i = 0; i < categoryList.size(); i++) {
                        Category category = new Category();
                        category.setName(categoryList.get(i));
                        categoryArrayList.add(category);
                    }
                }
                asset.setCategories(categoryArrayList);

                ArrayList<Location> locationArrayList = new ArrayList<>();

                ArrayList<String> locationList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getLocation().split("->").length; i++){
                    locationList.add(assetsDetail.getLocation().split("->")[i]);
                }


                if(locationList!= null) {
                    for (int i = 0; i < locationList.size(); i++) {
                        Location location = new Location();
                        location.setName(locationList.get(i));
                        locationArrayList.add(location);
                    }
                }
                asset.setLocations(locationArrayList);
                asset.setEPC(assetsDetail.getEpc());

                // Adding contact to list
                assetsDetails.add(asset);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }



    public int searchAssetWithEPCCount(String assetNo, String name, String startLoc, String endLoc, String brand, String model, String startCat, String endCat, String offset) {
        String countQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE epc is not null AND TRIM(epc,\" \") != \"\" " +
                (true ? ("AND  ((assetNo LIKE '%" + assetNo + "%' OR length('" + assetNo + "') == 0)  AND (name LIKE '%" + name +"%' OR length('" + name + "') == 0) AND (brand LIKE '%" + brand + "%' OR LENGTH('" + brand +"') ==0) AND (model LIKE  '%" + model + "%' OR LENGTH('" + model + "') == 0) AND (location LIKE '" + startLoc + "%' OR length('" + startLoc + "') == 0) AND (location LIKE '%" + endLoc + "%' OR length('" + endLoc + "') == 0) AND (category LIKE '" + startCat + "%' OR length('" + startCat + "') == 0) AND (category LIKE '%" + endCat + "%' OR length('" + endCat + "') == 0) ) ") : "");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }
    public List<AssetsDetail> searchAssetsDetail(String assetNo) {
        List<AssetsDetail> assetsDetails = new ArrayList<AssetsDetail>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE  " +
                //      (false ? ("AND  (assetNo LIKE '%" + assetNo + "%' OR name LIKE '%" + name +"%' OR brand LIKE '%" + brand + "%' OR model LIKE  '%" + model + "%')") : "")

                /*OR location LIKE '%" + startLoc + "' OR location LIKE '"+ endLoc + "%' OR category LIKE '%" + startCat + "' OR category LIKE '" + endCat + "%'*/
                (true ? ("  ((barcode LIKE '%"  + "%' OR length('"  + "') == 0) AND (assetNo LIKE '" + assetNo + "' OR length('" + assetNo + "') == 0)  AND (name LIKE '%"  +"%' OR length('"  + "') == 0) AND (brand LIKE '%"  + "%' OR LENGTH('"  +"') ==0) AND (model LIKE  '%"  + "%' OR LENGTH('"  + "') == 0) AND (location LIKE '"  + "%' OR length('"  + "') == 0) AND (location LIKE '%"  + "%' OR length('"  + "') == 0) AND (category LIKE '"  + "%' OR length('"  + "') == 0) AND (category LIKE '%"  + "%' OR length('"  + "') == 0) ) ") : "");


        Log.i("query", "query " + selectQuery);

        //(filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%' OR model LIKE '%" + filter + "%' OR serialNo LIKE '%" + filter + "%' OR unit LIKE  '%" + filter + "%')") : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();
                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setStatusname((cursor.getString(3)));

                assetsDetail.setBrand((cursor.getString(4)));
                assetsDetail.setModel((cursor.getString(5)));
                assetsDetail.setSerialno((cursor.getString(6)));
                assetsDetail.setUnit((cursor.getString(7)));
                assetsDetail.setCategory((cursor.getString(8)));
                assetsDetail.setLocation((cursor.getString(9)));
                assetsDetail.setLastStockDate((cursor.getString(10)));
                assetsDetail.setCreatedById((cursor.getString(11)));
                assetsDetail.setCreatedByName((cursor.getString(12)));
                assetsDetail.setCreatedDate((cursor.getString(13)));
                assetsDetail.setPurchaseDate((cursor.getString(14)));
                assetsDetail.setPossessor((cursor.getString(15)));
                assetsDetail.setNewEpc((cursor.getString(17)));
                assetsDetail.setEpc((cursor.getString(16)));


                assetsDetail.setBarcode((cursor.getString(18)));
                assetsDetail.setInvoiceDate((cursor.getString(19)));
                assetsDetail.setInvoiceNo((cursor.getString(20)));
                assetsDetail.setFundingSourceid((cursor.getString(21)));
                assetsDetail.setFundingSourcename((cursor.getString(22)));
                assetsDetail.setSupplier((cursor.getString(23)));
                assetsDetail.setMaintenanceDate((cursor.getString(24)));
                assetsDetail.setCost((cursor.getString(25)));
                assetsDetail.setPraticalValue((cursor.getString(26)));
                assetsDetail.setEstimatedLifetime((cursor.getString(27)));
                assetsDetail.setTypeOfTag((cursor.getString(28)));
                assetsDetail.setCertType((cursor.getString(29)));
                assetsDetail.setCertUrl((cursor.getString(30)));
                assetsDetail.setCerstatus((cursor.getString(31)));
                assetsDetail.setStartdate((cursor.getString(32)));
                assetsDetail.setEnddate((cursor.getString(33)));

                assetsDetail.setExhibitsource(cursor.getString(34));
                assetsDetail.setExhibitwitness(cursor.getString(35));
                assetsDetail.setLastassetno(cursor.getString(36));
                //assetsDetail.setExhibitwitness(cursor.getString(37));
                //assetsDetail.setLastassetno(cursor.getString(38));



                //TODO
                // Adding contact to list
                assetsDetails.add(assetsDetail);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }

    public List<AssetsDetail> searchAssetsDetail(String assetNo, String name, String startLoc, String endLoc, String brand, String model, String startCat, String endCat, String barcode) {
        List<AssetsDetail> assetsDetails = new ArrayList<AssetsDetail>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE  " +
                //      (false ? ("AND  (assetNo LIKE '%" + assetNo + "%' OR name LIKE '%" + name +"%' OR brand LIKE '%" + brand + "%' OR model LIKE  '%" + model + "%')") : "")

                /*OR location LIKE '%" + startLoc + "' OR location LIKE '"+ endLoc + "%' OR category LIKE '%" + startCat + "' OR category LIKE '" + endCat + "%'*/
                (true ? ("  ((barcode LIKE '%" + barcode + "%' OR length('" + barcode + "') == 0) AND (assetNo LIKE '%" + assetNo + "%' OR length('" + assetNo + "') == 0)  AND (name LIKE '%" + name +"%' OR length('" + name + "') == 0) AND (brand LIKE '%" + brand + "%' OR LENGTH('" + brand +"') ==0) AND (model LIKE  '%" + model + "%' OR LENGTH('" + model + "') == 0) AND (location LIKE '" + startLoc + "%' OR length('" + startLoc + "') == 0) AND (location LIKE '%" + endLoc + "%' OR length('" + endLoc + "') == 0) AND (category LIKE '" + startCat + "%' OR length('" + startCat + "') == 0) AND (category LIKE '%" + endCat + "%' OR length('" + endCat + "') == 0) ) ") : "");


        Log.i("query", "query " + selectQuery);

        //(filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%' OR model LIKE '%" + filter + "%' OR serialNo LIKE '%" + filter + "%' OR unit LIKE  '%" + filter + "%')") : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();
                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setStatusname((cursor.getString(3)));

                assetsDetail.setBrand((cursor.getString(4)));
                assetsDetail.setModel((cursor.getString(5)));
                assetsDetail.setSerialno((cursor.getString(6)));
                assetsDetail.setUnit((cursor.getString(7)));
                assetsDetail.setCategory((cursor.getString(8)));
                assetsDetail.setLocation((cursor.getString(9)));
                assetsDetail.setLastStockDate((cursor.getString(10)));
                assetsDetail.setCreatedById((cursor.getString(11)));
                assetsDetail.setCreatedByName((cursor.getString(12)));
                assetsDetail.setCreatedDate((cursor.getString(13)));
                assetsDetail.setPurchaseDate((cursor.getString(14)));
                assetsDetail.setPossessor((cursor.getString(15)));
                assetsDetail.setNewEpc((cursor.getString(17)));
                assetsDetail.setEpc((cursor.getString(16)));


                assetsDetail.setBarcode((cursor.getString(18)));
                assetsDetail.setInvoiceDate((cursor.getString(19)));
                assetsDetail.setInvoiceNo((cursor.getString(20)));
                assetsDetail.setFundingSourceid((cursor.getString(21)));
                assetsDetail.setFundingSourcename((cursor.getString(22)));
                assetsDetail.setSupplier((cursor.getString(23)));
                assetsDetail.setMaintenanceDate((cursor.getString(24)));
                assetsDetail.setCost((cursor.getString(25)));
                assetsDetail.setPraticalValue((cursor.getString(26)));
                assetsDetail.setEstimatedLifetime((cursor.getString(27)));
                assetsDetail.setTypeOfTag((cursor.getString(28)));
                assetsDetail.setCertType((cursor.getString(29)));
                assetsDetail.setCertUrl((cursor.getString(30)));
                assetsDetail.setCerstatus((cursor.getString(31)));
                assetsDetail.setStartdate((cursor.getString(32)));
                assetsDetail.setEnddate((cursor.getString(33)));

                assetsDetail.setExhibitsource(cursor.getString(34));
                assetsDetail.setExhibitwitness(cursor.getString(35));
                assetsDetail.setLastassetno(cursor.getString(36));
                //assetsDetail.setExhibitwitness(cursor.getString(37));
                //assetsDetail.setLastassetno(cursor.getString(38));



                //TODO
                // Adding contact to list
                assetsDetails.add(assetsDetail);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }

    public List<AssetsDetail> searchAssetsDetailWithEPC(String assetNo, String name, String startLoc, String endLoc, String brand, String model, String startCat, String endCat, String barcode) {
        List<AssetsDetail> assetsDetails = new ArrayList<AssetsDetail>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE epc is not null AND TRIM(epc,\" \") != \"\" " +
                //      (false ? ("AND  (assetNo LIKE '%" + assetNo + "%' OR name LIKE '%" + name +"%' OR brand LIKE '%" + brand + "%' OR model LIKE  '%" + model + "%')") : "")

                /*OR location LIKE '%" + startLoc + "' OR location LIKE '"+ endLoc + "%' OR category LIKE '%" + startCat + "' OR category LIKE '" + endCat + "%'*/
                (true ? ("AND  ((barcode LIKE '%" + barcode + "%' OR length('" + barcode + "') == 0) AND (assetNo LIKE '%" + assetNo + "%' OR length('" + assetNo + "') == 0)  AND (name LIKE '%" + name +"%' OR length('" + name + "') == 0) AND (brand LIKE '%" + brand + "%' OR LENGTH('" + brand +"') ==0) AND (model LIKE  '%" + model + "%' OR LENGTH('" + model + "') == 0) AND (location LIKE '" + startLoc + "%' OR length('" + startLoc + "') == 0) AND (location LIKE '%" + endLoc + "%' OR length('" + endLoc + "') == 0) AND (category LIKE '" + startCat + "%' OR length('" + startCat + "') == 0) AND (category LIKE '%" + endCat + "%' OR length('" + endCat + "') == 0) ) ") : "");


        Log.i("query", "query " + selectQuery);

        //(filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%' OR model LIKE '%" + filter + "%' OR serialNo LIKE '%" + filter + "%' OR unit LIKE  '%" + filter + "%')") : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();
                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setStatusname((cursor.getString(3)));

                assetsDetail.setBrand((cursor.getString(4)));
                assetsDetail.setModel((cursor.getString(5)));
                assetsDetail.setSerialno((cursor.getString(6)));
                assetsDetail.setUnit((cursor.getString(7)));
                assetsDetail.setCategory((cursor.getString(8)));
                assetsDetail.setLocation((cursor.getString(9)));
                assetsDetail.setLastStockDate((cursor.getString(10)));
                assetsDetail.setCreatedById((cursor.getString(11)));
                assetsDetail.setCreatedByName((cursor.getString(12)));
                assetsDetail.setCreatedDate((cursor.getString(13)));
                assetsDetail.setPurchaseDate((cursor.getString(14)));
                assetsDetail.setPossessor((cursor.getString(15)));
                assetsDetail.setNewEpc((cursor.getString(17)));
                assetsDetail.setEpc((cursor.getString(16)));


                assetsDetail.setBarcode((cursor.getString(18)));
                assetsDetail.setInvoiceDate((cursor.getString(19)));
                assetsDetail.setInvoiceNo((cursor.getString(20)));
                assetsDetail.setFundingSourceid((cursor.getString(21)));
                assetsDetail.setFundingSourcename((cursor.getString(22)));
                assetsDetail.setSupplier((cursor.getString(23)));
                assetsDetail.setMaintenanceDate((cursor.getString(24)));
                assetsDetail.setCost((cursor.getString(25)));
                assetsDetail.setPraticalValue((cursor.getString(26)));
                assetsDetail.setEstimatedLifetime((cursor.getString(27)));
                assetsDetail.setTypeOfTag((cursor.getString(28)));
                assetsDetail.setCertType((cursor.getString(29)));
                assetsDetail.setCertUrl((cursor.getString(30)));
                assetsDetail.setCerstatus((cursor.getString(31)));
                assetsDetail.setStartdate((cursor.getString(32)));
                assetsDetail.setEnddate((cursor.getString(33)));



                //TODO
                // Adding contact to list
                assetsDetails.add(assetsDetail);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }

    public List<Asset> searchAssetWithEPC(String assetNo, String name, String startLoc, String endLoc, String brand, String model, String startCat, String endCat, String offset) {
        List<Asset> assetsDetails = new ArrayList<Asset>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE epc is not null AND TRIM(epc,\" \") != \"\" " +
          //      (false ? ("AND  (assetNo LIKE '%" + assetNo + "%' OR name LIKE '%" + name +"%' OR brand LIKE '%" + brand + "%' OR model LIKE  '%" + model + "%')") : "")

                /*OR location LIKE '%" + startLoc + "' OR location LIKE '"+ endLoc + "%' OR category LIKE '%" + startCat + "' OR category LIKE '" + endCat + "%'*/
        (true ? ("AND  ((assetNo LIKE '%" + assetNo + "%' OR length('" + assetNo + "') == 0)  AND (name LIKE '%" + name +"%' OR length('" + name + "') == 0) AND (brand LIKE '%" + brand + "%' OR LENGTH('" + brand +"') ==0) AND (model LIKE  '%" + model + "%' OR LENGTH('" + model + "') == 0) AND (location LIKE '" + startLoc + "%' OR length('" + startLoc + "') == 0) AND (location LIKE '%" + endLoc + "%' OR length('" + endLoc + "') == 0) AND (category LIKE '" + startCat + "%' OR length('" + startCat + "') == 0) AND (category LIKE '%" + endCat + "%' OR length('" + endCat + "') == 0) ) ") : "")
        + " LIMIT " + SearchListAdapter.PAGE_SIZE +
                " OFFSET " + Integer.parseInt(offset) * SearchListAdapter.PAGE_SIZE;


        Log.i("query", "query " + selectQuery);

        //(filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%' OR model LIKE '%" + filter + "%' OR serialNo LIKE '%" + filter + "%' OR unit LIKE  '%" + filter + "%')") : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();
                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setStatusname((cursor.getString(3)));

                assetsDetail.setBrand((cursor.getString(4)));
                assetsDetail.setModel((cursor.getString(5)));
                assetsDetail.setSerialno((cursor.getString(6)));
                assetsDetail.setUnit((cursor.getString(7)));
                assetsDetail.setCategory((cursor.getString(8)));
                assetsDetail.setLocation((cursor.getString(9)));
                assetsDetail.setLastStockDate((cursor.getString(10)));
                assetsDetail.setCreatedById((cursor.getString(11)));
                assetsDetail.setCreatedByName((cursor.getString(12)));
                assetsDetail.setCreatedDate((cursor.getString(13)));
                assetsDetail.setPurchaseDate((cursor.getString(14)));
                assetsDetail.setPossessor((cursor.getString(15)));
                assetsDetail.setNewEpc((cursor.getString(17)));
                assetsDetail.setEpc((cursor.getString(16)));


                assetsDetail.setBarcode((cursor.getString(18)));
                assetsDetail.setInvoiceDate((cursor.getString(19)));
                assetsDetail.setInvoiceNo((cursor.getString(20)));
                assetsDetail.setFundingSourceid((cursor.getString(21)));
                assetsDetail.setFundingSourcename((cursor.getString(22)));
                assetsDetail.setSupplier((cursor.getString(23)));
                assetsDetail.setMaintenanceDate((cursor.getString(24)));
                assetsDetail.setCost((cursor.getString(25)));
                assetsDetail.setPraticalValue((cursor.getString(26)));
                assetsDetail.setEstimatedLifetime((cursor.getString(27)));
                assetsDetail.setTypeOfTag((cursor.getString(28)));
                assetsDetail.setCertType((cursor.getString(29)));
                assetsDetail.setCertUrl((cursor.getString(30)));
                assetsDetail.setCerstatus((cursor.getString(31)));
                assetsDetail.setStartdate((cursor.getString(32)));
                assetsDetail.setEnddate((cursor.getString(33)));

                //TODO

                Asset asset = new Asset();
                asset.setAssetno(assetsDetail.getAssetNo());
                asset.setName(assetsDetail.getName());
                asset.setBrand(assetsDetail.getBrand());
                asset.setModel(assetsDetail.getModel());
                asset.setEPC(assetsDetail.getEpc());

                ArrayList<String> categoryList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getCategory().split("->").length; i++){
                    categoryList.add(assetsDetail.getCategory().split("->")[i]);
                }
                ArrayList<Category> categoryArrayList = new ArrayList<>();

                if(categoryList != null) {
                    for (int i = 0; i < categoryList.size(); i++) {
                        Category category = new Category();
                        category.setName(categoryList.get(i));
                        categoryArrayList.add(category);
                    }
                }
                asset.setCategories(categoryArrayList);

                ArrayList<Location> locationArrayList = new ArrayList<>();

                ArrayList<String> locationList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getLocation().split("->").length; i++){
                    locationList.add(assetsDetail.getLocation().split("->")[i]);
                }


                if(locationList!= null) {
                    for (int i = 0; i < locationList.size(); i++) {
                        Location location = new Location();
                        location.setName(locationList.get(i));
                        locationArrayList.add(location);
                    }
                }
                asset.setLocations(locationArrayList);
                asset.setEPC(assetsDetail.getEpc());

                Status status = new Status();

                if(assetsDetail.getStatusid() != null){
                    try {
                        status.id = Integer.parseInt(assetsDetail.getStatusid());
                    } catch (Exception e) {
                    }
                    //status.id = -1;
                }
                asset.setStatus(status);
                // Adding contact to list
                assetsDetails.add(asset);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }

    public List<Asset> getAssetByEPC(String filter, String offset) {
        List<Asset> assetsDetails = new ArrayList<Asset>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE epc is not null AND TRIM(epc,\" \") != \"\" " +
                (filter.length() > 0 ? ("AND  ( epc LIKE '%" + filter + "%' )" ) : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +
                " OFFSET " + Integer.parseInt(offset) ;

        //(filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%' OR model LIKE '%" + filter + "%' OR serialNo LIKE '%" + filter + "%' OR unit LIKE  '%" + filter + "%')") : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();

                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setStatusname((cursor.getString(3)));

                assetsDetail.setBrand((cursor.getString(4)));
                assetsDetail.setModel((cursor.getString(5)));
                assetsDetail.setSerialno((cursor.getString(6)));
                assetsDetail.setUnit((cursor.getString(7)));
                assetsDetail.setCategory((cursor.getString(8)));
                assetsDetail.setLocation((cursor.getString(9)));
                assetsDetail.setLastStockDate((cursor.getString(10)));
                assetsDetail.setCreatedById((cursor.getString(11)));
                assetsDetail.setCreatedByName((cursor.getString(12)));
                assetsDetail.setCreatedDate((cursor.getString(13)));
                assetsDetail.setPurchaseDate((cursor.getString(14)));
                assetsDetail.setPossessor((cursor.getString(15)));
                assetsDetail.setNewEpc((cursor.getString(17)));
                assetsDetail.setEpc((cursor.getString(16)));


                assetsDetail.setBarcode((cursor.getString(18)));
                assetsDetail.setInvoiceDate((cursor.getString(19)));
                assetsDetail.setInvoiceNo((cursor.getString(20)));
                assetsDetail.setFundingSourceid((cursor.getString(21)));
                assetsDetail.setFundingSourcename((cursor.getString(22)));
                assetsDetail.setSupplier((cursor.getString(23)));
                assetsDetail.setMaintenanceDate((cursor.getString(24)));
                assetsDetail.setCost((cursor.getString(25)));
                assetsDetail.setPraticalValue((cursor.getString(26)));
                assetsDetail.setEstimatedLifetime((cursor.getString(27)));
                assetsDetail.setTypeOfTag((cursor.getString(28)));
                assetsDetail.setCertType((cursor.getString(29)));
                assetsDetail.setCertUrl((cursor.getString(30)));
                assetsDetail.setCerstatus((cursor.getString(31)));
                assetsDetail.setStartdate((cursor.getString(32)));
                assetsDetail.setEnddate((cursor.getString(33)));


                //TODO

                Asset asset = new Asset();
                asset.setAssetno(assetsDetail.getAssetNo());
                asset.setName(assetsDetail.getName());
                asset.setBrand(assetsDetail.getBrand());
                asset.setModel(assetsDetail.getModel());
                asset.setEPC(assetsDetail.getEpc());

                ArrayList<String> categoryList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getCategory().split("->").length; i++){
                    categoryList.add(assetsDetail.getCategory().split("->")[i]);
                }
                ArrayList<Category> categoryArrayList = new ArrayList<>();

                if(categoryList != null) {
                    for (int i = 0; i < categoryList.size(); i++) {
                        Category category = new Category();
                        category.setName(categoryList.get(i));
                        categoryArrayList.add(category);
                    }
                }
                asset.setCategories(categoryArrayList);

                ArrayList<Location> locationArrayList = new ArrayList<>();

                ArrayList<String> locationList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getLocation().split("->").length; i++){
                    locationList.add(assetsDetail.getLocation().split("->")[i]);
                }


                if(locationList!= null) {
                    for (int i = 0; i < locationList.size(); i++) {
                        Location location = new Location();
                        location.setName(locationList.get(i));
                        locationArrayList.add(location);
                    }
                }
                asset.setLocations(locationArrayList);
                asset.setEPC(assetsDetail.getEpc());

                Status status = new Status();

                if(assetsDetail.getStatusid() != null){
                    try {
                        status.id = Integer.parseInt(assetsDetail.getStatusid());
                    } catch (Exception e) {
                    }
                    //status.id = -1;
                }
                asset.setStatus(status);
                // Adding contact to list
                assetsDetails.add(asset);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }
    public List<AssetsDetail> getAssetByAssetNo(String filter, String offset) {
        List<AssetsDetail> assetsDetails = new ArrayList<AssetsDetail>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE epc is not null AND TRIM(epc,\" \") != \"\" " +
                (filter.length() > 0 ? ("AND  ( assetNo LIKE '%" + filter + "%' )" ) : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +
                " OFFSET " + Integer.parseInt(offset) ;

        //(filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%' OR model LIKE '%" + filter + "%' OR serialNo LIKE '%" + filter + "%' OR unit LIKE  '%" + filter + "%')") : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();
                try {
                    for (int i = 0; i < cursor.getCount(); i++) {
                        Log.i("demo", "demo " + " " + i + " " + cursor.getColumnName(i) + " " + cursor.getString(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setStatusname((cursor.getString(3)));

                assetsDetail.setBrand((cursor.getString(4)));
                assetsDetail.setModel((cursor.getString(5)));
                assetsDetail.setSerialno((cursor.getString(6)));
                assetsDetail.setUnit((cursor.getString(7)));
                assetsDetail.setCategory((cursor.getString(8)));
                assetsDetail.setLocation((cursor.getString(9)));
                assetsDetail.setLastStockDate((cursor.getString(10)));
                assetsDetail.setCreatedById((cursor.getString(11)));
                assetsDetail.setCreatedByName((cursor.getString(12)));
                assetsDetail.setCreatedDate((cursor.getString(13)));
                assetsDetail.setPurchaseDate((cursor.getString(14)));
                assetsDetail.setPossessor((cursor.getString(15)));
                assetsDetail.setNewEpc((cursor.getString(17)));
                assetsDetail.setEpc((cursor.getString(16)));


                assetsDetail.setBarcode((cursor.getString(18)));
                assetsDetail.setInvoiceDate((cursor.getString(19)));
                assetsDetail.setInvoiceNo((cursor.getString(20)));
                assetsDetail.setFundingSourceid((cursor.getString(21)));
                assetsDetail.setFundingSourcename((cursor.getString(22)));
                assetsDetail.setSupplier((cursor.getString(23)));
                assetsDetail.setMaintenanceDate((cursor.getString(24)));
                assetsDetail.setCost((cursor.getString(25)));
                assetsDetail.setPraticalValue((cursor.getString(26)));
                assetsDetail.setEstimatedLifetime((cursor.getString(27)));
                assetsDetail.setTypeOfTag((cursor.getString(28)));
                assetsDetail.setCertType((cursor.getString(29)));
                assetsDetail.setCertUrl((cursor.getString(30)));
                assetsDetail.setCerstatus((cursor.getString(31)));
                assetsDetail.setStartdate((cursor.getString(32)));
                assetsDetail.setEnddate((cursor.getString(33)));


                //TODO

                ArrayList<String> categoryList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getCategory().split("->").length; i++){
                    categoryList.add(assetsDetail.getCategory().split("->")[i]);
                }
                ArrayList<Category> categoryArrayList = new ArrayList<>();

                if(categoryList != null) {
                    for (int i = 0; i < categoryList.size(); i++) {
                        Category category = new Category();
                        category.setName(categoryList.get(i));
                        categoryArrayList.add(category);
                    }
                }

                ArrayList<Location> locationArrayList = new ArrayList<>();

                ArrayList<String> locationList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getLocation().split("->").length; i++){
                    locationList.add(assetsDetail.getLocation().split("->")[i]);
                }


                if(locationList!= null) {
                    for (int i = 0; i < locationList.size(); i++) {
                        Location location = new Location();
                        location.setName(locationList.get(i));
                        locationArrayList.add(location);
                    }
                }

                Status status = new Status();

                if(assetsDetail.getStatusid() != null){
                    try {
                        status.id = Integer.parseInt(assetsDetail.getStatusid());
                    } catch (Exception e) {
                    }
                    //status.id = -1;
                }
                // Adding contact to list
                assetsDetails.add(assetsDetail);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }

    public List<Asset> getAssetWithEPC(String filter, String offset) {
        List<Asset> assetsDetails = new ArrayList<Asset>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE epc is not null AND TRIM(epc,\" \") != \"\" " +
                (filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%' OR model LIKE  '%" + filter + "%' OR epc LIKE '%" + filter + "%' )" ) : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +
        " OFFSET " + Integer.parseInt(offset) ;

        //(filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%' OR model LIKE '%" + filter + "%' OR serialNo LIKE '%" + filter + "%' OR unit LIKE  '%" + filter + "%')") : "") + " LIMIT " + SearchListAdapter.PAGE_SIZE +

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();
                try {
                    for (int i = 0; i < cursor.getCount(); i++) {
                       // Log.i("demo", "demo " + " " + i + " " + cursor.getColumnName(i) + " " + cursor.getString(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setStatusname((cursor.getString(3)));

                assetsDetail.setBrand((cursor.getString(4)));
                assetsDetail.setModel((cursor.getString(5)));
                assetsDetail.setSerialno((cursor.getString(6)));
                assetsDetail.setUnit((cursor.getString(7)));
                assetsDetail.setCategory((cursor.getString(8)));
                assetsDetail.setLocation((cursor.getString(9)));
                assetsDetail.setLastStockDate((cursor.getString(10)));
                assetsDetail.setCreatedById((cursor.getString(11)));
                assetsDetail.setCreatedByName((cursor.getString(12)));
                assetsDetail.setCreatedDate((cursor.getString(13)));
                assetsDetail.setPurchaseDate((cursor.getString(14)));
                assetsDetail.setPossessor((cursor.getString(15)));
                assetsDetail.setNewEpc((cursor.getString(17)));
                assetsDetail.setEpc((cursor.getString(16)));


                assetsDetail.setBarcode((cursor.getString(18)));
                assetsDetail.setInvoiceDate((cursor.getString(19)));
                assetsDetail.setInvoiceNo((cursor.getString(20)));
                assetsDetail.setFundingSourceid((cursor.getString(21)));
                assetsDetail.setFundingSourcename((cursor.getString(22)));
                assetsDetail.setSupplier((cursor.getString(23)));
                assetsDetail.setMaintenanceDate((cursor.getString(24)));
                assetsDetail.setCost((cursor.getString(25)));
                assetsDetail.setPraticalValue((cursor.getString(26)));
                assetsDetail.setEstimatedLifetime((cursor.getString(27)));
                assetsDetail.setTypeOfTag((cursor.getString(28)));
                assetsDetail.setCertType((cursor.getString(29)));
                assetsDetail.setCertUrl((cursor.getString(30)));
                assetsDetail.setCerstatus((cursor.getString(31)));
                assetsDetail.setStartdate((cursor.getString(32)));
                assetsDetail.setEnddate((cursor.getString(33)));

                assetsDetail.setLastassetno(cursor.getString(36));
                Log.i("data", "data " + (cursor.getString(33)) + " " + (cursor.getString(34)) + " " + (cursor.getString(35))  + " " + (cursor.getString(36)));


                //TODO

                Asset asset = new Asset();
                asset.setAssetno(assetsDetail.getAssetNo());
                asset.setName(assetsDetail.getName());
                asset.setBrand(assetsDetail.getBrand());
                asset.setModel(assetsDetail.getModel());
                asset.setEPC(assetsDetail.getEpc());
                asset.setProsecutionNo(assetsDetail.getLastassetno());

                ArrayList<String> categoryList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getCategory().split("->").length; i++){
                    categoryList.add(assetsDetail.getCategory().split("->")[i]);
                }
                ArrayList<Category> categoryArrayList = new ArrayList<>();

                if(categoryList != null) {
                    for (int i = 0; i < categoryList.size(); i++) {
                        Category category = new Category();
                        category.setName(categoryList.get(i));
                        categoryArrayList.add(category);
                    }
                }
                asset.setCategories(categoryArrayList);

                ArrayList<Location> locationArrayList = new ArrayList<>();

                ArrayList<String> locationList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getLocation().split("->").length; i++){
                    locationList.add(assetsDetail.getLocation().split("->")[i]);
                }


                if(locationList!= null) {
                    for (int i = 0; i < locationList.size(); i++) {
                        Location location = new Location();
                        location.setName(locationList.get(i));
                        locationArrayList.add(location);
                    }
                }
                asset.setLocations(locationArrayList);
                asset.setEPC(assetsDetail.getEpc());

                Status status = new Status();

                if(assetsDetail.getStatusid() != null){
                    try {
                        status.id = Integer.parseInt(assetsDetail.getStatusid());
                    } catch (Exception e) {
                    }
                    //status.id = -1;
                }
                asset.setStatus(status);
                // Adding contact to list
                assetsDetails.add(asset);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }


    public int getAssetWithEPCCount(String filter, String offset) {
        String countQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE epc is not null AND TRIM(epc,\" \") != \"\" " +
                (filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%') ") : "");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

         return count;
        //return cursor.getCount();
    }

    public List<Asset> getAssetWithEPC(String filter) {
        List<Asset> assetsDetails = new ArrayList<Asset>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ASSETS_DETAIL + " WHERE epc is not null AND TRIM(epc,\" \") != \"\" " +
                (filter.length() > 0 ? ("AND  (assetNo LIKE '%" + filter + "%' OR name LIKE '%" + filter +"%' OR brand LIKE '%" + filter + "%')") : "");
        ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AssetsDetail assetsDetail = new AssetsDetail();
                assetsDetail.setAssetNo((cursor.getString(0)));
                assetsDetail.setName((cursor.getString(1)));
                assetsDetail.setStatusid((cursor.getString(2)));
                assetsDetail.setBrand((cursor.getString(3)));
                assetsDetail.setModel((cursor.getString(4)));
                assetsDetail.setSerialno((cursor.getString(5)));
                assetsDetail.setUnit((cursor.getString(6)));
                assetsDetail.setCategory((cursor.getString(7)));
                assetsDetail.setLocation((cursor.getString(8)));
                assetsDetail.setLastStockDate((cursor.getString(9)));
                assetsDetail.setCreatedById((cursor.getString(10)));
                assetsDetail.setCreatedByName((cursor.getString(11)));
                assetsDetail.setCreatedDate((cursor.getString(12)));
                assetsDetail.setPurchaseDate((cursor.getString(13)));
                assetsDetail.setPossessor((cursor.getString(14)));
                assetsDetail.setNewEpc((cursor.getString(16)));
                assetsDetail.setEpc((cursor.getString(15)));


                //TODO

                Asset asset = new Asset();
                asset.setAssetno(assetsDetail.getAssetNo());
                asset.setName(assetsDetail.getName());
                asset.setBrand(assetsDetail.getBrand());
                asset.setModel(assetsDetail.getModel());
                asset.setEPC(assetsDetail.getEpc());

                ArrayList<String> categoryList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getCategory().split("->").length; i++){
                    categoryList.add(assetsDetail.getCategory().split("->")[i]);
                }
                ArrayList<Category> categoryArrayList = new ArrayList<>();

                if(categoryList != null) {
                    for (int i = 0; i < categoryList.size(); i++) {
                        Category category = new Category();
                        category.setName(categoryList.get(i));
                        categoryArrayList.add(category);
                    }
                }
                asset.setCategories(categoryArrayList);

                ArrayList<Location> locationArrayList = new ArrayList<>();

                ArrayList<String> locationList = new ArrayList<>();

                for(int i = 0; i < assetsDetail.getLocation().split("->").length; i++){
                    locationList.add(assetsDetail.getLocation().split("->")[i]);
                }


                if(locationList!= null) {
                    for (int i = 0; i < locationList.size(); i++) {
                        Location location = new Location();
                        location.setName(locationList.get(i));
                        locationArrayList.add(location);
                    }
                }
                asset.setLocations(locationArrayList);
                asset.setEPC(assetsDetail.getEpc());

                // Adding contact to list
                assetsDetails.add(asset);
            } while (cursor.moveToNext());
        }

        // return contact list
        return assetsDetails;
    }
    /*
    // code to add the new contact

    // code to get the single contact
    Contact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                        KEY_NAME, KEY_PH_NO }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        // return contact
        return contact;
    }

    // code to get all contacts in a list view
    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setID(Integer.parseInt(cursor.getString(0)));
                contact.setName(cursor.getString(1));
                contact.setPhoneNumber(cursor.getString(2));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // code to update the single contact
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
    }

    // Deleting single contact
    public void deleteContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
        db.close();
    }

    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
*/
}