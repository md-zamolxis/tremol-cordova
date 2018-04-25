package com.tremol.zfplibj;

import com.sun.PrintfFormat;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Mincho on 17.3.2017 г..
 */

public class ZFPLib_BG extends ZFPLib
{
    public ZFPLib_BG(ZFPLib baseLib)
    {
        super(baseLib.m_port, "cp1251", baseLib.m_fpLogger);
        init(ZFPException.ZFP_LANG_BG, ZFPCountry.BG, 20);
    }

    public ZFPLib_BG(ZFPPort m_port, FPLogger fpLogger)
    {
        super(m_port, "cp1251", fpLogger);
        init(ZFPException.ZFP_LANG_BG, ZFPCountry.BG, 20);
    }

//    /**
//     * @deprecated use {@link #setArticleInfo(int number, String name, float price, char taxgrp) throws ZFPException} instead.
//     */
//    @Deprecated
//    @Override
//    public void setArticleInfo(int number, String name, float price, char taxgrp, int depNo) throws ZFPException
//    {
//        throw new ZFPException(0xFFF, m_lang);
//    }

    /**
     * Starts Operators report
     *
     * @param zero true speciffies the report as 'Z' (zero report), false 'X' (information report)
     * @param oper speciffies the operator number (0 - 20; 0 is for all operators)
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    @Override
    public void reportOperator(boolean zero, int oper) throws ZFPException
    {
        if ((0 > oper) || (20 < oper))
            throw new ZFPException(0x101, m_lang);

        String data = zero ? "Z" : "X";
        data += ";";
        data += String.format("%02d", oper);
        sendCommand((byte) 0x7D, data.getBytes());
    }


    @Override
    public String getVersion() throws ZFPException
    {
        sendCommand((byte) 0x21, null);
        return getStringResult();
    }
}
