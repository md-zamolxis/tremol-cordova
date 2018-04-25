package com.tremol.zfplibj;

import com.sun.PrintfFormat;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Mincho on 28.3.2017 г..
 */

public class ZFPLib_MK extends ZFPLib
{
    public ZFPLib_MK(ZFPLib baseLib)
    {
        super(baseLib.m_port,"cp1251", baseLib.m_fpLogger);
        init(ZFPException.ZFP_LANG_BG, ZFPCountry.MK, 20);
    }

    public ZFPLib_MK(ZFPPort m_port, FPLogger fpLogger)
    {
        super(m_port ,"cp1251", fpLogger);
        init(ZFPException.ZFP_LANG_BG, ZFPCountry.MK, 20);
    }

    @Override
    @Deprecated
    public void openFiscalBon(int oper, String pass, boolean detailed, boolean vat, boolean delayPrint) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    public void openFiscalBon(int oper, String pass, boolean delayPrint) throws ZFPException
    {
        if ((m_Operators < oper) || (1 > oper))
            throw new ZFPException(0x101, m_lang);

        String opDigitsStr = String.valueOf(Integer.toString(m_Operators).length());
        StringBuffer data = new StringBuffer(new PrintfFormat("%0" + opDigitsStr + "d").sprintf(oper));
        data.append(";");
        data.append(new PrintfFormat("%-4s").sprintf(nstrcpy(pass, 4)));
        data.append(";1;0"); //type sale , reserved
        data.append(delayPrint ? ";2" : ";0");//x

        sendCommand((byte) 0x30, data.toString().getBytes());
    }

    public void openStornoBon(int oper, String pass, boolean delayPrint) throws ZFPException
    {
        if ((m_Operators < oper) || (1 > oper))
            throw new ZFPException(0x101, m_lang);

        String opDigitsStr = String.valueOf(Integer.toString(m_Operators).length());
        StringBuffer data = new StringBuffer(new PrintfFormat("%0" + opDigitsStr + "d").sprintf(oper));
        data.append(";");
        data.append(new PrintfFormat("%-4s").sprintf(nstrcpy(pass, 4)));
        data.append(";0;0"); //type storno , reserved
        data.append(delayPrint ? ";2" : ";0");//x

        sendCommand((byte) 0x30, data.toString().getBytes());
    }

    @Override
    public void payment(double sum, int type, boolean noRest) throws ZFPException //float
    {
        if ((0 > type) || (4 < type) || (9999999999.0f < sum))//|| (0.0f > sum)
            throw new ZFPException(0x101, m_lang);

        String data = Integer.toString(type);
        data += noRest ? ";1;" : ";0;";
        data += sum == -1f ? "\"" : getFloatFormat(Math.ceil(sum), 2);///македонците нямат стотинки
        sendCommand((byte) 0x35, data.getBytes());
    }

    @Override
    public com.tremol.zfplibj.ZFPArticle_MK getArticleInfo(int number) throws ZFPException
    {
        if ((1000 < number) || (0 > number))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%05d").sprintf(number));
        data.append(";1");
        sendCommand((byte) 0x6B, data.toString().getBytes());
        return new com.tremol.zfplibj.ZFPArticle_MK(number, m_receiveBuf, m_receiveLen, m_lang);
    }

    @Deprecated
    @Override
    public void setArticleInfo(int number, String name, float price, char taxgrp) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public void setArticleInfo(int number, String name, float price, char taxgrp, int depNo) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    public void setArticleInfo(int number, String name, float price, int depNum, int flagPrice, int flagQTY, int flagOrigin, String barcode, float quantity) throws ZFPException
    {
        if ((0 > number) || (10000 < number) || (-999999999.0f > price) || (9999999999.0f < price) ||
                (-999999999.0f > quantity) || (9999999999.0f < quantity))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%05d").sprintf(number));
        data.append(";1;"); //opt 1
        data.append(new PrintfFormat("%-32s").sprintf(nstrcpy(name, 20)));
        data.append(";");
        data.append(getFloatFormat(price, 2));
        data.append(";");

        byte flag = (byte) (0x80 | flagPrice | (flagQTY << 2) | (flagOrigin << 4));
        byte[] flags = new byte[1];
        flags[0] = flag;
        try
        {
            data.append(new String(flags, CP));
        }
        catch (Exception e)
        {
            throw new ZFPException(0x101, m_lang);
        }
        data.append(";");
        byte b[] = new byte[1];
        b[0] = (byte) (0x80 + depNum);
        try
        {
            data.append(new String(b, CP));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(0x101, m_lang);
        }
        data.append(";");
        data.append(getFloatFormat(quantity, 3));
        data.append(";");
        data.append(barcode);
        try
        {
            byte[] dt = data.toString().getBytes(CP);
            sendCommand((byte) 0x4B, dt);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(0x101, m_lang);
        }
    }
}
