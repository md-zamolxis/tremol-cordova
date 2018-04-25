package com.tremol.zfplibj;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * Created by Mincho on 28.3.2017 г..
 */

public class ZFPArticle_MK extends ZFPArticle implements CharSequence
{
    public static final int FLAG_QTY_NOT_CALCULATED = 0;
    public static final int FLAG_QTY_FORBID_NEGATIVE = 1;
    public static final int FLAG_QTY_ALLOW_NEGATIVE = 2;

    public static final int FLAG_PRICE_ONLY_PROGRAMMED = 0;
    public static final int FLAG_PRICE_FREE_ALLOWED = 1;
    public static final int FLAG_PRICE_LIMITED = 2;

    public static final int FLAG_ORIGIN_IMPORTED = 0;
    public static final int FLAG_ORIGIN_LOCAL = 1;

    protected int m_num;
    protected String m_name;
    protected double m_price;//float
    protected float m_turnover;
    protected float m_turnover_storno;
    protected float m_available_qty;
    protected float m_sales;
    protected float m_sales_storno;
    protected int m_no_z_rep;
    protected Calendar m_datetime;
    protected int m_dep_no;
    protected int m_flag_qty;
    protected int m_flag_price;
    protected int m_flag_origin;
    protected String m_barcode;


    public ZFPArticle_MK(int number, byte[] output, int outputLen, int lang) throws ZFPException
    {
        m_num = number;
        try
        {
            int idx = 0;
            String[] s = new String(output, 4, outputLen - 7, ZFPLib.CP).split("[;]"); //x  \\s-\\:
            if (14 != s.length) //5
                throw new ZFPException(0x106, lang);

            if (Integer.parseInt(s[idx++]) != number) //5
                throw new ZFPException(0x106, lang);
            idx++; //option

            m_name = s[idx++];
            m_price = Double.parseDouble(s[idx++]);// Float.parseFloat(s[0].trim());
            byte flag = s[idx++].getBytes(ZFPLib.CP)[0];

            m_flag_price = 0x03 & flag ;
            m_flag_qty =(0x0C & flag) >> 2 ;
            m_flag_origin = (0x10 & flag) >> 4;
            m_dep_no = (s[idx++].getBytes(ZFPLib.CP)[0] & 0xFF) - 0x80;
            m_available_qty = Float.parseFloat(s[idx++].trim());
            m_barcode = s[idx++];
            m_turnover = Float.parseFloat(s[idx++].trim());
            m_sales = Float.parseFloat(s[idx++].trim());
            m_turnover_storno = Float.parseFloat(s[idx++].trim());
            m_sales_storno = Float.parseFloat(s[idx++].trim());
            m_no_z_rep = Integer.parseInt(s[idx++].trim());
            String[] dat = s[idx++].split("[\\s-\\:]");
            if (5 != dat.length) //5 s
                throw new ZFPException(0x106, lang);
            m_datetime = Calendar.getInstance();
            m_datetime.set(Integer.parseInt(dat[2]), Integer.parseInt(dat[1]), Integer.parseInt(dat[0]), Integer.parseInt(dat[3]), Integer.parseInt(dat[4]));
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
     * Gets the turnover accumulated by item sales
     *
     * @return item turnover
     */
    public float getTurnover()
    {
        return m_turnover;
    }

    /** Gets the number of item sales
     *  @return item sales number

    public float getSales()
    {
    retun m_sales;
    }
     */
    /**
     * Gets the number of last item report
     *
     * @return date and time of last report
     */

    public Calendar getReportDateTime()
    {
        return m_datetime;
    }

    public int getFlagQTY() { return m_flag_qty; }
    public int getFlagPrice() { return m_flag_price; }
    public int getFlagOrigin() { return m_flag_origin; }

    public String getBarcode() { return m_barcode; }
    public int getDepNo() { return m_dep_no; }
    public float getAvailableQuantity() { return m_available_qty; }

    String tostring;

    @Override
    public String toString()
    {
        if (tostring == null)
            tostring = m_num + "." + (m_name == null ? "" : m_name) + "-" + m_price + "-" + m_dep_no;
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