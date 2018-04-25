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
public class ZFPLib_RO extends ZFPLib
{
    public ZFPLib_RO(ZFPLib baseLib)
    {
        super(baseLib.m_port ,"cp1250", baseLib.m_fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.RO, 9);
    }

    public ZFPLib_RO(ZFPPort m_port, FPLogger fpLogger)//x
    {
        super(m_port,"cp1250", fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.RO, 9);
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
}