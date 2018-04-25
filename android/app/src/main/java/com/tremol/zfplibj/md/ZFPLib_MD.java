package com.tremol.zfplibj;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Mincho on 17.3.2017 г..
 */

public class ZFPLib_MD extends ZFPLib
{
    public ZFPLib_MD(ZFPLib baseLib)
    {
        super(baseLib.m_port,"UTF-8", baseLib.m_fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.MD, 20);
    }

    public ZFPLib_MD(ZFPPort m_port, FPLogger fpLogger)
    {
        super(m_port ,"cp1251", fpLogger);
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.MD, 20);
    }
}
