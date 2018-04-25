package Interfaces;

import com.tremol.zfplibj.ZFPException;
import com.tremol.zfplibj.ZFPLib;

public interface Command
{
    public void execute(ZFPLib fp) throws ZFPException;
}
