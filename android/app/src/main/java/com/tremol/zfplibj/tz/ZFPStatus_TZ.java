/*
 * ZFPStatus.java
 *
 */

package com.tremol.zfplibj;


/** ZFPStatus - interface is result of {@link com.tremol.zfplibj.ZFPLib#getStatus} method.
  * Contains information about the current status of Zeka FP.
  * @author <a href="http://tremol.bg/">Tremol Ltd.</a> (Stanimir Jordanov)
  */

public class ZFPStatus_TZ extends ZFPStatus
{
    /**
     * Creates a new instance of ZFPStatus
     */
    public ZFPStatus_TZ(byte[] output, int outputLen, int lang) throws ZFPException
    {
        super(output, outputLen, lang);
    }


    /**
     * Gets receipt type 1 status
     *
     * @return receipt opened type 1 when true
     */
    public boolean isFiscalRcpType1()
    {
        return (0 < (m_status[2] & 0x04)) ? true : false;
    }

    /**
     * Gets receipt type 2 status
     *
     * @return receipt opened type 2 when true
     */
    public boolean isFiscalRcpType2()
    {
        return (0 < (m_status[2] & 0x08)) ? true : false;
    }

    /**
     * Gets receipt type 3 status
     *
     * @return receipt opened type 3 when true
     */
    public boolean isFiscalRcpType3()
    {
        return (0 < (m_status[2] & 0x10)) ? true : false;
    }

    /**
     * Gets SD card near full status
     *
     * @return SD card near full when true
     */
    public boolean isSDcardNearFull()
    {
        return (0 < (m_status[2] & 0x20)) ? true : false;
    }

    /**
     * Gets SD card full status
     *
     * @return SD card full when true
     */
    public boolean isSDcardrFull()
    {
        return (0 < (m_status[2] & 0x40)) ? true : false;
    }



    public boolean isNearPaperEnd()
    {
        return (0 < (m_status[6] & 0x10)) ? true : false;
    }


    @Override
    @Deprecated
    public boolean isNoMO() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Override
    @Deprecated
    public boolean isMissingDisplay() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Override
    @Deprecated
    public boolean isModemReceiveTask() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Override
    @Deprecated
    public boolean isDetailedInfo() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }

    @Override
    @Deprecated
    public boolean isVATinfo() throws ZFPException
    {
        throw new ZFPException(0xFFF, m_lang);
    }
}