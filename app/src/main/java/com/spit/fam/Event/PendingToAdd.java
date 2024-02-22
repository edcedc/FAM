package com.spit.fam.Event;

import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPUser;
import com.spit.fam.Response.LevelData;

import java.util.ArrayList;
import java.util.List;

public class PendingToAdd {
    public ArrayList<LevelData> levelData = new ArrayList<>();
    public String fatherNo;
    public int level;
    public int type = -1;
    public String typeString;
    public List<AssetsDetail> assetsDetail = null;
    public List<Asset> assetList = null;

    public List<SPUser> spUsers = null;
 }
