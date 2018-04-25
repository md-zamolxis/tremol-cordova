
package com.tremol.zfplibj;

import java.io.UnsupportedEncodingException;


public class ZFPDepartment implements CharSequence
{

    protected int m_num;
    protected String m_name;
    protected float m_price;
    protected char m_taxgrp;
    protected float m_turnover;
    protected float m_sales;
    protected int m_counter;

    public ZFPDepartment(int number, byte[] output, int outputLen, int lang) throws ZFPException
    {
        m_num = number;

        try
        {
            String[] sa = new String(output, ZFPLib.CP).split("[;]");
            m_name = sa[1].trim();
            m_taxgrp = sa[2].charAt(0);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(0x106, lang);
        }

        // m_name = new String(output, 7, 20).trim();
        //        String[] s = new String(output, 31, outputLen - 52,"cp1251").split("[;]"); //x  \\s-\\:
        //        if (6 != s.length) //5
        //            throw new ZFPException(0x106, lang);
        //
        //        String[] dat = new String(output, 75, 16).split("[\\s-\\:]");
        //        if (5 != dat.length) //5 s
        //            throw new ZFPException(0x106, lang);
        //
        //        m_datetime = Calendar.getInstance();
        //        m_datetime.set(Integer.parseInt(dat[2]), Integer.parseInt(dat[1]), Integer.parseInt(dat[0]), Integer.parseInt(dat[3]), Integer.parseInt(dat[4]));
        //
        //        try {
        //            m_price = Float.parseFloat(s[0].trim());
        //            m_taxgrp = s[1].charAt(0);
        //            m_turnover = Float.parseFloat(s[2].trim());
        //            m_sales = Float.parseFloat(s[3].trim());
        //            m_counter = Integer.parseInt(s[4].trim());
        //        } catch (Exception e) {
        //            throw new ZFPException(0x106, lang);
        //        }
    }


    public int getNumber()
    {
        return m_num;
    }

    public String getName()
    {
        return m_name;
    }

    public float getPrice()
    {
        return m_price;
    }

    public char getTaxGroup()
    {
        return m_taxgrp;
    }

    public float getTurnover()
    {
        return m_turnover;
    }

    public float getSales()
    {
        return m_sales;
    }

    public int getReportCounter()
    {
        return m_counter;
    }

    String tostring;

    @Override
    public String toString()
    {
        if (tostring == null)
            tostring = m_num + "." + (m_name == null ? "" : m_name) + "-" + m_taxgrp;
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