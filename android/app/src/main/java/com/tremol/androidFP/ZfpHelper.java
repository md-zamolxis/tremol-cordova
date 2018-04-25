package com.tremol.androidFP;

import android.app.Activity;
import android.content.res.Resources;

import com.tremol.androidFP.OpenReceipt.OpenRcpAlert;
import com.tremol.androidFP.OpenReceipt.ReceiptParams;
import com.tremol.androidFP.ProgramArticle.ZfpProgArticleBuilder;
import com.tremol.androidFP.ProgramArticle.ZfpProgArticleBuilder_MK;
import com.tremol.androidFP.ProgramArticle.ZfpProgArticleBuilder_MN;
import com.tremol.androidFP.Status.StatusAdapter;
import com.tremol.androidFP.Status.ZfpStatusAdapterBuilder;
import com.tremol.androidFP.Status.ZfpStatusAdapterBuilder_MN;
import com.tremol.androidFP.Status.ZfpStatusAdapterBuilder_TZ;
import com.tremol.zfplibj.FPLogger;
import com.tremol.zfplibj.ZFPException;
import com.tremol.zfplibj.ZFPLib;
import com.tremol.zfplibj.ZFPPort;
import com.tremol.zfplibj.ZFPLib_MK;
import com.tremol.zfplibj.ZFPLib_MN;
import com.tremol.zfplibj.ZFPLib_PA;
import com.tremol.zfplibj.ZFPLib_TZ;
import com.tremol.zfplibj.ZFPStatus;
import com.tremol.zfplibj.ZFPStatus_MN;
import com.tremol.zfplibj.ZFPStatus_TZ;


/**
 * Created by Mincho on 24.3.2017 г..
 */

public class ZfpHelper
{
    public static ZFPLib getSpecificLib(String countryAbbreviation, ZFPPort m_port, FPLogger m_fpLogger)
    {
        countryAbbreviation = countryAbbreviation.toUpperCase();
        if (countryAbbreviation.contains("BG"))
        {
            return new com.tremol.zfplibj.ZFPLib_BG(m_port, m_fpLogger);
        }
        else if (countryAbbreviation.contains("TZ"))
        {
            return new com.tremol.zfplibj.ZFPLib_TZ(m_port, m_fpLogger);
        }
        else if (countryAbbreviation.contains("KE"))
        {
            return new com.tremol.zfplibj.ZFPLib_KE(m_port, m_fpLogger);
        }
        else if (countryAbbreviation.contains("MN"))
        {
            return new com.tremol.zfplibj.ZFPLib_MN(m_port, m_fpLogger);
        }
        else if (countryAbbreviation.contains("PA"))
        {
            return new com.tremol.zfplibj.ZFPLib_PA(m_port, m_fpLogger);
        }
        else if (countryAbbreviation.contains("RO"))
        {
            return new com.tremol.zfplibj.ZFPLib_RO(m_port, m_fpLogger);
        }
        else if (countryAbbreviation.contains("MK"))
        {
            return new com.tremol.zfplibj.ZFPLib_MK(m_port, m_fpLogger);
        }
        else if (countryAbbreviation.contains("LT"))
        {
            return new com.tremol.zfplibj.ZFPLib_LT(m_port, m_fpLogger);
        }
        else if (countryAbbreviation.contains("MD"))
        {
            return new com.tremol.zfplibj.ZFPLib_MD(m_port, m_fpLogger);
        }
        return null;
    }

    public static ZFPLib getSpecificLib(String countryAbbreviation, ZFPLib baseLib)
    {
        countryAbbreviation = countryAbbreviation.toUpperCase();
        if (countryAbbreviation.contains("BG"))
        {
            return new com.tremol.zfplibj.ZFPLib_BG(baseLib);
        }
        else if (countryAbbreviation.contains("TZ"))
        {
            return new com.tremol.zfplibj.ZFPLib_TZ(baseLib);
        }
        else if (countryAbbreviation.contains("KE"))
        {
            return new com.tremol.zfplibj.ZFPLib_KE(baseLib);
        }
        else if (countryAbbreviation.contains("MN"))
        {
            return new com.tremol.zfplibj.ZFPLib_MN(baseLib);
        }
        else if (countryAbbreviation.contains("PA"))
        {
            return new com.tremol.zfplibj.ZFPLib_PA(baseLib);
        }
        else if (countryAbbreviation.contains("RO"))
        {
            return new com.tremol.zfplibj.ZFPLib_RO(baseLib);
        }
        else if (countryAbbreviation.contains("MK"))
        {
            return new com.tremol.zfplibj.ZFPLib_MK(baseLib);
        }
        else if (countryAbbreviation.contains("LT"))
        {
            return new com.tremol.zfplibj.ZFPLib_LT(baseLib);
        }
        else if (countryAbbreviation.contains("MD"))
        {
            return new com.tremol.zfplibj.ZFPLib_MD(baseLib);
        }
        return baseLib;
    }

    public static void openFiscalBon(final ZFPLib fp, String bon_psw,Activity act, Resources res) throws ZFPException
    {
        switch(fp.getCountry())
        {
            case TZ:
                ReceiptParams r = new OpenRcpAlert().OpenRcpShowDialog(act, true);
                if (r.isOK())
                {
                    ((ZFPLib_TZ)fp).openFiscalBon(1, bon_psw, false, r.getReceiptType().getRcpType(), r.getCompanyName(), r.getCompanyHeadQuarters(), r.getClientTIN(), r.getCompanyAddress(), r.getCompanyPostalCity(), r.getClientVRN(), r.getSUM(), r.getVAT());
                }
                else
                {
                    throw new ZFPException(res.getString(R.string.incorrectData));
                }
                break;

            case PA:
                ((ZFPLib_PA)fp).openFiscalBon(1, bon_psw, 2);
                break;

            case MK:
                ((ZFPLib_MK)fp).openFiscalBon(1, bon_psw, false);
                break;

            default:
                fp.openFiscalBon(1, bon_psw, false, false, false);
                break;
        }
    }

    public static void programArticle(ZFPLib fp, final FiscalFuncActivity act, final Resources mRes)
    {
        switch (fp.getCountry())
        {
//            case BG:
//                ZfpProgArticleBuilder_BG.build((ZFPLib_BG) fp, act, mRes);
//                break;

            case MN:
                ZfpProgArticleBuilder_MN.build((ZFPLib_MN) fp, act, mRes);
                break;

            case MK:
                ZfpProgArticleBuilder_MK.build((ZFPLib_MK) fp, act, mRes);
                break;

            default:
                ZfpProgArticleBuilder.build(fp, act, mRes);
                break;
        }
    }

    public static StatusAdapter getStatusAdapter(ZFPStatus stat, Activity activity, Resources mRes) throws ZFPException
    {
        if(stat instanceof ZFPStatus_MN)
        {
            return ZfpStatusAdapterBuilder_MN.build((ZFPStatus_MN) stat, activity, mRes);
        }
        else if(stat instanceof ZFPStatus_TZ)
        {
            return ZfpStatusAdapterBuilder_TZ.build((ZFPStatus_TZ) stat, activity, mRes);
        }
        else
        {
            return ZfpStatusAdapterBuilder.build(stat, activity, mRes);
        }
    }
}
