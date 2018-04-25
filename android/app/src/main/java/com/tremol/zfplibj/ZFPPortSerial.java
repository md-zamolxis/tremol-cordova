package com.tremol.zfplibj;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Mincho on 22.11.2017 г..
 */

//TODO
public class ZFPPortSerial extends ZFPPort
{
    protected int m_commNum;
    protected int m_baudRate;

    public static final int STOPBITS_1 = 1;
    public static final int DATABITS_8 = 8;
    public static final int PARITY_NONE = 0;


    protected OutputStream m_outputStream;//x
    protected InputStream m_inputStream;//x


    //TODO
    public ZFPPortSerial(int comm, int baudRate) throws Exception
    {


    }

    @Override
    public int read(byte[] buffer, int offset, int count) throws Exception
    {
        return 0;
    }

    @Override
    public int read(byte[] buffer) throws Exception
    {
        return 0;
    }

    @Override
    public void write(byte[] buffer) throws Exception
    {

    }

    @Override
    public int available() throws Exception
    {
        return 0;
    }

    @Override
    public void close()
    {

    }

    @Override
    public void open() throws Exception
    {

    }

    @Override
    public void flush()
    {

    }

    @Override
    public void discardInBuffer() throws Exception
    {

    }

    @Override
    public boolean isClosed()
    {
        return false;
    }
}
