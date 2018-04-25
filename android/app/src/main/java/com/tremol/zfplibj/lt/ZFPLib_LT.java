package com.tremol.zfplibj;


import com.sun.PrintfFormat;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Mincho on 7.4.2017 г..
 */

public class ZFPLib_LT extends ZFPLib
{
    public ZFPLib_LT(ZFPLib baseLib)
    {
        super(baseLib.m_port,"cp1257", baseLib.m_fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.LT, 9);
    }

    public ZFPLib_LT(ZFPPort m_port, FPLogger fpLogger)//x
    {
        super(m_port, "UTF-8", fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.LT, 9);
    }
}
