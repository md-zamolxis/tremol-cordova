package Interfaces;


import com.tremol.androidFP.Diags.PDiag;
import com.tremol.zfplibj.ZFPException;

public abstract class MyRunnable
{
	public Exception ex=null;
	public Double f;
	public String s;
	public void run(PDiag pd)throws ZFPException, Exception{}
}
