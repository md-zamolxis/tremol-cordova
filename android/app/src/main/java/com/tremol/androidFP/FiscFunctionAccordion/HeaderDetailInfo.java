package com.tremol.androidFP.FiscFunctionAccordion;

import java.util.ArrayList;

/**
 * Created by User on 3/13/2015.
 */
public class HeaderDetailInfo
{
    private String name;
    private ArrayList<DetailChildInfo> productList = new ArrayList<DetailChildInfo>();;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ArrayList<DetailChildInfo> getProductList() {
        return productList;
    }
    public void setProductList(ArrayList<DetailChildInfo> productList) { this.productList = productList; }
}
