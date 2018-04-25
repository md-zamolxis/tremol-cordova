package com.tremol.androidFP.OpenReceipt;

import java.text.DecimalFormat;

/**
 * Created by User on 19.1.2016 г..
 */
public class ReceiptParams {

    private int operatorNum;
    private int OperatorPass;
    private ReceiptType receiptType;

    private String CompanyName;
    private String CompanyHeadQuarters;
    private String ClientTIN;
    private String CompanyAddress;
    private String CompanyPostalCity;
    private String ClientVRN;
    private double SUM;
    private double VAT;
    public boolean OK;

    public boolean isOK() {
        return OK;
    }

    public void setOK(boolean OK) {
        this.OK = OK;
    }
    public ReceiptParams(){}

    public enum ReceiptType {standartFiscal(0), purchase(1), standartFiscalPosponedPrint(2);
        private final int TypeRcp;
        private ReceiptType(int value)
        {
            this.TypeRcp = value;
        }

        public int getRcpType()
        {
            return TypeRcp;
        }
    }
    public int getOperatorNum() {
        return operatorNum;
    }

    public void setOperatorNum(int operatorNum) {
        this.operatorNum = operatorNum;
    }

    public int getOperatorPass() {
        return OperatorPass;
    }

    public void setOperatorPass(int operatorPass) {
        OperatorPass = operatorPass;
    }

    public ReceiptType getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(ReceiptType receiptType) {
        this.receiptType = receiptType;
    }

    public String getCompanyName() {
        return CompanyName;
    }

    public void setCompanyName(String companyName) {
        CompanyName = companyName;
    }

    public String getCompanyHeadQuarters() {
        return CompanyHeadQuarters;
    }

    public void setCompanyHeadQuarters(String companyHeadQuarters) {
        CompanyHeadQuarters = companyHeadQuarters;
    }

    public String getClientTIN() {
        return ClientTIN;
    }

    public void setClientTIN(String clientTIN) {
        ClientTIN = clientTIN;
    }

    public String getCompanyAddress() {
        return CompanyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        CompanyAddress = companyAddress;
    }

    public String getCompanyPostalCity() {
        return CompanyPostalCity;
    }

    public void setCompanyPostalCity(String companyPostalCity) {
        CompanyPostalCity = companyPostalCity;
    }

    public String getClientVRN() {
        return ClientVRN;
    }

    public void setClientVRN(String clientVRN) {
        ClientVRN = clientVRN;
    }

    public double getSUM() {
        double s = roundToDecimal(SUM);
        return s;
    }

    public void setSUM(double SUM) {
        this.SUM = SUM;
    }


    public double getVAT() {
        double v = roundToDecimal(VAT);
        return v;
    }

    public void setVAT(double VAT) {
        this.VAT = VAT;
    }

    private double roundToDecimal(double d)
    {
        DecimalFormat twoDform = new DecimalFormat("#.##");
        return Double.valueOf(twoDform.format(d));
    }

}
