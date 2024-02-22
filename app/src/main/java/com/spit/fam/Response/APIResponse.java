package com.spit.fam.Response;

public class APIResponse {

    private int status;

    public void setStatus(String status) {
        Status = status;
    }

    private String Status;

    public int getStatus() {
        return status;
    }
    public String getStatusString() {
        return Status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public int getReturnCount() {
        return returnCount;
    }

    public void setReturnCount(int returnCount) {
        this.returnCount = returnCount;
    }

    private int returnCount;

    public int getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }

    private int borrowCount;

    public int getDisposalCount() {
        return disposalCount;
    }

    public void setDisposalCount(int disposalCount) {
        this.disposalCount = disposalCount;
    }

    private int disposalCount;

    public int type;
}
