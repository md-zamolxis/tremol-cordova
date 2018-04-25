package com.tremol.zfplibj;

/**
 * Created by Mincho on 21.3.2017 г..
 */

public enum ZFPCountry implements CharSequence
{
    BG("Bulgaria","България",0),
    KE("Kenya","Кения",1),
    MN("Montenegro","Монтенегро",2),
    PA("Panama","Панама", 3),
    RO("Romania","Румъния", 4),
    TZ("Tanzania","Танзания", 5),
    MK("Macedonia","Македония",6),
    LT("Lithuania","Литва",7),
    MD("Moldova","Молдова",8);

    private String stringValue;
    private String stringValueBG;
    private int intValue;

    ZFPCountry(String toString, String toStringBG, int value) {
        stringValue = toString;
        stringValueBG = toStringBG;
        intValue = value;
    }

    public static ZFPCountry fromString(String text) {
        for (ZFPCountry b : ZFPCountry.values()) {
            if (b.stringValue.equalsIgnoreCase(text) ||
                b.stringValueBG.equalsIgnoreCase(text))
            {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        if(ZFPLib.getLanguage() == ZFPException.ZFP_LANG_BG)
            return stringValueBG;
        return stringValue;
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