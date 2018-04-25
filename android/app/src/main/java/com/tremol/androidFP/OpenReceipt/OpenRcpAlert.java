package com.tremol.androidFP.OpenReceipt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.tremol.androidFP.R;

/**
 * Created by User on 19.1.2016 г..
 */
public class OpenRcpAlert {

    public ReceiptParams rcpParams = new ReceiptParams();


    public ReceiptParams OpenRcpShowDialog(Activity activity, final boolean block)
    {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        rcpParams.setOK(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Open Receipt");

        //setting custom alert dialog view
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.open_receipt_alert_dialog_tz, null);
        builder.setView(dialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Dialog f = (Dialog) dialog;

                Spinner spinner = (Spinner) f.findViewById(R.id.spinnerTypeRcp);
                EditText txtCompanyName = (EditText) f.findViewById(R.id.txtCompanyName);
                EditText txtCompanyHQ = (EditText) f.findViewById(R.id.txtCompanyHqAddres);
                EditText txtClientTIN = (EditText) f.findViewById(R.id.txtTINnumber);
                EditText txtCompanyAddress = (EditText) f.findViewById(R.id.txtCompanyAddress);
                EditText txtCompanyPostalCity = (EditText) f.findViewById(R.id.txtCodeCity);
                EditText txtClientVRN = (EditText) f.findViewById(R.id.txtClientVRNnumber);
                EditText txtSUM = (EditText) f.findViewById(R.id.txtSUM);
                EditText txtVAT = (EditText) f.findViewById(R.id.txtVAT);

                int rcpType = spinner.getSelectedItemPosition();
                String companyName = txtCompanyName.getText().toString();
                String companyHQ = txtCompanyHQ.getText().toString();
                String clientTIN = txtClientTIN.getText().toString();
                String companyAddress = txtCompanyAddress.getText().toString();
                String companyCodeCity = txtCompanyPostalCity.getText().toString();
                String clientVRN = txtClientVRN.getText().toString();

                String s = txtSUM.getText().toString();
                Float sum = Float.parseFloat(s);

                String v = txtVAT.getText().toString();
                Float vat = Float.parseFloat(v);

                ReceiptParams.ReceiptType t = getType(rcpType);

                rcpParams.setReceiptType(t);
                rcpParams.setCompanyName(companyName);
                rcpParams.setCompanyHeadQuarters(companyHQ);
                rcpParams.setClientTIN(clientTIN);
                rcpParams.setCompanyAddress(companyAddress);
                rcpParams.setCompanyPostalCity(companyCodeCity);
                rcpParams.setClientVRN(clientVRN);

                rcpParams.setSUM(sum);
                rcpParams.setVAT(vat);

                rcpParams.setOK(true);


                if (block)
                    handler.sendMessage(handler.obtainMessage());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
           public void onClick(DialogInterface dialog, int id)
           {
               rcpParams.setOK(false);
               if (block)
                   handler.sendMessage(handler.obtainMessage());
               dialog.dismiss();
           }
        });

        builder.show();

        if(block)
        {
            try { Looper.loop(); }
            catch(RuntimeException e2) {}
        }
        return rcpParams;

    }

    private ReceiptParams.ReceiptType getType(int item)
    {
        ReceiptParams.ReceiptType type = ReceiptParams.ReceiptType.standartFiscal;
        switch (item)
        {
            case 1:
                type = ReceiptParams.ReceiptType.purchase;
                break;
            case 2:
                type = ReceiptParams.ReceiptType.standartFiscalPosponedPrint;
                break;
            default:
                break;
        }
        return type;

    }

}
