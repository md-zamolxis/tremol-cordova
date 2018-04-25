package com.tremol.zfplibj;


/**
 * Created by Mincho on 22.11.2017 г..
 */

public abstract class ZFPPort
{
    public abstract int read(byte[] buffer, int offset, int count) throws Exception;

    public abstract int read(byte[] buffer) throws Exception;

    public abstract void write(byte[] buffer) throws Exception;

    public abstract int available() throws Exception;

    public abstract void close();

    public abstract void open() throws Exception;

    public abstract void flush() throws Exception;

    public abstract void discardInBuffer() throws Exception;

    public abstract boolean isClosed();

    public int writeTimeout = 1500;
    public int readTimeout = 1500;
    public int pingTimeout = 300;
    public int pingRetries = 5;
}