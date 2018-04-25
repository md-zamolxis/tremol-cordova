package com.tremol.androidFP.DBdataClients;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tremol.androidFP.MainAct;
import com.tremol.androidFP.R;
import com.tremol.zfplibj.ZFPClientsDBdata;
import com.tremol.zfplibj.ZFPException;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * Created by User on 28.10.2016 г..
 */

public class OpenDataClientAlert {

    public DataClients dataClients = new DataClients();


    public DataClients OpenDbDataClientsDialog(final Activity activity, final boolean block)
    {
        final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message mesg)
            {
                throw new RuntimeException();
            }
        };

        dataClients.setOK(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.clientsDB);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.db_clients_data , null);
        builder.setView(dialogView);

        final EditText txtClientNo = (EditText) dialogView.findViewById(R.id.txtClientNo);
        final EditText txtClientName = (EditText) dialogView.findViewById(R.id.txtClientName);
        final EditText txtBuyerName = (EditText) dialogView.findViewById(R.id.txtBuyerName);
        final EditText txtZDDS = (EditText) dialogView.findViewById(R.id.txtZDDSNo);
        final EditText txtBulstat = (EditText) dialogView.findViewById(R.id.txtBulstat);
        final EditText txtAddress = (EditText) dialogView.findViewById(R.id.txtAddress);

        dialogView.findViewById(R.id.btnread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try
                {
                    int clientNo = Integer.parseInt(txtClientNo.getText().toString());
                    if(clientNo < 0 || txtClientNo.getText().toString().contentEquals(""))
                    {
                        Toast.makeText(activity.getBaseContext(), R.string.PleaseEnterDBdata, Toast.LENGTH_LONG).show();
                        return;
                    }
                    ZFPClientsDBdata clientsDBdata = MainAct.a.getFP().getDBclientsData(clientNo);
                    txtClientName.setText(clientsDBdata.getClientName());
                    txtBuyerName.setText(clientsDBdata.getBuyerName());
                    txtZDDS.setText(clientsDBdata.getZDDS());
                    txtBulstat.setText(clientsDBdata.getBulstat());
                    txtAddress.setText(clientsDBdata.getAddress());
                }
                catch (ZFPException e)
                {
                    Toast.makeText(activity.getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        dialogView.findViewById(R.id.btnUntilEnd).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ZFPClientsDBdata clientData = null;
                try
                {
                    for(int i=1; i <=1000; i++)
                    {
                        clientData = MainAct.a.getFP().getDBclientsData(i);
                        if(clientData.getClientName().contentEquals(""))
                        {
                            txtClientNo.setText(String.valueOf(i));
                            break;
                        }
                    }
                }
                catch (ZFPException e)
                {
                    Toast.makeText(activity.getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Dialog dialog = (Dialog) dialogInterface;

                int clientNo = Integer.parseInt(txtClientNo.getText().toString());
                String clientName = txtClientName.getText().toString();
                String buyerName = txtBuyerName.getText().toString();
                String zdds = txtZDDS.getText().toString();
                String bulstat = txtBulstat.getText().toString();
                String address = txtAddress.getText().toString();

                if(txtClientNo.getText().toString().contentEquals("") ||
                        clientName.contentEquals("") ||
                        buyerName.contentEquals("") ||
                        zdds.contentEquals("") ||
                        bulstat.contentEquals("") ||
                        address.contentEquals("")) {
                    dataClients.setOK(false);
                    Toast.makeText(activity.getBaseContext(), R.string.PleaseEnterAllFields, Toast.LENGTH_LONG).show();
                }
                else
                {
                    dataClients.setClientNo(clientNo);
                    dataClients.setClientName(clientName);
                    dataClients.setBuyerName(buyerName);
                    dataClients.setZDDS(zdds);
                    dataClients.setBulstat(bulstat);
                    dataClients.setAddress(address);
                    dataClients.setOK(true);
                }
                Toast.makeText(activity.getBaseContext(), R.string.DataSaved, Toast.LENGTH_LONG).show();

                if (block)
                    handler.sendMessage(handler.obtainMessage());
                dialog.dismiss();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dataClients.setOK(false);
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

        return dataClients;
    }
}
