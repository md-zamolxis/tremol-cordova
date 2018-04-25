package com.tremol.zfplibj;


import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 11/02/12
 */

public class ZFPPortUSB extends ZFPPort
{
    public final static String CHIP_FTDI="0403:6001";
    public final static String CHIP_FT232RL="0403:6001";
    public final static String CHIP_FT232H="0403:6014";
    public final static String CHIP_FT2232C="0403:6010";
    public final static String CHIP_FT2232D="0403:6010";
    public final static String CHIP_FT2232HL="0403:6011";

    private int SIZE_SERIALUSB_READ = 256;
    private int SIZE_SERIALUSB_WRITE = 256;

    private String usbDeviceID;
    private UsbDevice sDevice = null;

    private UsbDeviceConnection conn;

    private int baud_rate = 0x4138;
    private UsbEndpoint epIN = null;
    private UsbEndpoint epOUT = null;

    private boolean is_connected = false;


    public ZFPPortUSB(String chip, UsbDevice device, UsbDeviceConnection connection)
    {
        this.sDevice = device;
        this.conn = connection;
        this.usbDeviceID = chip;
        this.readTimeout = 3000;
        this.writeTimeout = 2000;
    }


    @Override
    public void open() throws Exception
    {
        try
        {
            UsbDevice dev = sDevice;
            if (dev == null)
                throw new Exception("USB Device is null!");

            conn.controlTransfer(0x40, 0, 0, 0, null, 0, 100);//reset
            conn.controlTransfer(0x40, 0, 1, 0, null, 0, 100);//clear Rx
            conn.controlTransfer(0x40, 0, 2, 0, null, 0, 100);//clear Tx
            conn.controlTransfer(0x40, 0x02, 0x0000, 0, null, 0, 100);//flow control none
            conn.controlTransfer(0x40, 0x03, baud_rate, 0, null, 0, 100);//baudrate
            conn.controlTransfer(0x40, 0x04, 0x0008, 0, null, 0, 300); //data bit 8, parity none, stop bit 1, tx off
            //conn.controlTransfer(0x40, 0x04, 0x2008, 0, null, 0, 300); //data bit 8, parity none, stop bit 1, tx on
            //conn.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
            //conn.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);

            epIN = epOUT = null;
            try
            {
                UsbInterface usbInterface = sDevice.getInterface(1);
                boolean b = conn.claimInterface(usbInterface, true);
                if(b)
                {
                    int interfaceCount = 0, endpointCount = 0;
                    interfaceCount = sDevice.getInterfaceCount();
                    endpointCount = usbInterface.getEndpointCount();

                    for(int i=0; i<=endpointCount; i++)
                    {
                        UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                        int epType = endpoint.getType();
                        int epDirection = endpoint.getDirection();
                        if(epType == UsbConstants.USB_ENDPOINT_XFER_BULK && epDirection  == UsbConstants.USB_DIR_IN)
                            epIN = endpoint;
                        else
                            epOUT = endpoint;
                        if(epIN != null && epOUT != null)
                            break;
                    }
                    SIZE_SERIALUSB_READ = epIN.getMaxPacketSize();
                    SIZE_SERIALUSB_WRITE = epOUT.getMaxPacketSize();
                    //epIN = usbInterface.getEndpoint(0);
                    //epOUT = usbInterface.getEndpoint(1);
                }
            }
            catch (Exception error)
            {
                throw new Exception("Error to get EndPoints!");
            }
            is_connected = (dev.getInterfaceCount() > 0 && epIN != null && epOUT !=null);
            if(!is_connected)
            {
                throw new Exception("Serial openning error, end points null! ");
            }
        }
        catch (Exception e)
        {
            throw new Exception("Serial openning : " + e.getCause().toString());
        }
    }



    @Override
    public int read(byte[] data) throws Exception
    {
        try
        {
            return read(data, 0, data.length);
        }
        catch(Exception ex)
        {
            throw new Exception("Error while reading from USB. Bulk Transfer fail. (data)");
        }
    }


    byte[] tmp_arr = new byte[SIZE_SERIALUSB_READ * 10];
    public int tmp_offset = 0;
    public int tmp_count = 0;
    private final int TICK = 50;

