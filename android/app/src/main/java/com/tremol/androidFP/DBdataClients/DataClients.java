package com.tremol.androidFP.DBdataClients;

/**
 * Created by User on 28.10.2016 г..
 */

public class DataClients {

    private int ClientNo;
    private String ClientName;
    private String BuyerName;
    private String ZDDS;
    private String Bulstat;
    private String Address;

    public boolean OK;

    public DataClients(){}

    public int getClientNo() {
        return ClientNo;
    }

    public void setClientNo(int clientNo) {
        ClientNo = clientNo;
    }

    public String getClientName() {
        return ClientName;
    }

    public void setClientName(String clientName) {
        ClientName = clientName;
    }

    public String getBuyerName() {
        return BuyerName;
    }

    public void setBuyerName(String buyerName) {
        BuyerName = buyerName;
    }

    public String getZDDS() {
        return ZDDS;
    }

    public void setZDDS(String ZDDS) {
        this.ZDDS = ZDDS;
    }

    public String getBulstat() {
        return Bulstat;
    }

    public void setBulstat(String bulstat) {
        Bulstat = bulstat;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public boolean isOK() {
        return OK;
    }

    public void setOK(boolean OK) {
        this.OK = OK;
    }

}
