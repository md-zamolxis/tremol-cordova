package com.tremol.androidFP;
////////////////
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tremol.androidFP.Diags.DUtil;
import com.tremol.androidFP.Diags.DUtil.DRES;
import com.tremol.androidFP.Server.FileServer;
import com.tremol.androidFP.Server.ServerException;
import com.tremol.kb.KBut;
import com.tremol.zfplibj.ZFPArticle;
import com.tremol.zfplibj.ZFPClientsDBdata;
import com.tremol.zfplibj.ZFPCountry;
import com.tremol.zfplibj.ZFPException;
import com.tremol.zfplibj.ZFPLib;
import com.tremol.zfplibj.ZFPLib_MK;
import com.tremol.zfplibj.ZFPStatus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import FPData.FPInfo;



public class MainAct extends Activity implements OnClickListener
{
    // Debugging
    private static final String TAG = "BT test";
    private static final boolean D = true;
    //MODE TYPE
    public static int APPMODE = 1;
    public static final int APPMODE_NORMAL = 1;
    public static final int APPMODE_SERVICE = 2;
    public static final int APPMODE_PRINT_DEMO = 3;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private TextView mTitle;
    private ListView mDisplayView;
    private ListView mArticlesView;
    private EditText mOutEditText;
    private Button mSendButton;
    private ProgressBar mProgBar;

    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mAADisplay;
    private ArrayAdapter<ZFPArticle> mAAArticles = null;


    public static Resources mRes = null;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BtService mChatService = null;

    private FileServer mFServer = null;
    private Bon mBon = null;

    int counter = 0;

    public static MainAct a = null;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        a = this;
        super.onCreate(savedInstanceState);
        if (D)
            Log.e(TAG, "+++ ON CREATE +++");
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mRes = getResources();

        if (mBluetoothAdapter == null)
        {
            Toast.makeText(this, mRes.getString(R.string.bt_not_enabled_leaving), Toast.LENGTH_LONG).show();
            //!!!!!
            exit();
            return;
        }

        Resources res = getApplicationContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();

        if (APPMODE != APPMODE_SERVICE)
            conf.locale = res.getConfiguration().locale;
        else
            conf.locale = new Locale("bg");