    @Override
    public int read(byte[] data, int offset, int count) throws Exception
    {
        long start = System.currentTimeMillis();
        while(count > tmp_count)
        {
            byte[] tmp = new byte[SIZE_SERIALUSB_WRITE];
            int byteCount = conn.bulkTransfer(epIN, tmp, tmp.length, TICK);
            if(byteCount == -1)
            {
                if(readTimeout < System.currentTimeMillis() - start)
                {
                    throw new Exception("Error while reading from USB. Bulk Transfer fail. (data, offset, count)");
                }
            }
            else
            {
                System.arraycopy(tmp, 0, tmp_arr, tmp_offset, byteCount);
                tmp_count += byteCount;
                if ((tmp_arr.length / 2) < tmp_count + tmp_offset)
                {
                    byte[] new_tmp = new byte[tmp_count];
                    System.arraycopy(tmp_arr, tmp_offset, new_tmp, 0, tmp_count);
                    //tmp_arr = new byte[SIZE_SERIALUSB * 4];
                    System.arraycopy(new_tmp, 0, tmp_arr, 0, tmp_count);
                    tmp_offset = 0;
                }
            }
        }
        System.arraycopy(tmp_arr, tmp_offset, data , offset, count);
        tmp_offset += count;
        tmp_count -= count;

        return count;
    }

    @Override
    public void write(byte[] data) throws Exception
    {
        int offset = 0;
        if (isClosed())
        {
            open();
        }
        while (offset < data.length)
        {
            int size_toupload = SIZE_SERIALUSB_WRITE;
            if (SIZE_SERIALUSB_WRITE > data.length - offset)
                size_toupload = data.length - offset;

            byte[] buffer = new byte[size_toupload];
            System.arraycopy(data, offset, buffer, 0, size_toupload);
            int size_uploaded = conn.bulkTransfer(epOUT, buffer, size_toupload, writeTimeout);
            if (size_uploaded < 0)
                throw new Exception("Error while writing to USB. Bulk Transfer fail");

            offset += size_uploaded;
        }
    }

    @Override
    public void discardInBuffer() throws Exception
    {
        //tmp_arr = new byte[SIZE_SERIALUSB_READ * 10];
        tmp_offset = 0;
        tmp_count = 0;
        flush();
    }

    @Override
    public int available() throws Exception
    {
        return 1; // TODO !!!
    }

    @Override
    public void flush() throws Exception
    {
        byte[] result = new byte[SIZE_SERIALUSB_READ];
        conn.bulkTransfer(epIN, result, result.length, TICK);
        conn.bulkTransfer(epIN, result, result.length, TICK);
    }


    @Override
    public boolean isClosed()
    {
        return !is_connected;
    }

    @Override
    public void close() {
        try
        {
            //sDevice = null;
            //mManager = null;
            epIN = epOUT = null;
            is_connected = false;
        }catch (Exception e) {

        }
    }

    /**
     /* 0x2710 300
     * 0x1388 600
     * 0x09C4 1200
     * 0x04E2 2400
     * 0x0271 4800
     * 0x4138 9600
     * 0x809C 19200
     * 0xC04E 38400
     * 0x0034 57600
     * 0x001A 115200
     * 0x000D 230400
     * 0x4006 460800
     * 0x8003 921600
     */
    public void setBaudrate(int bitrate) throws Exception {

        switch(bitrate)
        {
            case 300:
                baud_rate=0x2710;
                break;
            case 600:
                baud_rate=0x1388;
                break;
            case 1200:
                baud_rate=0x09C4;
                break;
            case 2400:
                baud_rate=0x0271;
                break;
            case 4800:
                baud_rate=0x4138;
                break;
            case 9600:
                baud_rate=0x4138;
                break;
            case 19200:
                baud_rate=0x809C;
                break;
            case 38400:
                baud_rate=0xC04E;
                break;
            case 57600:
                baud_rate=0x0034;
                break;
            case 115200:
                baud_rate=0x001A;
                break;
            case 460800:
                baud_rate=0x4006;
                break;
            case 921600:
                baud_rate=0x8003;
                break;
            default :
                throw new Exception("The baudrate selected is out of scope "+bitrate);
        }
    }
}
