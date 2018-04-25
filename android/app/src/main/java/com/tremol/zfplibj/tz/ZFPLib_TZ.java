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
public class ZFPLib_TZ extends ZFPLib
{
    public ZFPLib_TZ(ZFPLib baseLib)
    {
        super(baseLib.m_port ,"UTF-8", baseLib.m_fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.TZ, 9);
    }

    public ZFPLib_TZ(ZFPPort m_port, FPLogger fpLogger)//x
    {
        super(m_port, "UTF-8", fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.TZ, 9);
    }


    /**
     * @deprecated use {@link #setArticleInfo(int number, String name, float price, char taxgrp, int depNo) throws ZFPException} instead.
     */
    @Deprecated
    @Override
    public void setArticleInfo(int number, String name, float price, char taxgrp) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }


    /**
     * Gets Zeka FP status (errors and other flags)
     *
     * @return ZFPStatus_TZ class containing the current status
     * @throws ZFPException in case of communication error
     * @see ZFPStatus
     */
    @Override
    public com.tremol.zfplibj.ZFPStatus_TZ getStatus() throws ZFPException
    {
        sendCommand((byte) 0x20, null);
        return new com.tremol.zfplibj.ZFPStatus_TZ(m_receiveBuf, m_receiveLen, m_lang);
    }

    /**
     * @deprecated use {@link #openFiscalBon(int, String, boolean, int, String, String, String , String , String , String , double , double ) throws ZFPException, UnsupportedEncodingException
    throws ZFPException} instead.
     */
    @Deprecated
    @Override
    public void openFiscalBon(int oper, String pass, boolean detailed, boolean vat, boolean delayPrint) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }


    public void openFiscalBon(int oper, String pass, boolean detail, int type, String cmpnyName, String cmpnyHeadquarters,
                              String clientTIN, String cmpnyAddress, String cmpnyCodeCity, String clientVRN, double sum, double vat) throws ZFPException
    {
        if ((20 < oper) || (1 > oper))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer();
        data.append(String.format("%02d", oper));
        data.append(";");
        data.append(new PrintfFormat("%-4s").sprintf(nstrcpy(pass, 4)));
        data.append(";");
        data.append(detail ? '1' : '0');
        data.append(";1;");
        data.append(Integer.toString(type));
        data.append(";");
        data.append(new PrintfFormat("%-30s").sprintf(nstrcpy(cmpnyName, 30)));
        data.append(";");
        data.append(new PrintfFormat("%-30s").sprintf(nstrcpy(cmpnyHeadquarters, 30)));
        data.append(";");
        data.append(new PrintfFormat("%-10s").sprintf(nstrcpy(clientTIN, 10)));
        data.append(";");
        String adr = new PrintfFormat("%-30s").sprintf(nstrcpy(cmpnyAddress, 30));
        data.append(adr);
        //data.append(new PrintfFormat("%-30s").sprintf(nstrcpy(cmpnyAddress, 30)));
        data.append(";");
        data.append(new PrintfFormat("%-30s").sprintf(nstrcpy(cmpnyCodeCity, 30)));
        data.append(";");
        data.append(new PrintfFormat("%-10s").sprintf(nstrcpy(clientVRN, 10)));
        data.append(";");
        if (sum != 0 && vat != 0)
        {
            data.append(getFloatFormat(sum, 2));
            data.append(";");
            data.append(getFloatFormat(vat, 2));
        }

        StringBuffer d = new StringBuffer();
        try
        {
            byte b[] = data.toString().getBytes();
            d.append(new String(b, CP));
            sendCommand((byte) 0x30, d.toString().getBytes(CP));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
    }


    public void reportPurchase() throws ZFPException
    {
        sendCommand((byte) 0x7C, "Y".getBytes());
    }

    public void reportWeeklyPurchase() throws ZFPException
    {
        sendCommand((byte) 0x7C, "P".getBytes());
    }
}