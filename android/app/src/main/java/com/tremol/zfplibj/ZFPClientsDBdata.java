package com.tremol.zfplibj;

import java.io.UnsupportedEncodingException;

/**
 * Created by User on 28.10.2016 г..
 */

public class ZFPClientsDBdata implements CharSequence
{

    protected int ClientNo;
    protected String ClientName;
    protected String BuyerName;
    protected String ZDDS;
    protected String Bulstat;
    protected String Address;

    public ZFPClientsDBdata(int number, byte[] output, int outputLen, int lang) throws ZFPException
    {
        ClientNo = number;
        try
        {

            String[] sa = new String(output, ZFPLib.CP).split("[;]");

            if (sa.length < 6) //6
                throw new ZFPException(0x106, lang);

            ClientName = sa[1].trim();
            BuyerName = sa[2].trim();
            ZDDS = sa[3].trim();
            Bulstat = sa[4].trim();
            Address = sa[5].trim();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(0x106, lang);
        }
    }

    @Override
    public String toString()
    {
        return getClientNo() + ". " + getClientName() + ", " + getBulstat() + ", " + getAddress();
    }

    public int getClientNo()
    {
        return ClientNo;
    }

    public String getClientName()
    {
        return ClientName;
    }

    public String getBuyerName()
    {
        return BuyerName;
    }

    public String getZDDS()
    {
        return ZDDS;
    }

    public String getBulstat()
    {
        return Bulstat;
    }

    public String getAddress()
    {
        return Address;
    }

    @Override
    public int length()
    {
        return 0;
    }

    @Override
    public char charAt(int i)
    {
        return 0;
    }

    @Override
    public CharSequence subSequence(int i, int i1)
    {
        return null;
    }
}