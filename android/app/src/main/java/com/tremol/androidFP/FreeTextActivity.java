package com.tremol.androidFP;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tremol.zfplibj.ZFPBarcodeType;
import com.tremol.zfplibj.ZFPException;
import com.tremol.zfplibj.ZFPLib;
import com.tremol.zfplibj.ZFPLib_BG;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mincho on 23.6.2017 г..
 */

public class FreeTextActivity extends Activity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_print_text);

        final ZFPLib fp = MainAct.a.getFP();

        final Spinner cbBarcodeType = (Spinner) findViewById(R.id.cbBarcodeType);
        ArrayList<ZFPBarcodeType> list = new ArrayList<ZFPBarcodeType>(Arrays.asList(ZFPBarcodeType.values()));
        final CharSequence[] items =list.toArray(new CharSequence[list.size()]);
        final ArrayAdapter<CharSequence> barcodeAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, items);
        barcodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cbBarcodeType.setAdapter(barcodeAdapter);

        final EditText txtFreeText = (EditText) findViewById(R.id.txtFreeText);
        final EditText txtBarcode = (EditText) findViewById(R.id.txtBarcode);
        final EditText txtFreeTextEsc = (EditText) findViewById(R.id.txtFreeTextEsc);
        final CheckBox chReversed =  (CheckBox) findViewById(R.id.chReversed);
        final CheckBox chLowFont =  (CheckBox) findViewById(R.id.chLowFont);
        final CheckBox chDelayPrint =  (CheckBox) findViewById(R.id.chDelayPrint);
        txtFreeTextEsc.setText(MainAct.mRes.getString(R.string.testTextRows));
        findViewById(R.id.btnPrintBarcode).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    ZFPBarcodeType type = ZFPBarcodeType.fromString(cbBarcodeType.getSelectedItem().toString());
                    fp.printBarcode(type, txtBarcode.getText().toString(), false);
                }
                catch (Exception zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });



        findViewById(R.id.btnOpenFiscalReceipt).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    String password = fp.getOperatorInfo(1).getPassword().toString();
                    switch(fp.getCountry())
                    {
                        case BG:
                            boolean delayPrint = chDelayPrint.isChecked();
                            fp.openFiscalBon(1, password, false, false, delayPrint);
                            break;
                        default:
                            ZfpHelper.openFiscalBon(fp, password, MainAct.a, MainAct.mRes);
                            break;
                    }

                }
                catch (Exception zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btnOpenNonFiscalReceipt).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    String password = fp.getOperatorInfo(1).getPassword().toString();
                    boolean delayPrint = chDelayPrint.isChecked();
                    fp.openBon(1, password, delayPrint);
                }
                catch (ZFPException zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btnCloseFiscalReceipt).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    fp.closeFiscalBon();
                }
                catch (Exception zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btnCloseNonFiscalReceipt).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    fp.closeBon();
                }
                catch (ZFPException zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btnPrintFreeText).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                String txt = txtFreeText.getText().toString();
                try
                {
                    fp.printText(txt, ZFPLib.ZFP_TEXTALIGNLEFT);
                }
                catch (ZFPException zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btnPrintFreeTextEsc).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                String str = txtFreeTextEsc.getText().toString();
                String delimiter = "\n";
                String[] lines= str.split(delimiter);
                boolean reversed = chReversed.isChecked();
                boolean lowfont = chLowFont.isChecked();
                try
                {
                    fp.printTextESC(lines, reversed, lowfont);
                }
                catch (ZFPException zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


        findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
