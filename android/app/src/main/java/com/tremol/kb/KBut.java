package com.tremol.kb;

import java.util.HashMap;
import java.util.Map;

public enum KBut
{
	CL("CL", KCat.CLEAR),
	QTY("QTY", KCat.OPERATOR),
	VD("VD", KCat.OPERATOR),
	PRC_PLUS("%+", KCat.OPERATOR),
	PRC_MINUS("%-", KCat.OPERATOR),
	TX_PLUS("Tx-", KCat.OPERATOR),
	TX_MINUS("Tx+", KCat.OPERATOR),
	MODE("MODE", KCat.OPERATOR),
	PY("PY", KCat.OPERATOR),
	ST("ST", KCat.OPERATOR),

	ZERO("0", KCat.NUMBER),
	ONE("1", KCat.NUMBER),
	TWO("2", KCat.NUMBER),
	THREE("3", KCat.NUMBER),
	FOUR("4", KCat.NUMBER),
	FIVE("5", KCat.NUMBER),
	SIX("6", KCat.NUMBER),
	SEVEN("7", KCat.NUMBER),
	EIGHT("8", KCat.NUMBER),
	NINE("9", KCat.NUMBER),

	DOT(".", KCat.DOT),

	PLU("PLU", KCat.RESULT),
	TOTAL("TOTAL", KCat.RESULT),
	D("D", KCat.RESULT);


	private static final Map<String, KBut> map;

	static
	{
		map = new HashMap<String, KBut>();
		for (KBut b : KBut.values())
			map.put(b.txt.toString(), b);
	}

	CharSequence txt;
	KCat cat;

	KBut(CharSequence text, KCat category)
	{
		txt = text;
		cat = category;
	}

	public static KBut get(String str)
	{
		//return KBut.valueOf(str);
		return map.get(str);
	}

	public CharSequence getText()
	{
		return txt;
	}

	public KCat getKCat()
	{
		return this.cat;
	}
}