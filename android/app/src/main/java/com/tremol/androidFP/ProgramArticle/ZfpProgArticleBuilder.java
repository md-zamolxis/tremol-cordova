package com.tremol.androidFP.ProgramArticle;

import android.content.res.Resources;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tremol.androidFP.MainAct;
import com.tremol.androidFP.R;
import com.tremol.androidFP.FiscalFuncActivity;
import com.tremol.zfplibj.ZFPArticle;
import com.tremol.zfplibj.ZFPException;
import com.tremol.zfplibj.ZFPLib;

import FPData.FPInfo;

/**
 * Created by Mincho on 20.3.2017 г..
 */

public class ZfpProgArticleBuilder
{
    public static void build(final ZFPLib fp, final FiscalFuncActivity act, final Resources mRes)
    {
        act.setContentView(R.layout.program_article);
        //fill dropdown menu
        final Spinner vatClass = (Spinner) act.findViewById(R.id.txtPLUvat);
        final ArrayAdapter<CharSequence> VATadapter = ArrayAdapter.createFromResource(act.getBaseContext(), R.array.tg_arr, android.R.layout.simple_spinner_item);
        VATadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vatClass.setAdapter(VATadapter);

        final EditText txtPluNum = (EditText) act.findViewById(R.id.txtPLUnumber);
        final EditText txtPluName = (EditText) act.findViewById(R.id.txtPLUname);
        final EditText txtPluPrice = (EditText) act.findViewById(R.id.txtPLUprice);
        final EditText txtPluDepNo = (EditText) act.findViewById(R.id.txtPLUdepNo);
        txtPluDepNo.setText("0");

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
                    if (numStr.length() == 0)
                    {
                        throw new ZFPException(mRes.getString(R.string.MissingNumber));
                    }
                    ZFPArticle art = fp.getArticleInfo(Integer.parseInt(numStr));
                    txtPluName.setText(art.getName());
                    txtPluPrice.setText(Double.toString(art.getPrice()));
                    txtPluDepNo.setText(Integer.toString(art.getDepartmentNumber()));
                    vatClass.setSelection(VATadapter.getPosition(String.valueOf(art.getTaxGroup())));
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
                String PLUnumber = txtPluNum.getText().toString();
                String PLUname = txtPluName.getText().toString();
                String PLUprice = txtPluPrice.getText().toString();
                String PLUdepNo = txtPluDepNo.getText().toString();
                String PLUvat = vatClass.getSelectedItem().toString();

                if (!PLUname.contentEquals("") && !PLUnumber.contentEquals("") && !PLUprice.contentEquals(""))
                {
                    try
                    {
                        int num = Integer.parseInt(PLUnumber);
                        fp.setArticleInfo(num, PLUname, Float.valueOf(PLUprice), PLUvat.charAt(0), Integer.parseInt(PLUdepNo));
                        MainAct.a.refreshArticle(num);
                    }
                    catch (ZFPException zfpex)
                    {
                        Toast.makeText(act.getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(act.getBaseContext(), mRes.getString(R.string.incorrectData), Toast.LENGTH_LONG).show();
                }
                act.setContentView(R.layout.activity_fiscal_func);
                act.setup();
            }
        });
    }
}
