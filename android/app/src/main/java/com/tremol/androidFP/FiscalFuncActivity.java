package com.tremol.androidFP;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tremol.androidFP.DBdataClients.DataClients;
import com.tremol.androidFP.DBdataClients.OpenDataClientAlert;
import com.tremol.androidFP.FiscFunctionAccordion.AccordionAdapter;
import com.tremol.androidFP.FiscFunctionAccordion.DetailChildInfo;
import com.tremol.androidFP.FiscFunctionAccordion.HeaderDetailInfo;
import com.tremol.androidFP.Status.StatusAdapter;
import com.tremol.zfplibj.ZFPCountry;
import com.tremol.zfplibj.ZFPException;
import com.tremol.zfplibj.ZFPStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;


public class FiscalFuncActivity extends Activity implements OnClickListener
{
    private ListView statusListView;
    //Accordion
    private ArrayList<HeaderDetailInfo> headers = new ArrayList<HeaderDetailInfo>();
    private AccordionAdapter accAdapter;
    private ExpandableListView accordionList;
    private LinkedHashMap<String, HeaderDetailInfo> myDepartments = new LinkedHashMap<String, HeaderDetailInfo>();
    TextView txtRes;
    Resources mRes;

    private Context mCtx = null;

    int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiscal_func);

        mCtx = this;
        mRes = getResources();

        setup();
    }

    public void setup()
    {
        statusListView = (ListView) findViewById(R.id.lvStatuses);

        getStatuses();
        txtRes = (TextView) findViewById(R.id.txtResult);

        accordion();
    }

    private int addAccodrionItem(String name, String childText)
    {
        //add
        int groupPosition = 0;
        //check hash map if the group already exist
        HeaderDetailInfo headerInfo = myDepartments.get(name);
        if(headerInfo == null)
        {
            headerInfo = new HeaderDetailInfo();
            headerInfo.setName(name);
            myDepartments.put(name, headerInfo);
            headers.add(headerInfo);
        }

        //get the children in this group
        ArrayList<DetailChildInfo> productList = headerInfo.getProductList();
        //size of the children list
        int productListSize = productList.size();
        //add to the counter
        productListSize++;

        DetailChildInfo child = new DetailChildInfo();
        child.setSequence(String.valueOf(productListSize));
        child.setName(childText);
        productList.add(child);
        headerInfo.setProductList(productList);

        //find the group inside of the list
        groupPosition = headers.indexOf(headerInfo);
        return groupPosition;
    }

    private void accordion()
    {
        //Accordion
        accordionList = (ExpandableListView) findViewById(R.id.myList);
        accAdapter = new AccordionAdapter(FiscalFuncActivity.this, headers);

        //Add items in accordion
        if(count == 1)
        {
            ZFPCountry cntr = MainAct.a.getFP().getCountry();
            //general
            addAccodrionItem(mRes.getString(R.string.general), mRes.getString(R.string.diagnostics));
            addAccodrionItem(mRes.getString(R.string.general), mRes.getString(R.string.lineFeed));
            addAccodrionItem(mRes.getString(R.string.general), mRes.getString(R.string.showVersion));
            addAccodrionItem(mRes.getString(R.string.general), mRes.getString(R.string.printLogo));
            //reports //TODO да видим кои отчети в коя държава се ползват
            addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makeXrep));
            addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makeZrep));
            addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makeArtXrep));
            if(cntr != ZFPCountry.MN) //в монтенегро артикулите се нулират с дневния отчет
            {
                addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makeArtZrep));
            }
            addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makeDepXreport));
            addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makeDepZreport));
            addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makeOperatorsXreport));
            addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makeOperatorsZreport));
            if(cntr == ZFPCountry.TZ) //долните отчети ги има само в танзания
            {
                addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makeWeeklyPurchaseReport));
                addAccodrionItem(mRes.getString(R.string.reports), mRes.getString(R.string.makePurchaseReport));
            }
            //program
            addAccodrionItem(mRes.getString(R.string.programCommands), mRes.getString(R.string.settingDT));
            addAccodrionItem(mRes.getString(R.string.programCommands), mRes.getString(R.string.programPLU));
            if(cntr == ZFPCountry.BG) //фунцкията за момента я има само за българия
            {
                addAccodrionItem(mRes.getString(R.string.programCommands), mRes.getString(R.string.clientsDB));
            }
            //reading
            addAccodrionItem(mRes.getString(R.string.readingCommands), mRes.getString(R.string.serialNumber));
            addAccodrionItem(mRes.getString(R.string.readingCommands), mRes.getString(R.string.showDT));
            addAccodrionItem(mRes.getString(R.string.readingCommands), mRes.getString(R.string.fiscalMemoryNum));
            addAccodrionItem(mRes.getString(R.string.readingCommands), mRes.getString(R.string.VATnumber));
        }

        count++;
        accordionList.setAdapter(accAdapter);
        accordionList.setOnChildClickListener(childListener);
    }

    private ExpandableListView.OnChildClickListener childListener = new ExpandableListView.OnChildClickListener()
    {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
        {
            getStatuses();

            HeaderDetailInfo header = headers.get(groupPosition);
            DetailChildInfo detailChild = header.getProductList().get(childPosition);
            String childName = detailChild.getName();

            try
            {
                if (childName.contentEquals(mRes.getString(R.string.makeZrep)))
                    MainAct.a.getFP().reportDaily(true, false);
                else if (childName.contentEquals(mRes.getString(R.string.makeXrep)))
                    MainAct.a.getFP().reportDaily(false, false);
                else if (childName.contentEquals(mRes.getString(R.string.makeDepXreport)))
                    MainAct.a.getFP().reportDepartments(false);
                else if (childName.contentEquals(mRes.getString(R.string.makeDepZreport)))
                    MainAct.a.getFP().reportDepartments(true);
                else if (childName.contentEquals(mRes.getString(R.string.makeOperatorsXreport)))
                    MainAct.a.getFP().reportOperator(false, 0);
                else if (childName.contentEquals(mRes.getString(R.string.makeOperatorsZreport)))
                    MainAct.a.getFP().reportOperator(true, 0);
                else if (childName.contentEquals(mRes.getString(R.string.makeArtZrep)))
                    MainAct.a.getFP().reportArticles(true);
                else if (childName.contentEquals(mRes.getString(R.string.makeArtXrep)))
                    MainAct.a.getFP().reportArticles(false);
                else if (childName.contentEquals(mRes.getString(R.string.makePurchaseReport)))
                    ((com.tremol.zfplibj.ZFPLib_TZ)MainAct.a.getFP()).reportPurchase();
                else if (childName.contentEquals(mRes.getString(R.string.makeWeeklyPurchaseReport)))
                    ((com.tremol.zfplibj.ZFPLib_TZ)MainAct.a.getFP()).reportWeeklyPurchase();
                else if (childName.contentEquals(mRes.getString(R.string.settingDT)))
                    MainAct.a.getFP().setDateTime(Calendar.getInstance());
                else if (childName.contentEquals(mRes.getString(R.string.showVersion)))
                    txtRes.setText(MainAct.a.getFP().getVersion());
                else if (childName.contentEquals(mRes.getString(R.string.showDT)))
                {
                    Calendar cal = MainAct.a.getFP().getDateTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
                    txtRes.setText(sdf.format(cal.getTime()));
                }
                else if (childName.contentEquals(mRes.getString(R.string.serialNumber)))
                    txtRes.setText(MainAct.a.getFP().getFactoryNumber());
                else if (childName.contentEquals(mRes.getString(R.string.fiscalMemoryNum)))
                    txtRes.setText(MainAct.a.getFP().getFiscalNumber());
                else if (childName.contentEquals(mRes.getString(R.string.VATnumber)))
                    txtRes.setText( MainAct.a.getFP().getTaxNumber());
                else if (childName.contentEquals(mRes.getString(R.string.printLogo)))
                    MainAct.a.getFP().printLogo();
                else if (childName.contentEquals(mRes.getString(R.string.diagnostics)))
                    MainAct.a.getFP().diagnostic();
                else if (childName.contentEquals(mRes.getString(R.string.lineFeed)))
                    MainAct.a.getFP().lineFeed();
                else if (childName.contentEquals(mRes.getString(R.string.programPLU)))
                    ZfpHelper.programArticle(MainAct.a.getFP(), (FiscalFuncActivity)mCtx , mRes);
                else if (childName.contentEquals(mRes.getString(R.string.clientsDB)))
                {
                    DataClients d = new OpenDataClientAlert().OpenDbDataClientsDialog(FiscalFuncActivity.this, true);
                    if (d.isOK())
                            MainAct.a.getFP().setDBdataForClients(d.getClientNo(), d.getClientName(),
                                    d.getBuyerName(), d.getZDDS(), d.getBulstat(), d.getAddress());
                }
            }
            catch (ZFPException zfpex)
            {
                Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    };

    public void getStatuses()
    {
        try
        {
            ZFPStatus stat = MainAct.a.getFP().getStatus();
            StatusAdapter adapter = ZfpHelper.getStatusAdapter(stat, (FiscalFuncActivity)mCtx, mRes);
            statusListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        catch (ZFPException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fiscal_func, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            finish();

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {

    }
}
