package com.tremol.androidFP.ProgramArticle;

import android.content.res.Resources;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tremol.androidFP.FiscalFuncActivity;
import com.tremol.androidFP.MainAct;
import com.tremol.androidFP.R;
import com.tremol.zfplibj.ZFPArticle_MK;
import com.tremol.zfplibj.ZFPException;
import com.tremol.zfplibj.ZFPLib_MK;

import FPData.FPInfo;

/**
 * Created by Mincho on 29.3.2017 г..
 */

public class ZfpProgArticleBuilder_MK
{
    public static void build(final ZFPLib_MK fp, final FiscalFuncActivity act, final Resources mRes)
    {
        act.setContentView(R.layout.program_article_mk);
        //fill dropdown menu
        final Spinner spFlagPrice = (Spinner) act.findViewById(R.id.txtPLUflagPrice);
        final Spinner spFlagQTY = (Spinner) act.findViewById(R.id.txtPLUflagQTY);
        final Spinner spFlagOrigin = (Spinner) act.findViewById(R.id.txtPLUflagOrigin);


        final ArrayAdapter<CharSequence> adapterFlagPrice = ArrayAdapter.createFromResource(act.getBaseContext(), R.array.flagsPrice, android.R.layout.simple_spinner_item);
        final ArrayAdapter<CharSequence> adapterFlagQty = ArrayAdapter.createFromResource(act.getBaseContext(), R.array.flagsQty, android.R.layout.simple_spinner_item);
        final ArrayAdapter<CharSequence> adapterFlagOrigin = ArrayAdapter.createFromResource(act.getBaseContext(), R.array.flagsOrigin, android.R.layout.simple_spinner_item);

        adapterFlagPrice.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterFlagQty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterFlagOrigin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spFlagPrice.setAdapter(adapterFlagPrice);
        spFlagQTY.setAdapter(adapterFlagQty);
        spFlagOrigin.setAdapter(adapterFlagOrigin);

        final EditText txtPluNum = ((EditText) act.findViewById(R.id.txtPLUnumber));
        final EditText txtPluName = ((EditText) act.findViewById(R.id.txtPLUname));
        final EditText txtPluPrice = ((EditText) act.findViewById(R.id.txtPLUprice));
        final EditText txtPluDepNum = ((EditText) act.findViewById(R.id.txtPLUdepNo));
        final EditText txtPluQty = ((EditText) act.findViewById(R.id.txtPLUavailQTY));
        final EditText txtPluBarcode = ((EditText) act.findViewById(R.id.txtPLUbarcode));


        act.findViewById(R.id.btnFirstAvailable).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    txtPluNum.setText(Integer.toString(FPInfo.get().getArticlesCount() + 1));
                }
                catch (Exception zfpex)
                {
                    Toast.makeText(act.getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        act.findViewById(R.id.btnRead).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    String numStr = txtPluNum.getText().toString();
                    if(numStr.length() == 0)
                    {
                        throw new ZFPException(mRes.getString(R.string.MissingNumber));
                    }
                    ZFPArticle_MK art = fp.getArticleInfo(Integer.parseInt(numStr));
                    txtPluName.setText(art.getName());
                    txtPluPrice.setText(Double.toString(art.getPrice()));
                    txtPluDepNum.setText(Integer.toString(art.getDepNo()));
                    spFlagPrice.setSelection(art.getFlagPrice());
                    spFlagQTY.setSelection(art.getFlagQTY());
                    spFlagOrigin.setSelection(art.getFlagOrigin());
                    txtPluBarcode.setText(art.getBarcode());
                    txtPluQty.setText(Float.toString(art.getAvailableQuantity()));
                }
                catch (ZFPException zfpex)
                {
                    Toast.makeText(act.getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
        act.findViewById(R.id.btnProgram).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    int num = Integer.parseInt(txtPluNum.getText().toString());
                    fp.setArticleInfo(num,
                            txtPluName.getText().toString(),
                            Float.parseFloat(txtPluPrice.getText().toString()),
                            Integer.parseInt(txtPluDepNum.getText().toString()),
                            spFlagPrice.getSelectedItemPosition(),
                            spFlagQTY.getSelectedItemPosition(),
                            spFlagOrigin.getSelectedItemPosition(),
                            txtPluBarcode.getText().toString(),
                            Float.parseFloat(txtPluQty.getText().toString())
                    );
                    FPInfo.get().refreshArticle(fp, num);
                    MainAct.a.refreshArticle(num);
                }
                catch (ZFPException zfpex)
                {
                    Toast.makeText(act.getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
                act.setContentView(R.layout.activity_fiscal_func);
                act.setup();
            }
        });
    }

}

