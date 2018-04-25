package com.tremol.zfplibj;

/**
 * Created by Mincho on 22.6.2017 г..
 */

public enum ZFPBarcodeType implements CharSequence
{
    UPCA("UPC-A", 'A', null),
    UPCE("UPC-E", 'B', null),
    EAN13("EAN 13", 'C', null),
    EAN8("EAN 8", 'D', null),
    CODE39("CODE 39", 'E', null),
    ITF("ITF", 'F', null),
    CODABAR("CODABAR", 'G', null),
    CODE93("CODE 93", 'H', null),
    CODE128A("CODE 128A", 'I', 'g'),
    CODE128B("CODE 128B", 'I', 'h'),
    CODE128C("CODE 128C", 'I', 'i');

    private String stringValue;
    private char barcodeType;
    private Character startingDataChar;

    ZFPBarcodeType(String string, char barcodetype, Character startingdatachar )
    {
        stringValue = string;
        barcodeType = barcodetype;
        startingDataChar = startingdatachar;
    }


    public static ZFPBarcodeType fromString(String text)
    {
        for (ZFPBarcodeType b : ZFPBarcodeType.values())
        {
            if (b.stringValue.equalsIgnoreCase(text))
            {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return stringValue;
    }

    public char getBarcodeType()
    {
        return barcodeType;
    }

    public String getDataStartingString()
    {
        if(startingDataChar == null)
            return "";
        return startingDataChar.toString();
    }



    @Override
    public char charAt(int arg0)
    {
        return this.toString().charAt(arg0);
    }

    @Override
    public int length()
    {
        return this.toString().length();
    }

    @Override
    public CharSequence subSequence(int arg0, int arg1)
    {
        return this.toString().subSequence(arg0, arg1);
    }
}
