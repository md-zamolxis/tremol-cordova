package com.tremol.androidFP.Status;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tremol.androidFP.R;

public class StatusAdapter extends ArrayAdapter<StatusModel>
{
    StatusModel[] modelItems = null;
    Context context;

    public StatusAdapter(Context contex, StatusModel[] resourse)
    {
        super(contex, R.layout.statuses, resourse);
        this.context = contex;
        this.modelItems = resourse;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.statuses, parent, false);
        TextView name = (TextView) convertView.findViewById(R.id.lblItem);
        CheckBox cb = (CheckBox) convertView.findViewById(R.id.chBoxItem);
        name.setText(modelItems[position].getName());

        if(modelItems[position].getValue() == 1)
            cb.setChecked(true);
        else
            cb.setChecked(false);

        return convertView;
    }

}
