/*
 * zfplib.java
 *
 */

package com.tremol.zfplibj;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
public class ZFPLib_PA extends ZFPLib
{

    public ZFPLib_PA(ZFPLib baseLib)
    {
        super(baseLib.m_port ,"UTF-8", baseLib.m_fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.PA, 9);
    }

    public ZFPLib_PA(ZFPPort m_port, FPLogger fpLogger)//x
    {
        super(m_port, "UTF-8", fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.PA, 9);
    }

    /**
     * @deprecated use {@link #setArticleInfo(int number, String name, float price, char taxgrp) throws ZFPException} instead.
     */
    @Deprecated
    @Override
    public void setArticleInfo(int number, String name, float price, char taxgrp, int depNo) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    /**
     * Prints graphic logo
     *
     * @throws ZFPException in case of communication error
     */
    @Override
    public void printLogo() throws ZFPException
    {
        sendCommand((byte) 0x6C, null);
    }

    /**
     * @deprecated use {@link #openFiscalBon(int, String, int) instead } throws ZFPException, UnsupportedEncodingException.
     */
    @Deprecated
    @Override
    public void openFiscalBon(int oper, String pass, boolean detailed, boolean vat, boolean delayPrint) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }


    /**
     * Opens  client receipt
     *
     * @param oper     indicates the exact number operator (1 to 9)
     * @param pass     string containing the certain operator password - 4 characters
     * @param docType  type of the document
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void openFiscalBon(int oper, String pass, int docType) throws ZFPException
    {
        if (((20 < oper) || (1 > oper)) || ((4 < docType) || (1 > docType)))
            throw new ZFPException(0x001, m_lang);
        //String operator = Integer.toString(oper);
        StringBuffer data = new StringBuffer(String.format("%02d", oper));
        data.append(";");
        data.append(new PrintfFormat("% -4s").sprintf(nstrcpy(pass, 4)));
        data.append(";");
        data.append(Integer.toString(docType));
        data.append(";0");

        sendCommand((byte) 0x30, data.toString().getBytes());
    }

    /**
     * @deprecated use {@link #sellFree(int code, String name, char taxgrp, double price, float quantity, float discount, float taxISC) throws ZFPException, UnsupportedEncodingException}
     */
    @Deprecated
    @Override
    public void sellFree(String name, char taxgrp, double price, float quantity, float discount) throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    public void sellFree(int code, String name, char taxgrp, double price, float quantity, float discount, float taxISC) throws ZFPException
    {
        if ((-99999999.0f > price) || (99999999.0f < price) || (0.0f > quantity) ||
                (999999.999f < quantity) || (-999.0f > discount) || (999.0f < discount))
            throw new ZFPException(0x101, m_lang);

        //StringBuffer data = new StringBuffer(new PrintfFormat("%-36s").sprintf(nstrcpy(name, 36)));
        StringBuffer data = new StringBuffer(new PrintfFormat("%-20s").sprintf(nstrcpy(Integer.toString(code), 20)));
        data.append(";");
        data.append(new PrintfFormat("%-30s").sprintf(nstrcpy(name, 30)));
        data.append(";");
        data.append(taxgrp);
        data.append(";");
        data.append(new PrintfFormat("%-3s").sprintf(nstrcpy(Float.toString(taxISC), 3)));
        //data.append(getFloatFormat(taxISC,2));
        data.append(";");
        data.append(getFloatFormat(price, 2));
        data.append("*");
        data.append(getFloatFormat(quantity, 3));
        if (0.0f != discount)
        {
            data.append(",");
            data.append(new PrintfFormat("%6.2f").sprintf(discount));
            data.append("%");
        }
        try
        {
            sendCommand((byte) 0x31, data.toString().getBytes(CP));//x
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
    }
}