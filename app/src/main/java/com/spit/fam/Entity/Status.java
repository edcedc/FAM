package com.spit.fam.Entity;

import com.spit.fam.InternalStorage;
import com.orhanobut.hawk.Hawk;

public class Status {
    public int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatusString() {
        String language = Hawk.get(InternalStorage.Setting.LANGUAGE, "zh");
        return getStatus(language, "" + id);
    }

    public String getStatus(String languageCode, String statusCode){
        if(languageCode == null) {
            languageCode = "en";
        }

        int status = -1;
        if(statusCode == null) {
            status = 0;
        } else {
            try {
                status = Integer.parseInt(statusCode);
            } catch (Exception e) {

            }
        }

        if(status == 1) {
            if(languageCode.equals("en")) {
                return "In Storage";
            } else if(languageCode.equals("zt")) {
                return "入库中";
            } else if(languageCode.equals("zh")) {
                return "入庫中";
            }
        } else if(status == 2) {
            if(languageCode.equals("en")) {
                return "In Library";
            } else if(languageCode.equals("zt")) {
                return "在库";
            } else if(languageCode.equals("zh")) {
                return "在庫";
            }
        } else if(status == 3) {
            if(languageCode.equals("en")) {
                return "On Loan";
            } else if(languageCode.equals("zt")) {
                return "已借出";
            } else if(languageCode.equals("zh")) {
                return "已借出";
            }
        }else if(status == 4) {
            if(languageCode.equals("en")) {
                return "To Be Lent";
            } else if(languageCode.equals("zt")) {
                return "待借出";
            } else if(languageCode.equals("zh")) {
                return "待借出";
            }
        }else if(status == 5) {
            if(languageCode.equals("en")) {
                return "Delete";
            } else if(languageCode.equals("zt")) {
                return "删除";
            } else if(languageCode.equals("zh")) {
                return "删除";
            }
        }else if(status == 6) {
            if(languageCode.equals("en")) {
                return "Lose";
            } else if(languageCode.equals("zt")) {
                return "丢失";
            } else if(languageCode.equals("zh")) {
                return "丟失";
            }
        }else if(status == 7) {
            if(languageCode.equals("en")) {
                return "Cancellation";
            } else if(languageCode.equals("zt")) {
                return "注销";
            } else if(languageCode.equals("zh")) {
                return "註銷";
            }
        }else if(status == 8) {
            if(languageCode.equals("en")) {
                return "Destruction In Progress";
            } else if(languageCode.equals("zt")) {
                return "销毁中";
            } else if(languageCode.equals("zh")) {
                return "銷毀中";
            }
        }else if(status == 9) {
            if(languageCode.equals("en")) {
                return "Abnormal";
            } else if(languageCode.equals("zt")) {
                return "异常";
            } else if(languageCode.equals("zh")) {
                return "異常";
            }
        }else if(status == 10) {
            if(languageCode.equals("en")) {
                return "Not in storage";
            } else if(languageCode.equals("zt")) {
                return "不在库";
            } else if(languageCode.equals("zh")) {
                return "不在庫";
            }
        } else if(status == 9999) {
            if(languageCode.equals("en")) {
                return "Overdue";
            } else if(languageCode.equals("zt")) {
                return "过期";
            } else if(languageCode.equals("zh")) {
                return "過期";
            }
        } else if(status == 9998) {
            if(languageCode.equals("en")) {
                return "Applied";
            } else if(languageCode.equals("zt")) {
                return "未审核";
            } else if(languageCode.equals("zh")) {
                return "未審核";
            }
        } else if(status == 9997) {
            if(languageCode.equals("en")) {
                return "Approved";
            } else if(languageCode.equals("zt")) {
                return "审核通过";
            } else if(languageCode.equals("zh")) {
                return "可借出";
            }
        } else if(status == 9996) {
            if(languageCode.equals("en")) {
                return "Not Approved";
            } else if(languageCode.equals("zt")) {
                return "审核不通过";
            } else if(languageCode.equals("zh")) {
                return "不可借出";
            }
        } else if(status == 9995) {
            if(languageCode.equals("en")) {
                return "Borrwed";
            } else if(languageCode.equals("zt")) {
                return "已借出";
            } else if(languageCode.equals("zh")) {
                return "已借出";
            }
        }else if(status == 10000) {
            if(languageCode.equals("en")) {
                return "Returned (Not uploaded yet)";
            } else if(languageCode.equals("zt")) {
                return "已归还 (未上传)";
            } else if(languageCode.equals("zh")) {
                return "已歸還 (未上傳)";
            }
        }


        return "";
    }

    private String name;
}
