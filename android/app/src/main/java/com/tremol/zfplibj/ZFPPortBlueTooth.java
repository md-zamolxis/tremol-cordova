package com.tremol.zfplibj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Mincho on 22.11.2017 г..
 */

public class ZFPPortBlueTooth extends ZFPPort
{
    protected OutputStream m_outputStream;//x
    protected InputStream m_inputStream;//x

    public ZFPPortBlueTooth(OutputStream os, InputStream is)
    {
        this.m_outputStream = os;
        this.m_inputStream = is;
        this.readTimeout = 3000;
    }


    @Override
    public int read(byte[] buffer, int offset, int count) throws Exception
    {
        return m_inputStream.read(buffer, offset, count);
    }

    @Override
    public int read(byte[] buffer) throws Exception
    {
        return m_inputStream.read(buffer);
    }

    @Override
    public void write(byte[] buffer) throws Exception
    {
        m_outputStream.write(buffer);
    }

    @Override
    public int available() throws Exception
    {
        return m_inputStream.available();
    }

    @Override
    public void close()
    {
        if(m_outputStream != null)
        {
            try
            {
                m_outputStream.flush();
            } catch (IOException e) { }
        }
        m_outputStream = null;
        m_inputStream = null;
    }

    @Override
    public void open()
    {

    }

    @Override
    public void discardInBuffer() throws Exception
    {
        byte[] avail = new byte [m_inputStream.available()];
        m_inputStream.read(avail);
        flush();
    }

    @Override
    public void flush() throws Exception
    {
        m_outputStream.flush();
    }

    @Override
    public boolean isClosed()
    {
        return (m_inputStream == null || m_outputStream == null);
    }
}
