package com.spit.fam.Entity.SPEntityP2;

import java.util.ArrayList;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AssetsDetail extends RealmObject {
    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }

    private int ordering;

    @PrimaryKey
    private String pk;

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    private String companyid;
    private String userid;

    private String assetNo;
    private String name;
    private String statusid;
    private String statusname;
    private String brand;
    private String model;
    private String serialno;
    private String unit;
    private String category;
    private String location;
    private String lastStockDate;
    private String createdById;
    private String createdByName;
    private String createdDate;
    private String purchaseDate;
    private String possessor;
    private String epc;
    private String newEpc;

    public String getPossessor() {
        return possessor;
    }

    public void setPossessor(String possessor) {
        this.possessor = possessor;
    }

    public String getUsergroup() {
        return usergroup;
    }

    public void setUsergroup(String usergroup) {
        this.usergroup = usergroup;
    }

    private String usergroup;

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    private String invoiceDate;
    private String invoiceNo;
    private String fundingSourceid;
    private String fundingSourcename;
    private String supplier;
    private String maintenanceDate;
    private String cost;
    private String praticalValue;
    private String estimatedLifetime;
    private String typeOfTag;
    private String barcode;

    private String certType;
    private String certUrl;
    private String cerstatus;

    public String getRono() {
        return rono;
    }

    public void setRono(String rono) {
        this.rono = rono;
    }

    private String rono;

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    private String startdate;
    private String enddate;

    public String getCertType() {
        return certType;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    public String getCertUrl() {
        return certUrl;
    }

    public void setCertUrl(String certUrl) {
        this.certUrl = certUrl;
    }

    public String getCerstatus() {
        return cerstatus;
    }

    public void setCerstatus(String cerstatus) {
        this.cerstatus = cerstatus;
    }

    public boolean isIsverified() {
        return isverified;
    }

    public void setIsverified(boolean isverified) {
        this.isverified = isverified;
    }

    private boolean isverified;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }



    public int getCategorySize() {
        if(category == null || category.length() == 0)
            return 0;

        try {
            return category.split("->").length;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getLocationSize() {
        if(location == null || location.length() == 0) {
            return 0;
        }

        try {
            return location.split("->").length;
        } catch (Exception e) {
            return 0;
        }
    }

    public ArrayList<String> getLocations() {
        if(location == null || location.length() == 0) {
            return new ArrayList<>();
        }

        try {
            ArrayList<String> locationList = new ArrayList<>();

            for(int i = 0; i < location.split("->").length; i++){
                locationList.add(location.split("->")[i]);
            }

            return locationList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public ArrayList<String> getCategorys() {
        if(category == null || category.length() == 0) {
            return new ArrayList<>();
        }

        try {
            ArrayList<String> categoryList = new ArrayList<>();

            for(int i = 0; i < category.split("->").length; i++){
                categoryList.add(category.split("->")[i]);
            }

            return categoryList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatusid() {
        return statusid;
    }

    public String getStatusString(String language) {
        if(language == null) {
            language = "en";
        }

        if(statusid == null) {

        } else {
            if(statusid.equals("1")) {
                if(language.equals("en")) {
                    return "In Storage";
                } else if(language.equals("cn")) {
                    return "入库中";
                } else if(language.equals("zt")) {
                    return "入庫中";
                }
            } else if(statusid.equals("2")) {
                if(language.equals("en")) {
                    return "In Library";
                } else if(language.equals("cn")) {
                    return "在库";
                } else if(language.equals("zt")) {
                    return "在庫";
                }
            }else if(statusid.equals("3")) {
                if(language.equals("en")) {
                    return "On Loan";
                } else if(language.equals("cn")) {
                    return "已借出";
                } else if(language.equals("zt")) {
                    return "已借出";
                }
            }else if(statusid.equals("4")) {
                if(language.equals("en")) {
                    return "To Be Lent";
                } else if(language.equals("cn")) {
                    return "待借出";
                } else if(language.equals("zt")) {
                    return "待借出";
                }
            }else if(statusid.equals("5")) {
                if(language.equals("en")) {
                    return "Delete";
                } else if(language.equals("cn")) {
                    return "删除";
                } else if(language.equals("zt")) {
                    return "刪除";
                }
            }else if(statusid.equals("6")) {
                if(language.equals("en")) {
                    return "Lose";
                } else if(language.equals("cn")) {
                    return "丢失";
                } else if(language.equals("zt")) {
                    return "丟失";
                }
            }else if(statusid.equals("7")) {
                if(language.equals("en")) {
                    return "Cancellation";
                } else if(language.equals("cn")) {
                    return "注销";
                } else if(language.equals("zt")) {
                    return "註銷";
                }
            }else if(statusid.equals("8")) {
                if(language.equals("en")) {
                    return "Destruction In Progress";
                } else if(language.equals("cn")) {
                    return "销毁中";
                } else if(language.equals("zt")) {
                    return "銷毀中";
                }
            }else if(statusid.equals("9")) {
                if(language.equals("en")) {
                    return "Abnormal";
                } else if(language.equals("cn")) {
                    return "异常";
                } else if(language.equals("zt")) {
                    return "異常";
                }
            }else if(statusid.equals("10")) {
                if(language.equals("en")) {
                    return "Not in storage";
                } else if(language.equals("cn")) {
                    return "不在库";
                } else if(language.equals("zt")) {
                    return "不在庫";
                }
            }
        }
        return "";
    }

    public void setStatusid(String statusid) {
        this.statusid = statusid;
    }

    public String getStatusname() {
        return statusname;
    }

    public void setStatusname(String statusname) {
        this.statusname = statusname;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialno() {
        return serialno;
    }

    public void setSerialno(String serialno) {
        this.serialno = serialno;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLastStockDate() {
        return lastStockDate;
    }

    public void setLastStockDate(String lastStockDate) {
        this.lastStockDate = lastStockDate;
    }

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getFundingSourceid() {
        return fundingSourceid;
    }

    public void setFundingSourceid(String fundingSourceid) {
        this.fundingSourceid = fundingSourceid;
    }

    public String getFundingSourcename() {
        return fundingSourcename;
    }

    public void setFundingSourcename(String fundingSourcename) {
        this.fundingSourcename = fundingSourcename;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(String maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getPraticalValue() {
        return praticalValue;
    }

    public void setPraticalValue(String praticalValue) {
        this.praticalValue = praticalValue;
    }

    public String getEstimatedLifetime() {
        return estimatedLifetime;
    }

    public void setEstimatedLifetime(String estimatedLifetime) {
        this.estimatedLifetime = estimatedLifetime;
    }

    public String getTypeOfTag() {
        return typeOfTag;
    }

    public void setTypeOfTag(String typeOfTag) {
        this.typeOfTag = typeOfTag;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getNewEpc() {
        return newEpc;
    }

    public void setNewEpc(String newEpc) {
        this.newEpc = newEpc;
    }

    private String exhibitsource;

    public String getExhibitsource() {
        return exhibitsource;
    }

    public void setExhibitsource(String exhibitsource) {
        this.exhibitsource = exhibitsource;
    }

    public String getExhibitwitness() {
        return exhibitwitness;
    }

    public void setExhibitwitness(String exhibitwitness) {
        this.exhibitwitness = exhibitwitness;
    }

    public String getLastassetno() {
        return lastassetno;
    }

    public void setLastassetno(String lastassetno) {
        this.lastassetno = lastassetno;
    }

    private String exhibitwitness;
    private String lastassetno;

}
