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
  * @author <a href="http://tremol.bg/">Tremol Ltd.</a> (Stanimir Jordanov)
  */
public class ZFPArticle implements CharSequence
{

    protected int m_num;
    protected String m_name;
    protected double m_price;//fload
    protected char m_taxgrp;
    protected float m_turnover;
    protected float m_sales;
    protected int m_counter;
    protected Calendar m_datetime;
    protected int m_depNum;

    public ZFPArticle()
    {

    }

    /**
     * Creates a new instance of ZFPArticle
     *
     * @throws ZFPException
     */
    public ZFPArticle(int number, byte[] output, int outputLen, int lang) throws ZFPException
    {

        try
        {
            String[] s = new String(output, 4, outputLen - 7, ZFPLib.CP).split("[;]"); //x  \\s-\\:

            if (9 != s.length)
                throw new ZFPException(0x106, lang);

            m_num = number;
            m_name = s[1].trim();

            m_price = Double.parseDouble(s[2]);// Float.parseFloat(s[0].trim());
            m_taxgrp = s[3].charAt(0);
            m_turnover = Float.parseFloat(s[4].trim());
            m_sales = Float.parseFloat(s[5].trim());
            m_counter = Integer.parseInt(s[6].trim());

            String[] dat = s[7].trim().split("[\\s-\\:]");
            if (5 != dat.length) //5 s
                throw new ZFPException(0x106, lang);

            m_datetime = Calendar.getInstance();
            m_datetime.set(Integer.parseInt(dat[2]), Integer.parseInt(dat[1]), Integer.parseInt(dat[0]), Integer.parseInt(dat[3]), Integer.parseInt(dat[4]));

            m_depNum = (s[8].getBytes(ZFPLib.CP)[0] & 0xFF) - 0x80;
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
     * Gets the number of item sales
     *
     * @return item sales number
     */
    public float getSales()
    {
        return m_sales;
    }

    /**
     * Gets the number of last item report
     *
     * @return number of last report
     */

    public int getReportCounter()
    {
        return m_counter;
    }

    /**
     * Gets the date and time of last item report
     *
     * @return date and time of last report
     */

    public Calendar getReportDateTime()
    {
        return m_datetime;
    }


    public int getDepartmentNumber() throws ZFPException
    {
        return m_depNum;
    }


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