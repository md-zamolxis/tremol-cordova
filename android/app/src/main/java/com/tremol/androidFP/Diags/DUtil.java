package com.tremol.androidFP.Diags;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tremol.androidFP.R;

public class DUtil
{
	public enum DRES{Ok,CANCEL,NONE}
	static DRES DRes=DRES.NONE;    
	public static DRES confirm( Context context,String title, String message,final boolean block) {
	    final Handler handler = new Handler() {
	        @Override
	        public void handleMessage(Message mesg) {
	            throw new RuntimeException();
	        } 
	    };
        DRes=DRES.NONE;
	    AlertDialog.Builder b = new AlertDialog.Builder(context);
	    b.setTitle(title)
	    .setMessage(message)
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	DRes=DRES.Ok;
	        	if(block)
	              handler.sendMessage(handler.obtainMessage());
	            dialog.dismiss();
	        }
	    });
	    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	           DRes=DRES.CANCEL;
	           if(block)
	            handler.sendMessage(handler.obtainMessage());
	            dialog.dismiss();
	        }
	    });
	    b.show();
	   
	    if(block)
	    {
	      try { Looper.loop(); }
	      catch(RuntimeException e2) {}
	    }
	    return DRes;
	}
    static String inp="";
	public static String input( Context context,String title, String message,boolean block) {
	    final Handler handler = new Handler() {
	        @Override
	        public void handleMessage(Message mesg) {
	            throw new RuntimeException();
	        } 
	    };
	    final EditText input = new EditText(context);
	    AlertDialog.Builder b = new AlertDialog.Builder(context);  
	    b.setView(input);
	    b.setTitle(title)
	    .setMessage(message)
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	inp = input.getText().toString();
	        	DRes=DRES.Ok;
	            handler.sendMessage(handler.obtainMessage());
	            dialog.dismiss();//?
	        }
	    });
	    b.setNegativeButton("No", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            DRes=DRES.CANCEL;
	            handler.sendMessage(handler.obtainMessage());
	            dialog.dismiss();
	        }
	    });
	    b.show();
	   	input.requestFocus();
	    if(block)
	    {
	      try { Looper.loop(); }
	      catch(RuntimeException e2) {}
	    }
	    return inp;
	}
	
	static CharSequence chs=null;
//	public static CharSequence choose( Context context,String title, String message,final List<?extends CharSequence> chsl,boolean block) {
//	    final Handler handler = new Handler() {
//	        @Override
//	        public void handleMessage(Message mesg) {
//	            throw new RuntimeException();
//	        }
//	    };
//	    final CharSequence[] items=chsl.toArray(new CharSequence[chsl.size()]);
//		AlertDialog.Builder b = new AlertDialog.Builder(context);
//	    b.setTitle(title)
//	    //.setMessage(message)
//	    .setIcon(android.R.drawable.ic_menu_set_as)
//	    .setItems(items, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//             chs=items[which];
//             handler.sendMessage(handler.obtainMessage());
//        }});
//	    b.show();
//	    if(block)
//	    {
//	      try { Looper.loop(); }
//	      catch(RuntimeException e2) {}
//	    }
//	    return chs;
//	}
	public static CharSequence choose( Context context,String title, String message,final List<?extends CharSequence> chsl,boolean block) {
		return choose(context, title, message, chsl, block, false);
	}

	public static CharSequence choose(Context context, String title, String message, final List<?extends CharSequence> chsl, boolean block, boolean addfilter) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message mesg) {
				throw new RuntimeException();
			}
		};
		final CharSequence[] items=chsl.toArray(new CharSequence[chsl.size()]);
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(context,
				android.R.layout.select_dialog_item,android.R.id.text1 , items);
		final AlertDialog.Builder b = new AlertDialog.Builder(context);

		final LinearLayout layout= new LinearLayout(context);
		LinearLayout.LayoutParams parm = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(parm);
		if(addfilter)
		{
			final EditText filter = new EditText(context);
			final TextWatcher filterTextWatcher = new TextWatcher()
			{
				public void afterTextChanged(Editable s) { }
				public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					adapter.getFilter().filter(s);
				}
			};
			filter.addTextChangedListener(filterTextWatcher);
			layout.addView(filter);
		}
		ListView list = new ListView(context);
		list.setBackgroundColor(Color.WHITE);
		list.setCacheColorHint(Color.WHITE);
		list.setAdapter(adapter);
		layout.addView(list);
		b.setTitle(title)
			.setIcon(android.R.drawable.ic_menu_set_as)
			.setView(layout);
		final AlertDialog dialog = b.create();
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				chs = items[position];
				handler.sendMessage(handler.obtainMessage());
				dialog.dismiss();
			}
		});

		dialog.show();
		//b.show();

		if(block)
		{
			try { Looper.loop(); }
			catch(RuntimeException e2) {}
		}
		return chs;
	}

	
	
	static ProgressDialog	pd = null;
	static boolean dismissed=false;
	public static void pdStart( Context context,String title, String message,boolean block) 
	{
		final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message mesg) {
            throw new RuntimeException();
        } 
    };	  
		pd=new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage(message);
        pd.setTitle(title);
        pd.setButton(DialogInterface.BUTTON_POSITIVE, "yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            handler.sendMessage(handler.obtainMessage());
	            dialog.dismiss();//?
	        }
	    });
        pd.show();
        if(block)
	    {
	      try { Looper.loop(); }
	      catch(RuntimeException e2) {}
	    }
	}

	public static void pdEnd()
	{
		if(pd!=null)
		{
			dismissed=true;
			pd.dismiss();		
	    }
    }
}
