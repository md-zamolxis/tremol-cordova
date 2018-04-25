/*
/*
 * zfplib.java
 *
 */

package com.tremol.zfplibj;

import java.io.*;
import com.sun.PrintfFormat;

/**
 * ZFPLib is the main class responsible for communication with Zeka FP
 * fiscal printer device. In order to be used with serial port, it requires
 * <a href="http://java.sun.com/products/javacomm/">Java(tm) Communications API</a>
 * <p>Sample:</p>
 * <pre>
 * ZFPLib zfp = new ZFPLib(2, 9600); // COM2 baud rate 9600
 * zfp.openFiscalBon(1, "0000", false, false);
 * zfp.sellFree("Test article", '1', 2.34f, 1.0f, 0.0f);
 * zfp.sellFree("��������", '1', 1.0f, 3.54f, 0.0f);
 * float sum = zfp.calcIntermediateSum(false, false, false, 0.0f, '0');
 * zfp.payment(sum, 0, false);
 * zfp.closeFiscalBon();
 * </pre>
 */
public class ZFPLib_MN extends ZFPLib
{
    public ZFPLib_MN(ZFPLib baseLib)
    {
        super(baseLib.m_port, "cp1251", baseLib.m_fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.MN, 9);
    }

    public ZFPLib_MN(ZFPPort m_port, FPLogger fpLogger)
    {
        super(m_port ,"cp1251", fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.MN, 9);
    }


    /**
     * Gets Zeka FP status (errors and other flags)
     *
     * @return ZFPStatus_TZ class containing the current status
     * @throws ZFPException in case of communication error
     * @see ZFPStatus
     */
    @Override
    public com.tremol.zfplibj.ZFPStatus_MN getStatus() throws ZFPException
    {
        sendCommand((byte) 0x20, null);
        return new com.tremol.zfplibj.ZFPStatus_MN(m_receiveBuf, m_receiveLen, m_lang);
    }


    @Override
    public com.tremol.zfplibj.ZFPArticle_MN getArticleInfo(int number) throws ZFPException
    {
        if ((1000 < number) || (0 > number))
            throw new ZFPException(0x101, m_lang);

        String data = new PrintfFormat("%05d").sprintf(number);
        sendCommand((byte) 0x6B, data.getBytes());
        return new com.tremol.zfplibj.ZFPArticle_MN(number, m_receiveBuf, m_receiveLen, m_lang);
    }

    //NO Discounts allowed in MN
    @Deprecated
    @Override
    public void sellDB(boolean isVoid, int number, float quantity, float discount) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    //NO Discounts allowed in MN
    @Deprecated
    @Override
    public void sellDepartment(String name, int depnum, double price, float quantity, float discount) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
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

    /**
     * Sets Zeka FP item from the internal database
     *
     * @param number the item number in the internal database (0 to 1000)
     * @param name   string with desired item name - truncated to 20 characters when longer
     * @param price  the item price
     * @param taxgrp item tax group attachment
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setArticleInfo(int number, String name, float price, char taxgrp, int depNum, String MU, String AdditionalName, float AivailableQty) throws ZFPException
    {
        if ((0 > number) || (100000 < number) || (-999999999.0f > price) || (9999999999.0f < price))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%05d").sprintf(number));
        data.append(";");
        data.append(new PrintfFormat("%-20s").sprintf(nstrcpy(name, 20)));
        data.append(";");
        data.append(getFloatFormat(price, 2));
        data.append(";");
        data.append(taxgrp);
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
        data.append(MU);
        data.append(";");
        data.append(new PrintfFormat("%-12s").sprintf(nstrcpy(AdditionalName, 12)));
        data.append(";");
        data.append(getFloatFormat(AivailableQty, 3));

        sendCommand((byte) 0x4B, data.toString().getBytes());
    }

}