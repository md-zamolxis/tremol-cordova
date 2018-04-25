package com.tremol.androidFP.Diags;



import Interfaces.MyRunnable;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.widget.Button;


public class PDiag extends ProgressDialog
{
	Activity context;
	public boolean AutoClose=true;
	private Button butPos=null;
	private Button butNeg=null;
	
	private PDiag(Activity context)
	{
		super(context);
		this.context=context;
	}

	public void setMsg(final String msg)
	{
		context.runOnUiThread(new Runnable()
		{
			public void run()
			{ 
				PDiag.this.setMessage(msg);
			}});
	}

	public void setPrgPrc(final int prc)
	{
		context.runOnUiThread(new Runnable()
		{
			public void run()
			{ 
				PDiag.this.setIndeterminate(false);
				PDiag.this.setProgress(prc);
			}});
	}

	public void setMsgPrc(String msg, Integer i)
	{
		if(msg != null)
			this.setMsg(msg);
		if(i != null)
			this.setPrgPrc(i);		
	}

	public void setIcn(final Integer i)
	{
		context.runOnUiThread(new Runnable()
		{
			public void run()
			{ 
				PDiag.this.setIcon(i);		
			}});
	}

	public void setOkEnbld()
	{
		context.runOnUiThread(new Runnable()
		{
			public void run()
			{ 
				PDiag.this.butPos.setEnabled(true);	
			}});
	}
	
	
	public static void perform( Activity context,String title, String message,final MyRunnable mr) 
	{
		final Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message mesg)
			{
				throw new RuntimeException();
        	}
    	};

        final PDiag pd=new PDiag(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage(message);
        pd.setTitle(title);
        pd.setIndeterminate(true);
        
        pd.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            handler.sendMessage(handler.obtainMessage());
	            dialog.dismiss();//?
	        }
	    });

        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            handler.sendMessage(handler.obtainMessage());
	            dialog.dismiss();//?
	        }
	    });

        pd.show();//1
        pd.butPos=pd.getButton(DialogInterface.BUTTON_POSITIVE);//2
        pd.butNeg=pd.getButton(DialogInterface.BUTTON_NEGATIVE);//2
        pd.butNeg.setEnabled(false);
        pd.butPos.setEnabled(false);

        new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
			 		mr.run(pd);
				}
				catch(Exception e)
				{
					mr.ex = e;
				}
				if(mr.ex != null)
				{
					pd.setIcn((android.R.drawable.stat_notify_error));
					pd.setMsg("Error:" + "\n" + mr.ex.getMessage());
					pd.setOkEnbld();
				}
				else if(pd.AutoClose)
				{
					pd.dismiss();
					handler.sendMessage(handler.obtainMessage());
				}
			}
		}).start();
       
      try { Looper.loop(); }
      catch(RuntimeException e2) {}
	    
	}
}
