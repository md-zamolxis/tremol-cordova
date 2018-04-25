/*
 * ZFPArticle.java
 *
 */

package com.tremol.zfplibj;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * ZFPArticle - interface is internal db article data and it is
 * result of {@link com.tremol.zfplibj.ZFPLib#getArticleInfo} method
 *
 * @author <a href="http://tremol.bg/">Tremol Ltd.</a> (Stanimir Jordanov)
 */
public class ZFPArticle_MN extends ZFPArticle implements CharSequence
{

    protected int m_num;
    protected String m_name;
    protected double m_price;//float
    protected char m_taxgrp;
    protected float m_turnover;
    protected Calendar m_datetime;

    protected float m_available_qty;
    protected int m_no_z_rep;

    protected int m_dep_no;
    protected String m_measure_unit;
    protected String m_additional_name;
    protected float m_available_qty_stock;

    /**
     * Creates a new instance of ZFPArticle
     *
     * @throws UnsupportedEncodingException
     */


    public ZFPArticle_MN(int number, byte[] output, int outputLen, int lang) throws ZFPException
    {
        m_num = number;

        m_name = new String(output, 10, 20).trim();

        //String[] dat = new String(output, 75, 16).split("[\\s-\\:]");
        try
        {
            String[] s = new String(output, 31, outputLen - 34, ZFPLib.CP).split("[;]"); //x  \\s-\\:
            if (10 != s.length) //5
                throw new ZFPException(0x106, lang);

            String[] dat = s[5].split("[\\s-\\:]");
            if (5 != dat.length) //5 s
                throw new ZFPException(0x106, lang);

            m_datetime = Calendar.getInstance();
            m_datetime.set(Integer.parseInt(dat[2]), Integer.parseInt(dat[1]), Integer.parseInt(dat[0]), Integer.parseInt(dat[3]), Integer.parseInt(dat[4]));


            m_price = Double.parseDouble(s[0]);// Float.parseFloat(s[0].trim());
            m_taxgrp = s[1].charAt(0);
            m_turnover = Float.parseFloat(s[2].trim());
            m_available_qty = Float.parseFloat(s[3].trim());
            m_no_z_rep = Integer.parseInt(s[4].trim());
            m_dep_no = (s[6].getBytes(ZFPLib.CP)[0] & 0xFF) - 0x80;
            m_measure_unit = s[7];
            m_additional_name = s[8];
            m_available_qty_stock = Float.parseFloat(s[9].trim());
        }
        catch (Exception e)
        {
            throw new ZFPException(0x106, lang);
        }
    }

    /**
     * Gets the number of an item
     *
     * @return item number
     */
    public int getNumber()
    {
        return m_num;
    }

    /**
     * Gets the name of an item
     *
     * @return item name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Gets the price of an item
     *
     * @return item price
     */
    public double getPrice()
    {
        return m_price;
    }

    /**
     * Gets the tax group attachment of an item
     *
     * @return item tax group
     */
    public char getTaxGroup()
    {
        return m_taxgrp;
    }

    /**
     * Gets the turnover accumulated by item sales
     *
     * @return item turnover
     */
    public float getTurnover()
    {
        return m_turnover;
    }

    /**
     * Gets the number of last item report
     *
     * @return date and time of last report
     */
    public Calendar getReportDateTime()
    {
        return m_datetime;
    }


    public float getAvailableQuantity() { return m_available_qty; }
    public float getAvailableQuantityInStock() { return m_available_qty_stock; }
    public int getZrepNumber() { return m_no_z_rep; }
    public int getDepartmentNumber() { return m_dep_no; }
    public String getMeasureUnit() {return m_measure_unit; }
    public String getExtraName() { return m_additional_name; }

    String tostring;

    @Override
    public String toString()
    {
        if (tostring == null)
            tostring = m_num + "." + (m_name == null ? "" : m_name) + "-" + m_price + "-" + m_taxgrp;
        return tostring;
    }

    @Override
    public char charAt(int arg0)
    {
        return this.toString().charAt(arg0);
    }

    @Override
    public int length()
    {
        return this.toString().length();
    }

    @Override
    public CharSequence subSequence(int arg0, int arg1)
    {
        return this.toString().subSequence(arg0, arg1);
    }
}