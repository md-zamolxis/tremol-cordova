/*
 * Main.java
 *
 */

package com.tremol.zfplibj;



/**
 * Main - sample usage if zfplibj (basic operations only)
 *
 * @author <a href="http://tremol.bg/">Tremol Ltd.</a> (Stanimir Jordanov)
 */

public class Main
{
    public Main() {}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            // main sample
            ZFPLib zfp = new ZFPLib(new ZFPPortSerial(1, 9600), "utf-8", null); // COM2 baud rate 9600
            zfp.openFiscalBon(1, "0000", false, false, false);
            zfp.sellFree("Test article", '1', 2.34f, 1.0f, 0.0f);
            zfp.sellFree("Test article2", '1', 1.0f, 3.54f, 0.0f);
            double sum = zfp.calcIntermediateSum(false, false, false, 0.0f);
            zfp.payment(sum, 0, false);
            zfp.closeFiscalBon();
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
            e.printStackTrace();
        }        
    }
}