        res.updateConfiguration(conf, dm);

        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        a.registerReceiver(pairingRequest, filter2);
    }


    private final BroadcastReceiver pairingRequest = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST"))
            {
                try
                {
                    try
                    {

                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        byte[] pin = (byte[]) BluetoothDevice.class.getMethod("convertPinToBytes", String.class).invoke(BluetoothDevice.class, "1234");
                        device.getClass().getMethod("setPin", byte[].class).invoke(device, pin);
                        device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                    }
                    catch (Exception ee)
                    {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        byte[] pin = (byte[]) BluetoothDevice.class.getMethod("convertPinToBytes", String.class).invoke(BluetoothDevice.class, "0000");
                        device.getClass().getMethod("setPin", byte[].class).invoke(device, pin);
                        device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                    }
                }
                catch (Exception e)
                {
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                }
            }
        }
    };



    void exit()
    {
        //int pid = android.os.Process.myPid();
        //android.os.Process.killProcess(pid);
        finish();
        System.exit(0);
    }

    @Override
    public void onBackPressed()
    {
        if (DUtil.confirm(a, mRes.getString(R.string.exit), "", true) != DRES.Ok)
        {
            moveTaskToBack(true);
            //exit();
        }
        else
        {
            super.onBackPressed();
            exit();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");
        //!!!
        if (!mBluetoothAdapter.isEnabled()) // If BT is not on, request that it be enabled. setup will then be called during onActivityResult
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else
        { // Otherwise, setup the chat session

            if (mChatService == null)
                setup();
        }
        if (MainAct.APPMODE == MainAct.APPMODE_SERVICE)
            setServiceTestMode();
    }

    @Override
    public synchronized void onResume()
    {
        super.onResume();
        if (D)
            Log.e(TAG, "+ ON RESUME +");


        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity
        // returns.
        //if (mChatService != null)
        //{
        // Only if the state is STATE_NONE, do we know that we haven't
        // started already
        //	if (mChatService.getState() == BtService.STATE_NONE)
        //	{
        // Start the Bluetooth chat services
        // mChatService.start();
        //	}
        //}
    }

    public static boolean setup_done = false;

    private void setup()
    {
        if (setup_done)
            return;

        Log.d(TAG, "setup()");

        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

        mChatService = new BtService(this, mHandler);
        mBon = new Bon(this);

        mAADisplay = new ArrayAdapter<String>(this, R.layout.message);
        mDisplayView = (ListView) findViewById(R.id.lvDisplay);
        mDisplayView.setAdapter(mAADisplay);

        mArticlesView = (ListView) findViewById(R.id.lvItems);
        mArticlesView.setVisibility(View.GONE);

        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        mOutEditText.setInputType(InputType.TYPE_NULL);

        mProgBar = (ProgressBar) findViewById(R.id.progressBar1);
        mProgBar.setVisibility(View.GONE);

        mSendButton = (Button) findViewById(R.id.button_send);


        mArticlesView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int pos, long mylng)
            {
                ZFPArticle selected = (ZFPArticle) (mArticlesView.getItemAtPosition(pos));
                mArticlesView.setTag(selected);
                processInput(mArticlesView);
            }
        });
        setup_done = true;
    }

    @Override
    public synchronized void onPause()
    {
        super.onPause();
        if (D)
            Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();
        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
    }

	/*Not used
	private void ensureDiscoverable()
	{
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
		{
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}*/

    private boolean checkBTConn()
    {
        if (mChatService.getState() != BtService.STATE_CONNECTED)
        {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public ZFPLib getFP()
    {
        return mChatService.zfplib;
    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener()
    {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event)
        {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP)
            {
                String message = view.getText().toString();
                try
                {
                    sendFPDirect(message);
                }
                catch (ZFPException e)
                {
                    e.printStackTrace();
                }
            }
            if (D)
                Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1)
                    {
                        case BtService.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(mConnectedDeviceName);
                            mAADisplay.clear();
                            if (APPMODE == APPMODE_SERVICE)
                            {
                                break;
                            }

                            checkZfpCountry();
                            loadArticles(true, true);
                            loadDeps();
                            checkOpenedReceipt();
                            refreshMenus();
                            if (APPMODE == APPMODE_PRINT_DEMO)
                            {
                                startActivity(new Intent(a, FreeTextActivity.class));
                            }
                            break;
                        case BtService.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BtService.STATE_LISTEN:
                        case BtService.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mAADisplay.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mAADisplay.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), mRes.getString(R.string.connected_to) + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        try
        {
            if (D)
                Log.d(TAG, "onActivityResult " + resultCode);
            switch (requestCode)
            {
                case REQUEST_CONNECT_DEVICE:// When DeviceListActivity returns with a device to connect
                    if (resultCode == Activity.RESULT_OK)
                    {
                        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                        String usbName = data.getExtras().getString(DeviceListActivity.EXTRA_USB_DEVICE_NAME);
                        if(!TextUtils.isEmpty(address))
                        {
                            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                            mChatService.connect(device);
                        }
                        else if(!TextUtils.isEmpty(usbName))
                        {
                            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                            UsbDevice deviceConnected = deviceList.get(usbName);
                            UsbDeviceConnection conn = manager.openDevice(deviceConnected);
                            mChatService.connectUsb(deviceConnected, conn);
                        }
                    }
                    break;
                case REQUEST_ENABLE_BT:    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK)
                        setup();
                    else
                    {
                        Log.d(TAG, "BT not enabled");
                        Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                        exit();
                    }
                    break;
            }
        }
        catch (Exception ex)
        {
            info(ex.getMessage());
        }
    }

    MenuItem itemInvoice;
    MenuItem itemStorno;

    public void refreshMenus()
    {
        if (mChatService != null && getFP() != null && itemInvoice != null && itemStorno != null)
        {
            if (getFP().getCountry() == ZFPCountry.BG)
                itemInvoice.setVisible(true);
            else if (getFP().getCountry() == ZFPCountry.MK)
                itemStorno.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        itemInvoice = menu.findItem(R.id.openInvoice);
        itemStorno = menu.findItem(R.id.openStorno);
        refreshMenus();
        super.onCreateOptionsMenu(menu); //TODO fixLenovoBuf
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        try
        {
            switch (item.getItemId())
            {
                case R.id.scan:
                    openScanDialog();
                    return true;
                //case R.id.discoverable:
                //ensureDiscoverable();
                //return true;
                //case R.id.FULoadArticles:
                //loadArticles(false);
                //return true;

                //case R.id.FULoadDeps:
                //loadArticles(false);
                //return true;
                case R.id.FUInfo:
                    if (checkBTConn())
                        DUtil.confirm(this, mRes.getString(R.string.conn_fp_info),
                                FPInfo.get().getInfo(getFP(), true, this), false);
                    return true;

                case R.id.clearLog:
                    mAADisplay.clear();
                    return true;

                case R.id.freeText:
                    if (!checkBTConn())
                    {
                        return false;
                    }
                    startActivity(new Intent(a, FreeTextActivity.class));
                    //ZfpPrintFreeTextBuilder.build(MainAct.a.getFP(), a , mRes);
                    return true;

                case R.id.cancelReceipt:
                    if (!checkBTConn())
                    {
                        return false;
                    }
                    DRES r = DUtil.confirm(this, mRes.getString(R.string.confirm_pls), mRes.getString(R.string.cancel), true);
                    if (DRES.Ok == r)
                    {
                        getFP().sendCommand((byte) 0x39, null);
                        mBon = new Bon(this);
                        mAADisplay.clear();
                    }
                    return true;

                case R.id.openInvoice:
                    if (!checkBTConn())
                    {
                        return false;
                    }
                    CharSequence s = DUtil.choose(this, mRes.getString(R.string.chooseClient), "", FPInfo.get().getClients(getFP(), false, this), true, true);
                    String num = s.toString().substring(0, s.toString().indexOf("."));
                    String bon_psw = getFP().getOperatorInfo(1).getPassword();
                    ZFPClientsDBdata c = getFP().getDBclientsData(Integer.parseInt(num));
                    getFP().openInvoice(1, bon_psw, c.getClientName(), c.getBuyerName(), c.getZDDS(), c.getBulstat(), c.getAddress());
                    mBon.opened = true;
                    return true;

                case R.id.openStorno:
                    if (!checkBTConn())
                    {
                        return false;
                    }
                    String bon_psw_st = getFP().getOperatorInfo(1).getPassword();
                    ((ZFPLib_MK) getFP()).openStornoBon(1, bon_psw_st, false);
                    mBon.opened = true;
                    return true;

                case R.id.directSend:
                    if (!checkBTConn())
                    {
                        return false;
                    }
                    CharSequence s1 = DUtil.input(this, mRes.getString(R.string.directCmd), mRes.getString(R.string.command), true);
                    sendFPDirect(s1.toString());
                    return true;
				/*
				case R.id.Zrep:
					getFP().reportDaily(true, false);
					return true;
					*/
                case R.id.freeSale:
                    if (!checkBTConn())
                    {
                        return false;
                    }
                    mBon.add(KBut.D);
                    mBon.add(KBut.ZERO);
                    mAADisplay.add(mBon.getCurToLog());
                    mOutEditText.setText("");
                    return true;

                case R.id.depSale:
                    if (!checkBTConn())
                    {
                        return false;
                    }
                    CharSequence ss = DUtil.choose(this, mRes.getString(R.string.chooseDep), "", FPInfo.get().getDeps(getFP(), false, this), true);
                    if (ss != null)
                    {
                        //SaleInfo si=mBon.getCurSI();
                        //if(si.qty==0) mBon.add(KBut.ONE);
                        //mBon.add( Integer.parseInt(""+s.charAt(0)));
                        mBon.add(KBut.D);
                        mBon.add(KBut.get("" + ss.charAt(0)));
                        mOutEditText.setText("");
                        mAADisplay.add(mBon.getCurToLog());
                    }
                    return true;

                case R.id.exit:
                    exit();
                    return true;

		/*		case R.id.testConfirm:
					DRES fs=DUtil.confirm(this,"������������","�� �� ������ �� �������� ��������",true);
					Toast.makeText(this,fs.toString(), Toast.LENGTH_SHORT).show();
					return true;
				case R.id.test_choose:
					CharSequence cs=DUtil.choose(this,"�������� �������","",
							FPInfo.get().getArticles(getFP(),false, false,this),true);
					Toast.makeText(this,cs, Toast.LENGTH_SHORT).show();
		*/
                case R.id.test_FSERV_start:
                    try
                    {
                        if (!checkBTConn())
                            return false;

                        if (mFServer == null)
                            mFServer = new FileServer(getFP());

                        //mFServer.stop();
                        mFServer.start();
                    }
                    catch (ServerException se)
                    {
                        info(mRes.getString(R.string.errorServer) + se.getMessage());
                    }
                    catch (Exception e)
                    {
                        info(mRes.getString(R.string.unknownErr) + e.getMessage());
                    }
                    return true;


                case R.id.test_FSERV_stop:
                    try
                    {
                        if (mFServer != null)
                        {
                            mFServer.stop();
                        }
                    }
                    catch (Exception e)
                    {
                        info(mRes.getString(R.string.unknownErr) + e.getMessage());
                    }
                    return true;

                case R.id.test_FSERV_create_freeSales:
                    if (mFServer == null)
                    {
                        info(mRes.getString(R.string.ServNotOpen));
                        return false;
                    }
                    try
                    {
                        mFServer.createSellFreeFiles();
                    }
                    catch (Exception e)
                    {
                        info(mRes.getString(R.string.unknownErr) + e.getMessage());
                    }
                    return true;

                case R.id.Cmds:
                    if (checkBTConn())
                    {
                        Intent intent = new Intent(this, FiscalFuncActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Log.d(TAG, "BT not enabled");
                        Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    }
            }
        }
        catch (Exception e)
        {
            if (!checkBTConn())
            {
                beep();
            }
            else
            {
                e.printStackTrace();
                info(mRes.getString(R.string.err) + e.getMessage());
            }
        }
        return false;
    }


    void openScanDialog()
    {
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }


    void info(CharSequence s)
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    void checkZfpCountry()
    {
        if (checkBTConn())
        {
            if (getFP().getClass().equals(ZFPLib.class))
            {
                String locale = a.getResources().getConfiguration().locale.getCountry();
                mChatService.zfplib = ZfpHelper.getSpecificLib(locale, getFP());
            }
            if (getFP().getClass().equals(ZFPLib.class))
            {
                ArrayList<ZFPCountry> list = new ArrayList<ZFPCountry>(Arrays.asList(ZFPCountry.values()));
                CharSequence rr = DUtil.choose(a, mRes.getString(R.string.unable_to_recognize_country), "", list, true);
                String cntr = ZFPCountry.fromString(rr.toString()).name();
                mChatService.zfplib = ZfpHelper.getSpecificLib(cntr, getFP());
            }
        }
    }

    public void loadArticles(boolean forceReload, boolean ten_max)
    {
        if (checkBTConn())
        {
            ArrayList<ZFPArticle> articles = FPInfo.get().getArticles(getFP(),forceReload, ten_max, this);
            mArticlesView.setVisibility(View.VISIBLE);
            mAAArticles = new ArrayAdapter<ZFPArticle>(this, R.layout.message, articles);
            mArticlesView.setAdapter(mAAArticles);
        }
    }

    public void refreshArticle(int num)
    {
        if (checkBTConn())
        {
            FPInfo.get().refreshArticle(getFP(), num);
            loadArticles(false, true);
        }

    }

    void loadDeps()
    {
        if (checkBTConn())
            FPInfo.get().getDeps(getFP(), true, this);
    }

    void checkOpenedReceipt()
    {
        if (checkBTConn())
        {
            try
            {
                ZFPStatus st = getFP().getStatus();
                if (st.isOpenFiscalBon())
                {
                    List<CharSequence> list = new ArrayList<CharSequence>();
                    list.add(mRes.getString(R.string.cancelReceipt));
                    list.add(mRes.getString(R.string.closeReceiptInCash));

                    if(DUtil.choose(this, mRes.getString(R.string.conf_open_rec_completion), "",  list ,true)
                            == mRes.getString(R.string.closeReceiptInCash))
                    {
                        getFP().closeFiscalBonWithAutoPayment();
                    }
                    else
                    {
                        getFP().cancelFiscalBon();
                    }
                }
                else if(st.isOpenOfficialBon())
                {
                    //if(DUtil.confirm(a, mRes.getString(R.string.confirmation), mRes.getString(R.string.conf_open_rec_completion), true) == DRES.Ok)
                        getFP().closeBon();
                }
            }
            catch(Exception ex)
            {}
        }
    }


    //	void cl()
    //	{
    //		String txt = mOutEditText.getText().toString();
    //		if (txt.length() != 0)
    //		{
    //			txt = txt.substring(0, txt.length() - 1);
    //			mOutEditText.setText(txt);
    //			mOutEditText.setSelection(txt.length());
    //		}
    //	}

    void beep()
    {
        try
        {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view)
    {
        //mProgBar.setVisibility(View.VISIBLE);
        processInput(view);
        //mProgBar.setVisibility(View.GONE);
    }

    private void processInput(View view)
    {
        if (!checkBTConn())
        {
            beep();
            return;
        }
        try
        {
            switch (view.getId())
            {
                case R.id.exit:
                    exit();
                    return;
                case R.id.button_send:
                    sendFPDirect(mOutEditText.getText().toString());
                    return;
            }
            Object obj = view.getTag();
            mBon.setFP(mChatService.zfplib);
            if (obj instanceof String)
                mBon.add(KBut.get(obj.toString()));
            else if (obj instanceof ZFPArticle)
            {
                //DRES r=DUtil.confirm(this,mRes.getString(R.string.confirm_pls),((ZFPArticle)o).toString()+" ?",true);
                DRES r = DRES.Ok;
                if (DRES.Ok == r)
                    mBon.add((ZFPArticle) obj);
                else
                    mBon.setCurToLog(null);
            }
            else
                throw new Exception("view tag");

        }
        catch (Exception e)
        {
            beep();
            info(mRes.getString(R.string.err) + e.getMessage());
            //mAADisplay.add("Err:  " + e.getMessage());
            return;
        }
        finally
        {
            if (mBon.getCurView() != null)
                mOutEditText.setText(mBon.getCurView());
            if (mBon.getCurMsg() != null)
                info(mBon.getCurMsg());
            if (mBon.getCurToLog() != null)
                mAADisplay.add(mBon.getCurToLog());
        }
    }

    private void sendFPDirect(final String cmd) throws ZFPException
    {
        try
        {
            if (!checkBTConn())
                return;
            if (cmd.length() == 0)
                return;
            ZFPLib fp = getFP();
            fp.sendCommand(cmd);

            mOutEditText.setText(fp.getStringResult());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void setServiceTestMode()
    {

        setContentView(R.layout.test_device);

        findViewById(R.id.btnXreport).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    getFP().reportDaily(false, false);
                }
                catch (Exception zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btnDiagnostics).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    getFP().diagnostic();
                }
                catch (Exception zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btnFeed).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    getFP().lineFeed();
                }
                catch (Exception zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btnPrintLogo).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    getFP().printLogo();
                }
                catch (Exception zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.btnScan).setOnClickListener(new View.OnClickListener()
        {
            @Override
            //On click function
            public void onClick(View view)
            {
                try
                {
                    openScanDialog();
                }
                catch (Exception zfpex)
                {
                    Toast.makeText(getBaseContext(), zfpex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void sell() throws ZFPException, UnsupportedEncodingException
    {
        if (!checkBTConn())
            return;

        ZFPLib fp = getFP();
        String bon_psw;
        bon_psw = FPInfo.get().getPass(fp);
        fp.openFiscalBon(1, bon_psw, false, false, false);
        fp.sellFree("Tomato", '1', 2.34f, 1.0f, 0.0f);
        fp.sellFree("Cucumber", '1', 1.0f, 3.54f, 0.0f);
        double sum = fp.calcIntermediateSum(false, false, false, 0.0f);
        fp.payment(sum, 0, false);
        fp.closeFiscalBon();
    }
}

// private Handler mHandler = new Handler(){
// public void handleMessage(Message msg)
// {
// dismissProgressDialog()
// }
// };
//
// private boolean downloadFiles() {
// showProgressDialog();
// for(int i = 0; i < filesList.size();i++) {
// Thread thread = new Thread(new Runnable() {
// @Override
// public void run() {
// //downloading code
// });
// thread.start();
// thread.run();
// }
// mHandler.sendEmptyMessage(0);
// return true;
// }
//
// //ProgressDialog progressDialog; I have declared earlier.
// private void showProgressDialog() {
// progressDialog = new ProgressDialog(N12ReadScreenActivity.this);
// progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
// progressDialog.setMessage("Downloading files...");
// progressDialog.show();
// }
//
// private void dismissProgressDialog() {
// if(progressDialog != null)
// progressDialog.dismiss();
// }

// http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog/3028660#3028660
// https://www.bluetooth.org/en-us/specification/assigned-numbers-overview/service-discovery
