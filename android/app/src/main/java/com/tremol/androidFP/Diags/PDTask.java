package com.tremol.androidFP.Diags;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class PDTask extends AsyncTask <Void, Void, String> 
{
    private ProgressDialog pd;
    private Context ctx; 
    private Runnable rnbl;

    public PDTask(Context ctx, Runnable rnbl)
    {
    	this.ctx=ctx;
    	this.rnbl=rnbl;
    }
    @Override
    protected void onPreExecute()
    {
    	 pd = ProgressDialog.show(ctx,"talking to FP...", "talking to FP...",  true);
    }

    @Override
    protected String doInBackground(Void... params)
    {
        rnbl.run();
        return "";
    }

    @Override
    protected void onPostExecute(String result)
    {
        pd.dismiss();
    }
}
 //!!! //http://stackoverflow.com/questions/9296539/android-runonuithread-vs-asynctask


//http://androiddesk.wordpress.com/tag/progress-dialog-with-an-example/
//new PDTask(cxt, new Runnable()
//{				
//	public void run()
//	{
//		try
//		{
//			Thread.sleep(9000);
//		}
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
//	}
//}).execute();