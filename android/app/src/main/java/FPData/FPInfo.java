package FPData;

import android.app.Activity;
import android.content.res.Resources;

import com.tremol.androidFP.Diags.PDiag;
import com.tremol.androidFP.MainAct;
import com.tremol.androidFP.R;
import com.tremol.zfplibj.ZFPArticle;
import com.tremol.zfplibj.ZFPClientsDBdata;
import com.tremol.zfplibj.ZFPDepartment;
import com.tremol.zfplibj.ZFPException;
import com.tremol.zfplibj.ZFPLib;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import Interfaces.MyRunnable;

public class FPInfo
{
 
	private static FPInfo inst;	
    private FPInfo(){}
    private static Resources mRes=null;
	private int artsCount = 0;
	private int depsCount = 0;
	private int clientsCount = 0;

    public static FPInfo get()
    {
    	if(inst == null)
    	{
    		inst = new FPInfo();
    		mRes = MainAct.mRes;
    	}
    	return inst;
    }


    private ArrayList<ZFPDepartment> deps = null;
    public ArrayList<ZFPDepartment> getDeps(final ZFPLib fp, boolean forceReload,Activity a)
    {
    	if(forceReload || deps == null)
    	{
    		deps = new ArrayList<ZFPDepartment>(8);
    		MyRunnable mr= new MyRunnable(){public void run(final PDiag pd) throws Exception
			{
				for(int i = 1; i <= 8; i++)
				{
					pd.setMsgPrc(mRes.getString(R.string.Department) + i, i * 12);
					ZFPDepartment dep = fp.getDepartmentInfo(i);
					deps.add(dep);
				}
			}};
			PDiag.perform(a,mRes.getString(R.string.LoadDepartments), "", mr);
    	}
    	depsCount = deps.size();
		return deps;
    }
	public int getDepsCount() { return depsCount; }

    private ArrayList<ZFPArticle> articles=null;
    public ArrayList<ZFPArticle> getArticles(final ZFPLib fp, boolean forceReload, final boolean onlyten, Activity a)
	{
		if (forceReload || articles == null)
		{
			articles = new ArrayList<ZFPArticle>(100);
			MyRunnable mr = new MyRunnable()
			{
				public void run(final PDiag pd) throws Exception
				{
					for (int i = 1; i <= 50; i++)
					{
						//if(onlyten && i>10)
						//  break;
						pd.setMsgPrc(mRes.getString(R.string.article) + i, i * 2);
						ZFPArticle art = fp.getArticleInfo(i);
						if (art.getPrice() == 0)
							break;
						articles.add(art);
					}
				}
			};
			PDiag.perform(a, mRes.getString(R.string.reading_articles), "", mr);
		}
		artsCount = articles.size();
		return articles;
	}

	public int getArticlesCount() { return artsCount; }

	public ZFPArticle getArticle(final ZFPLib fp, int n) throws ZFPException, UnsupportedEncodingException
	{
		if(articles != null && articles.size() >= n)
			return articles.get(n - 1);
		else
			return fp.getArticleInfo(n);
	}

	public void refreshArticle(final ZFPLib fp, int num)
	{
		if(articles != null)
		{
			try
			{
				ZFPArticle art = fp.getArticleInfo(num);
				if (artsCount >= num)
				{
					articles.set(num - 1, art);
				}
				else if (artsCount + 1 == num)
				{
					articles.add(art);
					artsCount++;
				}
			}
			catch(ZFPException ex) { }
		}
	}

	private ArrayList<ZFPClientsDBdata> DBclients = null;// new ArrayList<ZFPArticle>();
	public ArrayList<ZFPClientsDBdata> getClients(final ZFPLib fp, boolean forceReload, Activity a)
	{
		if(forceReload || DBclients == null)
		{
			DBclients= new ArrayList<ZFPClientsDBdata>(100);
			MyRunnable mr= new MyRunnable(){public void run(final PDiag pd) throws Exception
			{
				for(int i = 1; i <= 100; i++)
				{
					pd.setMsgPrc(mRes.getString(R.string.client) + i, i * 2);
					ZFPClientsDBdata client = fp.getDBclientsData(i);
					if(client.getClientName().contentEquals(""))
						break;
					DBclients.add(client);
				}
			}};
			PDiag.perform(a, mRes.getString(R.string.LoadingClients), "", mr);
		}
		clientsCount = DBclients.size();
		return DBclients;
	}
	public int getClientsCount() { return clientsCount; }


    public String sn = "";
    public String tn = "";
    public String fm = "";
	public String vi = "";
    
    public String info = "";
    public String getInfo(final ZFPLib fp, boolean forcereload, Activity a)
	{
		if (forcereload)
		{
			MyRunnable mr = new MyRunnable()
			{
				public void run(final PDiag pd) throws Exception
				{
					pd.setMsgPrc(mRes.getString(R.string.num_ser), 25);
					sn = fp.getFactoryNumber();
					pd.setMsgPrc(mRes.getString(R.string.num_fisc_mem), 50);
					fm = fp.getFiscalNumber();
					pd.setMsgPrc(mRes.getString(R.string.num_tax), 75);
					tn = fp.getTaxNumber();
					pd.setMsgPrc(mRes.getString(R.string.version), 100);
					vi = fp.getVersion();
				}
			};
			PDiag.perform(a, mRes.getString(R.string.reading_fd_info), "", mr);
		}
		info = mRes.getString(R.string.num_ser) + ": " + sn + "\n" +
				mRes.getString(R.string.num_fisc_mem) + ": " + fm + "\n" +
				mRes.getString(R.string.num_tax) + ": " + tn + "\n" +
				mRes.getString(R.string.version) + ": " + vi + "\n";
		return info;
	}

    public String password = "";
    public String getPass(final ZFPLib fp)
    {
        if(password.contentEquals("") || password.contentEquals(null))
        {
            try
            {
                password = fp.getOperatorInfo(1).getPassword().toString();
            }
            catch (Exception ex)
            {}
        }
        return password;
    }

//    public String getVersionInfo(final ZFPLib fp)
//    {
//        try
//        {
//            if(vi.contentEquals("") || vi.contentEquals(null))
//				vi = fp.getVersion();
//        }
//        catch (Exception ex)
//        {}
//        return vi;
//    }
}
