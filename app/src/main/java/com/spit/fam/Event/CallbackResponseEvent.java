package com.spit.fam.Event;

public class CallbackResponseEvent {
    //response.raw().request().url()
    public CallbackResponseEvent(Object response){
        this.response = response;
    }

    public CallbackResponseEvent(String id, String url, Object response){
        this.id = id;
        this.response = response;
        this.url = url;
    }

    public CallbackResponseEvent(String id, Object response){
        this.id = id;
        this.response = response;
    }

    public CallbackResponseEvent(String id, String assetNo, String url, Object response){
        this.id = id;
        this.response = response;
        this.url = url;
        this.assetNo = assetNo;
    }

    private String assetNo;
    public String getAssetNo() {
        return assetNo;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public String getFatherno() {
        return fatherno;
    }

    public void setFatherno(String fatherno) {
        this.fatherno = fatherno;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    private Object response;
    private String url;
    private String fatherno;
    public int type;
    public int level;
    public boolean empty;
    public String thiscalldate;

    public String getThiscalldate() {
        return thiscalldate;
    }

    public void setThiscalldate(String thiscalldate) {
        this.thiscalldate = thiscalldate;
    }
}
