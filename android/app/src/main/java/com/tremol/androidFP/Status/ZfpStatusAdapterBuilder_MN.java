package com.tremol.androidFP.Status;

import android.app.Activity;
import android.content.res.Resources;

import com.tremol.androidFP.R;
import com.tremol.zfplibj.ZFPException;
import com.tremol.zfplibj.ZFPStatus_MN;

/**
 * Created by Mincho on 20.3.2017 г..
 */

public class ZfpStatusAdapterBuilder_MN extends ZfpStatusAdapterBuilder
{
    public static StatusAdapter build(ZFPStatus_MN stat, Activity activity, Resources mRes) throws ZFPException
    {
        StatusModel[] modelItems;

        modelItems = new StatusModel[32];

        modelItems[0] = createItem(mRes.getString(R.string.readOnly), stat.getReadOnlyFM() ? 1 : 0);
        modelItems[1] = createItem(mRes.getString(R.string.powerDown), stat.isPowerDown() ? 1 : 0);
        modelItems[2] = createItem(mRes.getString(R.string.overheat), stat.isPrinterOverheat() ? 1 : 0);
        modelItems[3] = createItem(mRes.getString(R.string.notSetDateTime), stat.isDateTimeNotSet() ? 1 : 0);
        modelItems[4] = createItem(mRes.getString(R.string.wrongDate), stat.isWrongDateTime() ? 1 : 0);
        modelItems[5] = createItem(mRes.getString(R.string.wrongRAM), stat.isWrongRAM() ? 1 : 0);
        modelItems[6] = createItem(mRes.getString(R.string.clockHardwareError), stat.isClockHardwareError() ? 1 : 0);
        modelItems[7] = createItem(mRes.getString(R.string.outOfPaper), stat.isPaperOut() ? 1 : 0);
        modelItems[8] = createItem(mRes.getString(R.string.reportSumOverflow), stat.isReportSumOverflow() ? 1 : 0);
        modelItems[9] = createItem(mRes.getString(R.string.blockedWithoutReport), stat.isBlocked24HoursReport() ? 1 : 0);
        modelItems[10] = createItem(mRes.getString(R.string.nonZeroDaily), stat.isNonzeroDailyReport() ? 1 : 0);
        modelItems[11] = createItem(mRes.getString(R.string.nonZeroArticleRep), stat.isNonzeroArticleReport() ? 1 : 0);
        modelItems[12] = createItem(mRes.getString(R.string.nonZeroOperatorRep), stat.isNonzeroOperatorReport() ? 1 : 0);
        modelItems[13] = createItem(mRes.getString(R.string.duplicate), stat.isDuplicateNotPrinted() ? 1 : 0);
        modelItems[14] = createItem(mRes.getString(R.string.officialBon), stat.isOpenOfficialBon() ? 1 : 0);
        modelItems[15] = createItem(mRes.getString(R.string.fiscalBon), stat.isOpenFiscalBon() ? 1 : 0);
        modelItems[16] = createItem(mRes.getString(R.string.fiscRcpType1), stat.isFiscalRcpType1() ? 1 : 0);
        modelItems[17] = createItem(mRes.getString(R.string.fiscRcpType2), stat.isFiscalRcpType2() ? 1 : 0);
        modelItems[18] = createItem(mRes.getString(R.string.fiscRcpType3), stat.isFiscalRcpType3() ? 1 : 0);
        modelItems[19] = createItem(mRes.getString(R.string.SDNearFull), stat.isSDnearFull() ? 1 : 0);
        modelItems[20] = createItem(mRes.getString(R.string.SDFull), stat.isSDfull() ? 1 : 0);
        modelItems[21] = createItem(mRes.getString(R.string.missingFM), stat.isMissingFiscalMemory() ? 1 : 0);
        modelItems[22] = createItem(mRes.getString(R.string.wrongFM), stat.isWrongFiscalMemory() ? 1 : 0);
        modelItems[23] = createItem(mRes.getString(R.string.fullFM), stat.isFullFiscalMemory() ? 1 : 0);
        modelItems[24] = createItem(mRes.getString(R.string.nearFullFM), stat.isFiscalMemoryLimitNear() ? 1 : 0);
        modelItems[25] = createItem(mRes.getString(R.string.decimalPoint), stat.hasDecimalPoint() ? 1 : 0);
        modelItems[26] = createItem(mRes.getString(R.string.isFiscal), stat.isFiscal() ? 1 : 0);
        modelItems[27] = createItem(mRes.getString(R.string.fiscFactoryNum), stat.hasFiscalAndFactoryNum() ? 1 : 0);
        modelItems[28] = createItem(mRes.getString(R.string.autoCut), stat.hasAutoCutter() ? 1 : 0);
        modelItems[29] = createItem(mRes.getString(R.string.transparentDisplay), stat.hasTransparentDisplay() ? 1 : 0);
        modelItems[30] = createItem(mRes.getString(R.string.autoOpenDrawer), stat.isAutoOpenDrawer() ? 1 : 0);
        modelItems[31] = createItem(mRes.getString(R.string.printLogo), stat.isLogoInReceipt() ? 1 : 0);

        return new StatusAdapter(activity, modelItems);
    }
}

