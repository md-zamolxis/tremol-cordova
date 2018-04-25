package com.tremol.androidFP.FiscFunctionAccordion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.tremol.androidFP.R;

import java.util.ArrayList;

public class AccordionAdapter extends BaseExpandableListAdapter
{
    private Context context;
    private ArrayList<HeaderDetailInfo> deptList;

    public AccordionAdapter(Context context, ArrayList<HeaderDetailInfo> deptList)
    {
        this.context = context;
        this.deptList = deptList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        ArrayList<DetailChildInfo> productList = deptList.get(groupPosition).getProductList();
        return productList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent)
    {
        DetailChildInfo detailInfo = (DetailChildInfo) getChild(groupPosition, childPosition);
        if (view == null)
        {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.child_row, null);
        }

        TextView childItem = (TextView) view.findViewById(R.id.childItem);
        childItem.setText(detailInfo.getName().trim());

        //TextView seq = (TextView) view.findViewById(R.id.sequence);
        //seq.setText(detailInfo.getSequence() + ". ");

        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        ArrayList<DetailChildInfo> productList = deptList.get(groupPosition).getProductList();
        return productList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return deptList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return deptList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent)
    {
        HeaderDetailInfo headerInfo = (HeaderDetailInfo) getGroup(groupPosition);
        if (view == null)
        {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.header_row, null);
        }

        TextView heading = (TextView) view.findViewById(R.id.heading);
        heading.setText(headerInfo.getName().trim());

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
