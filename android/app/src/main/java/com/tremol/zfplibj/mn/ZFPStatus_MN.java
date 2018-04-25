/*
 * ZFPStatus.java
 *
 */

package com.tremol.zfplibj;


/**
 * ZFPStatus - interface is result of {@link com.tremol.zfplibj.ZFPLib#getStatus} method.
 * Contains information about the current status of Zeka FP.
 *
 * @author <a href="http://tremol.bg/">Tremol Ltd.</a> (Stanimir Jordanov)
 */

public class ZFPStatus_MN extends ZFPStatus
{
    /**
     * Creates a new instance of ZFPStatus
     */
    public ZFPStatus_MN(byte[] output, int outputLen, int lang) throws ZFPException
    {
        super(output, outputLen, lang);
    }



    /**
     * Gets receipt format status
     *
     * @return Fiscal receipt type 1 opened when true
     */
    public boolean isFiscalRcpType1()
    {
        return (0 < (m_status[2] & 0x04)) ? true : false;
    }


    /**
     * Gets receipt format status
     *
     * @return Fiscal receipt type 2 opened when true
     */
    public boolean isFiscalRcpType2()
    {
        return (0 < (m_status[2] & 0x08)) ? true : false;
    }

    /**
     * Gets receipt format status
     *
     * @return Fiscal receipt type 3 opened when true
     */
    public boolean isFiscalRcpType3()
    {
        return (0 < (m_status[2] & 0x10)) ? true : false;
    }

    /**
     * Gets SD Card information
     *
     * @return SC card near full when true
     */
    public boolean isSDnearFull()
    {
        return (0 < (m_status[2] & 0x20)) ? true : false;
    }

    /**
     * Gets SD Card information
     *
     * @return SC card is full when true
     */
    public boolean isSDfull()
    {
        return (0 < (m_status[2] & 0x40)) ? true : false;
    }

    //ST3

    /**
     * Check Tax Memory presence
     *
     * @return missing Tax Memory when true
     */
    public boolean isMissingFiscalMemory()
    {
        return (0 < (m_status[3] & 0x01)) ? true : false;
    }

    /**
     * Get Tax Memory propriety status
     *
     * @return Wrong Tax Memory when true
     */
    public boolean isWrongFiscalMemory()
    {
        return (0 < (m_status[3] & 0x02)) ? true : false;
    }

    /**
     * Get Tax Memory load status
     *
     * @return Full Tax Memory when true
     */
    public boolean isFullFiscalMemory()
    {
        return (0 < (m_status[3] & 0x04)) ? true : false;
    }

    /**
     * Get Tax Memory load status
     *
     * @return Tax Memory nearly full when true
     */
    public boolean isFiscalMemoryLimitNear()
    {
        return (0 < (m_status[3] & 0x08)) ? true : false;
    }

    /**
     * Get decimal point status
     *
     * @return sums with decimal point when true
     */

    public boolean hasDecimalPoint()
    {
        return (0 < (m_status[3] & 0x10)) ? true : false;
    }

    /**
     * Get fiscal status
     *
     * @return Printer is Fiscalized when true
     */
    public boolean isFiscal()
    {
        return (0 < (m_status[3] & 0x20)) ? true : false;
    }

    /**
     * Get fiscal and factory number status
     *
     * @return Fiscal and factory numbers are set when true
     */
    public boolean hasFiscalAndFactoryNum()
    {
        return (0 < (m_status[3] & 0x40)) ? true : false;
    }

    //ST4

    /**
     * Get cutter status
     *
     * @return Has automatic cutter when true
     */
    public boolean hasAutoCutter()
    {
        return (0 < (m_status[4] & 0x01)) ? true : false;
    }

    /**
     * Get display status
     *
     * @return The display is transparent when true
     */
    public boolean hasTransparentDisplay()
    {
        return (0 < (m_status[4] & 0x02)) ? true : false;
    }




    /**
     * Get drawer status
     *
     * @return Auto opening cash drawer when true
     */
    public boolean isAutoOpenDrawer()
    {
        return (0 < (m_status[4] & 0x10)) ? true : false;
    }

    /**
     * Get logo in receipt status
     *
     * @return Customer logo included in the receipt when true
     */
    public boolean isLogoInReceipt()
    {
        return (0 < (m_status[4] & 0x20)) ? true : false;
    }





    @Deprecated
    @Override
    public boolean isDetailedInfo() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public boolean isVATinfo() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public int getBaud() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public boolean isMissingDisplay() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public boolean isWrongSIM() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public boolean isNoMO() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public boolean isModemReceiveTask() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public boolean isSIMmissing() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public boolean isMissingModem() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public boolean isMissingMO() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Deprecated
    @Override
    public boolean isMissingGPRS() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }
}