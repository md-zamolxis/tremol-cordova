package com.tremol.androidFP.Server;

import android.os.Environment;
import android.os.FileObserver;

import com.tremol.androidFP.Server.XSaleFree.Item;
import com.tremol.zfplibj.ZFPLib;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FileServer
{
	public static final String DIR_ROOT = "TremolServer";
	public static final String DIR_IN = "IN";
	public static final String DIR_OK = "OK";
	public static final String DIR_ERR = "ERR";

	File dirRoot = null;
	File dirIn = null;
	File dirOut = null;
	File dirErr = null;
	File fileLog=null;
	
	private static int i = 0;

	private FileObserver observer = null;
	private Serializer serializer = null;
	ZFPLib fp = null;
	private String bon_psw;

	public FileServer(ZFPLib fp)
	{
		serializer = new Persister(new Format(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"));
		this.fp = fp;
	}

	public void stop()
	{
		if (observer != null)
			observer.stopWatching();
	}

	public void start() throws Exception
	{
 		checkExtStorage();
		setupDirs();
		if (bon_psw == null)
			bon_psw = fp.getOperatorInfo(1).getPassword().toString();
		observer = new FileObserver(dirIn.getAbsolutePath(),FileObserver.CREATE)//(dirRoot.getAbsolutePath())
		{
			@Override
			public void onEvent(int event, String file)
			{
				onObserverEvent(event, file);
			}
		};
		observer.startWatching();
	}

	private void onObserverEvent(int event, String file)
	{
		 //Log.d(TAG, "File created [" + pathToWatch + file + "]");
		switch (event)
		{
			case FileObserver.CREATE:
				File fileIn = new File(dirIn, file);
				File fileOut = new File(dirOut, file);
				File fileErr = new File(dirErr, file);
				try
				{
					if (fp.isClosed())
						throw new Exception("Not connected");

					String cname = XSaleFree.class.getSimpleName().toLowerCase(Locale.US);
					if (file.startsWith(cname.toLowerCase(Locale.US)))
					{
						XSaleFree sf = serializer.read(XSaleFree.class, fileIn);
						fp.openFiscalBon(1, bon_psw, false, false, false);
						for (Item i : sf.items)
							fp.sellFree(i.name, i.taxgrp, i.price, i.quantity, i.discountPCT);

						double sum = fp.calcIntermediateSum(false, false, false, 0.0f);
						fp.payment(sum, 0, false);
						fp.closeFiscalBon();

						try
						{
							fileIn.renameTo(fileOut);
						}
						catch (Exception e)
						{
						}
						appendLog(file + " OK", false);
					}
				}
				catch (Exception e)
				{
					try
					{
						appendLog(e.getMessage(), true);
					}
					catch (Exception ex1)
					{
					}
					try
					{
						fileIn.renameTo(fileErr);
					}
					catch (Exception ex2)
					{
					}
				}
				break;
		}
	}

	void appendLog(String msg, boolean err)
	{
		FileWriter fr = null;
		try
		{
		   String date = new SimpleDateFormat("dd-MM HH:mm").format(new Date());
		   fr = new FileWriter(fileLog, true);
		   fr.append(err ? "Err: " + date + " " + msg + "\r\n" : date + " " + msg + "\r\n");
		}
		catch (IOException ex){}
		finally
		{
			if (fr != null) try { fr.close(); } catch (IOException i) {}
		}
	}

	// ����
	public void createSellFreeFiles() throws Exception
	{		
		for(int j = 0; j < 2; j++)
		{
			i++;
			File f = new File(dirIn, "xsalefree_" + i + ".xml");
			if(f.exists())
				f.delete();

			XSaleFree sf = new XSaleFree();
			sf.command = "freeSale";

			Item i1 = new Item();
			i1.discountPCT = 0.0f;
			i1.name = "Test art " + i;
			i1.price = i;
			i1.quantity = 1.5f;
			i1.taxgrp = 'A';
			XSaleFree.Payment pmnt = new XSaleFree.Payment();
			pmnt.name = "Cash";
			pmnt.amount = 5f;

			Item i2 = new Item();
			i2.discountPCT = -10f;
			i2.name = "art test " + i;
			i2.price = 2.2f;
			i2.quantity = i;
			i2.taxgrp = 'B';

			sf.items = new ArrayList<Item>();
			sf.payments = new ArrayList<XSaleFree.Payment>();

			sf.payments.add(pmnt);
			sf.items.add(i1);
			sf.items.add(i2);
			serializer.write(sf, f);
		}
	}

	void setupDirs() throws Exception
	{
        boolean ok = true;
		dirRoot = Environment.getExternalStoragePublicDirectory(DIR_ROOT);// Environment.DIRECTORY_DCIM);		
		if (!dirRoot.exists())
			ok = dirRoot.mkdir();
		
		dirIn = new File(dirRoot, DIR_IN);
		if (!dirIn.exists())
			ok = ok && dirIn.mkdir();
		
		dirOut = new File(dirRoot, DIR_OK);
		if (!dirOut.exists())
			ok = ok && dirOut.mkdir();
		
		dirErr = new File(dirRoot, DIR_ERR);
		if (!dirErr.exists())
		    ok = ok && dirErr.mkdir();			
		
		fileLog = new File(dirRoot + File.separator + "log.txt");
		if(!fileLog.exists())
			ok = ok && fileLog.createNewFile();
		
		if (!ok)
			throw new Exception("FileServer: Can not create directory");    		
	}

	public void checkExtStorage()
	{
		boolean avlb = false;
		boolean writeable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state))
			avlb = writeable = true;
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			avlb = true;
			writeable = false;
		}
		else
			avlb = writeable = false;
	}

	public static <T> ArrayList<T> createArrayList(T... elements)
	{
		ArrayList<T> list = new ArrayList<T>();
		for (T element : elements)
			list.add(element);

		return list;
	}

}
