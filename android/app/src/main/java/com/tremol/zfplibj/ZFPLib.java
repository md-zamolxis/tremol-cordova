/*
 * zfplib.java
 *
 */

package com.tremol.zfplibj;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.sun.PrintfFormat;

/**
 * ZFPLib is the main class responsible for communication with Zeka FP
 * fiscal printer device. In order to be used with serial port, it requires
 * <a href="http://java.sun.com/products/javacomm/">Java(tm) Communications API</a>
 * <p>Sample:</p>
 * <pre>
 * ZFPLib zfp = new ZFPLib(2, 9600); // COM2 baud rate 9600
 * zfp.openFiscalBon(1, "0000", false, false);
 * zfp.sellFree("Test article", '1', 2.34f, 1.0f, 0.0f);
 * zfp.sellFree("��������", '1', 1.0f, 3.54f, 0.0f);
 * float sum = zfp.calcIntermediateSum(false, false, false, 0.0f, '0');
 * zfp.payment(sum, 0, false);
 * zfp.closeFiscalBon();
 * </pre>
 */
public class ZFPLib
{
    /// constants

    public static final int ZFP_TEXTALIGNLEFT = 0;
    /**
     * Text is right aligned
     *
     * @see #printText(String, int)
     */
    public static final int ZFP_TEXTALIGNRIGHT = 1;
    /**
     * Text is centered
     *
     * @see #printText(String, int)
     */
    public static final int ZFP_TEXTALIGNCENTER = 2;

    protected FPLogger m_fpLogger;//x

    protected ZFPPort m_port;

    protected int m_lastNbl;
    protected byte[] m_receiveBuf;
    protected int m_receiveLen;
    protected int m_Operators;
    protected ZFPCountry m_Country;

    protected static int m_lang;
    protected static String CP = "cp1251";

    /**
     * Creates a new instance of zfplib. Default contructor.
     * You need to call {@link #setup} in order to setup the parameters
     */
    public ZFPLib()
    {
        init(ZFPException.ZFP_LANG_EN, ZFPCountry.BG, 20);
    }


    public ZFPLib(ZFPPort port, String charset, FPLogger fpLogger)//x
    {
        this();
        m_port = port;
        m_fpLogger = fpLogger;
        CP = charset;
    }

    protected void init(int mLang, ZFPCountry cntr, int operators)
    {
        m_lastNbl = 0x20;
        m_receiveBuf = new byte[256];
        m_lang = mLang; // default is English
        m_Country = cntr;
        m_Operators = operators;
    }


    public void close()//x
    {
        if (m_port != null)
        {
            try
            {
                m_port.close();
            }
            catch (Exception e)
            {
            }
            m_port = null;
        }
    }

    public boolean isClosed()
    {
        return m_port == null || m_port.isClosed();
    }



    /**
     * Setup error message language
     *
     * @param language error message language
     * @see ZFPException#ZFP_LANG_EN
     */
    public void setLanguage(int language)
    {
        m_lang = language;
    }

    /**
     * Return error message language
     *
     * @return language error message language
     * @see ZFPException#ZFP_LANG_EN
     */
    public static int getLanguage()
    {
        return m_lang;
    }

    public ZFPCountry getCountry()
    {
        return m_Country;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //    protected void openComm() throws ZFPException
    //    {
    //        try {
    //            m_serialPort = (SerialPort) m_portId.open("zfplibj", (int)g_timeout);
    //            m_serialPort.setSerialPortParams(m_baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    //            m_outputStream = m_serialPort.getOutputStream();
    //            m_inputStream = m_serialPort.getInputStream();
    //        }
    //        catch (Exception e) {
    //            throw new ZFPException(e);
    //        }
    //        try {
    //            m_serialPort.enableReceiveTimeout((int)g_timeout);
    //        } catch (Exception e) {}
    //    }
    //////////////////////////////////////////////////////////////////////////////////////////// 
    protected void openComm() throws ZFPException
    {
    }

    protected void closeComm() throws ZFPException
    {
        try
        {
            if (m_port != null)
                m_port.flush();
        }
        catch (Exception e)
        {
            throw new ZFPException(e);
        }
    }

    //    protected void closeComm() throws ZFPException
    //    {
    //        try {
    //            m_outputStream.flush();
    //            m_outputStream.close();
    //            m_inputStream.close();
    //            m_serialPort.close();
    //        }
    //        catch (Exception e) {
    //            throw new ZFPException(e);
    //        }
    //    }

    protected boolean makeCRC(byte[] data, int len, int mode)
    {
        // calculate the CRC
        byte crc = 0;
        for (int i = 1; i < len - 3; i++)
            crc ^= data[i];

        switch (mode)
        {
            case 0:
                // add the CRC
                data[len - 3] = (byte) ((crc >> 4) | 0x30);
                data[len - 2] = (byte) ((crc & 0x0F) | 0x30);
                break;

            case 1:
                // add the CRC
                byte test = (byte) (((crc >> 4) & 0x0F) | 0x30);
                test |= (byte) 0x30;
                if (data[len - 3] != (byte) (((crc >> 4) & 0x0F) | 0x30))
                    return false;
                if (data[len - 2] != (byte) ((crc & 0x0F) | 0x30))
                    return false;
                break;
        }
        return true;
    }

    protected boolean doPing(byte ping, int retries) throws ZFPException
    {
        byte[] b = new byte[1];

        int tempTo = m_port.readTimeout;
        for (int i = 0; i < retries; i++)
        {
            m_port.readTimeout = m_port.pingTimeout;
            try
            {
                b[0] = (byte) 0x03;  // antiecho
                m_port.write(b);

                b[0] = ping;        // ping
                m_port.write(b);

                b[0] = 0;
                long start = System.currentTimeMillis();
                do
                {
                    if (0 < m_port.available())
                    {
                        m_port.read(b);
                        m_port.readTimeout = tempTo;
                        if (b[0] == (byte) 0x03)
                            throw new ZFPException(0x10E, m_lang);

                        if (b[0] == ping)
                            return true;

                        try
                        {
                            wait(20);
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
                while (m_port.readTimeout > System.currentTimeMillis() - start);
            }
            catch (Exception e)
            {
                m_port.readTimeout = tempTo;
                if (i + 1 == retries)
                    throw new ZFPException(e);
            }
        }
        throw new ZFPException(0x102, m_lang);
    }

    protected boolean checkForZFP() throws ZFPException //x
    {
        //return true;
        return doPing((byte) 0x04, m_port.pingRetries);
    }

    protected boolean checkForZFPBusy() throws ZFPException //x
    {
        //return true;
        return doPing((byte) 0x05,  m_port.pingRetries);  //!
    }

    protected void getResponse() throws ZFPException
    {
        int read;

        long start = System.currentTimeMillis();

        do
        {
            try
            {
                //if (0 < m_port.available())
                {
                    //Чете един байт ако има в стрийма!
                    read = m_port.read(m_receiveBuf, 0, 1);
                    if (0 < read)
                    {
                        if ((byte) 0x06 == m_receiveBuf[0])  // ACK
                            break;

                        else if ((byte) 0x02 == m_receiveBuf[0]) // STX
                            break;

                        else if ((byte) 0x15 == m_receiveBuf[0]) // NACK
                            throw new ZFPException(0x103, m_lang);

                        else if ((byte) 0x03 == m_receiveBuf[0]) // ANTIECHO
                            throw new ZFPException(0x10E, m_lang);

                        else if ((byte) 0x0E == m_receiveBuf[0]) // RETRY
                            // ToDo
                            break;

                    }
                }
            }
            catch (Exception e)
            {
                throw new ZFPException(e);
            }

            if (m_port.readTimeout < System.currentTimeMillis() - start)
                throw new ZFPException(0x102, m_lang);

            try
            {
                if (0 == m_port.available())
                    wait(20);
            }
            catch (Exception e)
            {
            }
        }
        while (true);

        // read the data
        m_receiveLen = 1;
        int avail;
        do
        {
            try
            {//Проверка колко байтове има в стрийма / check how many bytes are in the stream
                avail = m_port.available();
            }
            catch (Exception e)
            {
                throw new ZFPException(e);
            }
            if (0 < avail)
            {
                try
                {//Проверка колко байтове има в стрийма и запис в m_receiveBuf / check how many bytes are in the stream and write in m_receiveBuf
                    read = m_port.read(m_receiveBuf, m_receiveLen, 1);
                }
                catch (Exception e)
                {
                    throw new ZFPException(e);
                }
                if (0 < read)
                {//Проверка за край 0x0A / check for end 0x0A
                    if ((byte) 0x0A == m_receiveBuf[m_receiveLen])
                    {
                        m_receiveLen += read;
                        break;
                    }
                    m_receiveLen += read;
                }
            }
            // timeout check
            if (m_port.readTimeout < System.currentTimeMillis() - start)
                throw new ZFPException(0x102, m_lang);

            try
            {
                wait(20);
            }
            catch (Exception e)
            {
            }
        }
        while (true);


        if (!makeCRC(m_receiveBuf, m_receiveLen, 1))
            throw new ZFPException(0x104, m_lang);

        if ((byte) 0x06 == m_receiveBuf[0]) // ACK
        {
            if (((byte) 0x30 != m_receiveBuf[2]) || ((byte) 0x30 != m_receiveBuf[3]))
            {
                //int error = Integer.parseInt(new String(m_receiveBuf, 2, 2), 16);
                //throw new ZFPException(error, m_lang);
                byte s1 = m_receiveBuf[m_receiveLen - 5];
                byte s2 = m_receiveBuf[m_receiveLen - 4];
                byte ste = (byte) (((s1 <= (byte) '9' ? s1 : (s1 <= 0x3f ? s1 - 48 : s1 - 55)) << 4) | ((s2 <= (byte) '9' ? s2 : (s2 <= 0x3f ? s2 - 48 : s2 - 55)) & 15));
                if ((ste & 0xFF) != 0)
                    throw new ZFPException(ste & 0xFF, m_lang);
            }
        }
        else if (m_receiveBuf[2] != (byte) m_lastNbl)
            throw new ZFPException(0x10B, m_lang);

        if (m_fpLogger != null) //x
        {
            //byte[] all = new byte[data==null?1:data.length+1];
            //all[0]=cmd;
            //if(data!=null)
            //  System.arraycopy(data, 0, all, 1, data.length);
            try
            {
                m_fpLogger.Log(new String(m_receiveBuf, 0, m_receiveLen, CP), false);
            }
            catch (UnsupportedEncodingException e)
            {
                m_fpLogger.Log(new String(m_receiveBuf, 0, m_receiveLen), false);
            }
        }
    }

    public String[] getArrayResult() throws ZFPException
    {
        return getStringResult().split(";");
    }

    public String getStringResult() throws ZFPException
    {
        try
        {
            return new String(m_receiveBuf, 4, m_receiveLen - 7, CP);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
    }

    //Test Command for EscPOS protocol
    /*public void sendEscPOS()throws ZFPException
    {
        StringBuilder sb = new StringBuilder();
        sb.append((char)0x0a);
        sb.append("Romania");
        sb.append((char)0x0a);
        sb.append("Hello");
        try {
            //byte[] b = new byte[]{0x48,0x65, 0x6c, 0x6c, 0x6f, 0x0a, 0x52, 0x4f, 0x4d, 0x41, 0x4e, 0x49, 0x41};
            m_outputStream.write(sb.toString().getBytes());
            //sendCommand(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
         closeComm();
        }
    }*/

    public void sendCommand(String str) throws ZFPException //x
    {
        if (str == null || str.length() == 0)
            throw new ZFPException("Invalid CMD");

        byte[] all = str.getBytes();
        int datalen = all.length - 1;
        byte[] data = null;
        if (datalen > 0)
        {
            data = new byte[datalen];
            System.arraycopy(all, 1, data, 0, datalen);
        }
        sendCommand(all[0], data);
    }

    public void sendCommand(byte cmd, byte[] data) throws ZFPException
    {
        openComm();

        try
        {
            if (m_fpLogger != null) //x
            {
                byte[] all = new byte[data == null ? 1 : data.length + 1];
                all[0] = cmd;
                if (data != null)
                    System.arraycopy(data, 0, all, 1, data.length);
                try
                {
                    m_fpLogger.Log(new String(all, CP), true);
                }
                catch (UnsupportedEncodingException e)
                {
                    m_fpLogger.Log(new String(all), true);
                }
            }
            try
            {
                m_port.discardInBuffer();
            }
            catch (Exception ignored) { }
            checkForZFP();
            checkForZFPBusy();

            // prepare the command
            int len = (null != data) ? data.length : 0;
            byte[] fullCmd = new byte[4 + len + 3];
            fullCmd[0] = (byte) 0x02;                // STX
            fullCmd[1] = (byte) (len + 0x20 + 0x03); // LEN
            if (0xFF < ++m_lastNbl)
                m_lastNbl = 0x20;
            fullCmd[2] = (byte) m_lastNbl;           // NBL
            fullCmd[3] = cmd;                       // CMD

            if (null != data)
                System.arraycopy(data, 0, fullCmd, 4, len);

            makeCRC(fullCmd, fullCmd.length, 0);
            fullCmd[fullCmd.length - 1] = (byte) 0x0A; // ETX

            try
            {
                m_port.write(fullCmd);
            }
            catch (Exception e)
            {
                throw new ZFPException(e);
            }
            getResponse();
        }
        finally
        {
            closeComm();
        }

    }

    static public String nstrcpy(String s, int maxlen)
    {
        if (maxlen < s.length())
            return s.substring(0, maxlen);
        return s;
    }

    static public String getFloatFormat(double num, int count) //float
    {
        float max_value = (2 == count) ? 9999999.99f : 999999.999f;
        String match;
        if (max_value < num)
            match = "%.0f";
        else
        {
            match = "%010.";
            match += Integer.toString(count);
            match += "f";
        }

        String res = new PrintfFormat(match).sprintf(num).replace(',', '.');
        if ('.' == res.charAt(9))
            return res.substring(0, 9);

        return res;
    }

    //////////////////////////////////////////////////////////////////////
    // Commands
    //////////////////////////////////////////////////////////////////////

    /**
     * Gets Zeka FP status (errors and other flags)
     *
     * @return ZFPStatus class containing the current status
     * @throws ZFPException in case of communication error
     * @see ZFPStatus
     */
    public ZFPStatus getStatus() throws ZFPException
    {
        sendCommand((byte) 0x20, null);
        return new ZFPStatus(m_receiveBuf, m_receiveLen, m_lang);
    }

    /**
     * Runs Zeka FP diagnostic print
     *
     * @throws ZFPException in case of communication error
     */
    public void diagnostic() throws ZFPException
    {
        sendCommand((byte) 0x22, null);
    }

    /**
     * Gets Zeka FP firmware version info
     *
     * @return Zeka FP firmware version info
     * @throws ZFPException in case of communication error
     */
    public String getVersion() throws ZFPException
    {
        String data = "B";
        sendCommand((byte) 0x21, data.getBytes());
        return getStringResult();
    }

    /**
     * Clears Zeka FP external display
     *
     * @throws ZFPException in case of communication error
     */
    public void displayClear() throws ZFPException
    {
        sendCommand((byte) 0x24, null);
    }

    /**
     * Displays text on the first line of the external display
     *
     * @param line string to be displayed, truncated to 20 characters when longer
     * @throws ZFPException in case of communication error
     */
    public void displayLine1(String line) throws ZFPException
    {
        String data = new PrintfFormat("%-20s").sprintf(nstrcpy(line, 20));
        sendCommand((byte) 0x25, data.getBytes());
    }

    /**
     * Displays text on the second line of the external display
     *
     * @param line string to be displayed, truncated to 20 characters when longer
     * @throws ZFPException in case of communication error
     */
    public void displayLine2(String line) throws ZFPException
    {
        String data = new PrintfFormat("%-20s").sprintf(nstrcpy(line, 20));
        sendCommand((byte) 0x26, data.getBytes());
    }

    /**
     * Displays text on both lines of the external display
     *
     * @param line string to be displayed, truncated to 40 characters when longer
     * @throws ZFPException in case of communication error
     */
    public void display(String line) throws ZFPException
    {
        String data = new PrintfFormat("%-40s").sprintf(nstrcpy(line, 40));
        sendCommand((byte) 0x27, data.getBytes());
    }

    /**
     * Displays current date and time on the external display
     *
     * @throws ZFPException in case of communication error
     */
    public void displayDateTime() throws ZFPException
    {
        sendCommand((byte) 0x28, null);
    }

    /**
     * Cuts the paper
     *
     * @throws ZFPException in case of communication error
     */
    public void paperCut() throws ZFPException
    {
        sendCommand((byte) 0x29, null);
    }

    /**
     * Opens the safe box
     *
     * @throws ZFPException in case of communication error
     */
    public void openTill() throws ZFPException
    {
        sendCommand((byte) 0x2A, null);
    }

    /**
     * Feeds one line of paper
     *
     * @throws ZFPException in case of communication error
     */
    public void lineFeed() throws ZFPException
    {
        sendCommand((byte) 0x2B, null);
    }

    /**
     * Gets Zeka FP manifacture number
     *
     * @return manifacture number - string 10 characters long
     * @throws ZFPException in case of communication error
     */
    public String getFactoryNumber() throws ZFPException
    {
        sendCommand((byte) 0x60, null);
        return getArrayResult()[0];
    }

    /**
     * Gets Zeka FP Tax Memory number
     *
     * @return Tax Memory number - string 10 characters long
     * @throws ZFPException in case of communication error
     */
    public String getFiscalNumber() throws ZFPException
    {
        sendCommand((byte) 0x60, null);
        return getArrayResult()[1];
    }

    /**
     * Gets Zeka FP Tax number
     *
     * @return Tax number - string 14 characters long
     * @throws ZFPException in case of communication error
     */
    public String getTaxNumber() throws ZFPException
    {
        sendCommand((byte) 0x61, null);
        return getArrayResult()[0];
    }

    /**
     * Gets Zeka FP Tax Percents
     *
     * @return ZFPTaxNumbers class - percentage of different tax groups
     * @throws ZFPException in case of communication error
     * @see ZFPTaxNumbers
     */
    public ZFPTaxNumbers getTaxPercents() throws ZFPException
    {
        sendCommand((byte) 0x62, null);
        return new ZFPTaxNumbers(m_receiveBuf, m_receiveLen, m_lang, "%;");
    }

    /**
     * Gets Zeka FP Decimal point position
     *
     * @return decimal point position
     * @throws ZFPException in case of communication error
     */
    public int getDecimalPoint() throws ZFPException
    {
        sendCommand((byte) 0x63, null);
        return Integer.parseInt(getStringResult());
    }

    /**
     * Gets Zeka FP additional payment types names
     *
     * @return ZFPPayTypes class - names of additional payment types
     * @throws ZFPException in case of communication error
     * @see ZFPPayTypes
     */
    public ZFPPayTypes getPayTypes() throws ZFPException
    {
        sendCommand((byte) 0x64, null);
        return new ZFPPayTypes(m_receiveBuf, m_receiveLen, m_lang);
    }

    /**
     * Gets Zeka FP system parameter settings
     *
     * @return ZFPParameters class - parameter values
     * @throws ZFPException in case of communication error
     * @see ZFPParameters
     */
    public ZFPParameters getParameters() throws ZFPException
    {
        sendCommand((byte) 0x65, null);
        return new ZFPParameters(m_receiveBuf, m_receiveLen, m_lang);
    }

    /**
     * Gets Zeka FP system date and time
     *
     * @return java.util.Calendar class containing the date and time stored in Zeka FP
     * @throws ZFPException in case of communication error
     */
    public Calendar getDateTime() throws ZFPException
    {
        sendCommand((byte) 0x68, null);
        String[] s = getStringResult().split("[\\s-\\:]");
        if (5 != s.length)
            throw new ZFPException(0x106, m_lang);

        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(s[2]), Integer.parseInt(s[1]), Integer.parseInt(s[0]), Integer.parseInt(s[3]), Integer.parseInt(s[4]));
        return cal;
    }

    /**
     * Gets Zeka FP Header and Footer lines
     *
     * @param line indicates the exact line to read (1 to 8)
     * @return header or footer text - string up to 40 characters long
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public String getClisheLine(int line) throws ZFPException
    {
        if ((8 < line) || (1 > line))
            throw new ZFPException(0x101, m_lang);

        String data = Integer.toString(line);
        sendCommand((byte) 0x69, data.getBytes());
        return getArrayResult()[1]; // idx 0 == line
    }

    /**
     * Gets Zeka FP Operator information
     *
     * @param oper indicates the exact number operator to read
     * @return ZFPOperatorInfo class - information for the certain operator
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     * @see ZFPOperatorInfo
     */
    public ZFPOperatorInfo getOperatorInfo(int oper) throws ZFPException
    {
        if ((9 < oper) || (1 > oper))
            throw new ZFPException(0x101, m_lang);

        String data = Integer.toString(oper);
        sendCommand((byte) 0x6A, data.getBytes());
        return new ZFPOperatorInfo(oper, m_receiveBuf, m_receiveLen, m_lang);
    }

    /**
     * Prints graphic logo
     *
     * @throws ZFPException in case of communication error
     */
    public void printLogo() throws ZFPException
    {
        printLogo(0);
    }

    /**
     * Prints graphic logo
     *
     * @throws ZFPException in case of communication error
     */
    public void printLogo(int logonum) throws ZFPException
    {
        String data = Integer.toString(logonum);
        sendCommand((byte) 0x6C, data.getBytes());
    }

    /**
     * Opens non fiscal receipt
     *
     * @param oper indicates the exact number operator (1 to 9)
     * @param pass string containing the certain operator password
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void openBon(int oper, String pass, boolean delayPrint) throws ZFPException
    {
        if ((9 < oper) || (1 > oper))
            throw new ZFPException(0x101, m_lang);

        String data = Integer.toString(oper);
        data += ";";
        data += new PrintfFormat("%-4s").sprintf(nstrcpy(pass, 4));
        data += ";J";
        data += (delayPrint ? ";1" : ";0");
        sendCommand((byte) 0x2E, data.getBytes());
    }

    /**
     * Closes the opened non client receipt
     *
     * @throws ZFPException in case of communication error
     */
    public void closeBon() throws ZFPException
    {
        sendCommand((byte) 0x2F, null);
    }

    /**
     * Opens  client receipt
     *
     * @param oper     indicates the exact number operator (1 to 9)
     * @param pass     string containing the certain operator password - 4 characters
     * @param detailed flag for detailed or brief receipt (0 = brief, 1 = detailed)
     * @param vat      flag for printing the VAT tax sums separately (0 = do not print, 1 = print)
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void openFiscalBon(int oper, String pass, boolean detailed, boolean vat, boolean delayPrint) throws ZFPException
    {
        if ((m_Operators < oper) || (1 > oper))
            throw new ZFPException(0x101, m_lang);

        String opDigitsStr = String.valueOf(Integer.toString(m_Operators).length());
        StringBuffer data = new StringBuffer(new PrintfFormat("%0" + opDigitsStr + "d").sprintf(oper));

        data.append(";");
        data.append(new PrintfFormat("%-4s").sprintf(nstrcpy(pass, 4)));
        data.append(detailed ? ";1" : ";0");
        data.append(vat ? ";1" : ";0");//x//data.append(vat ? ";1;0" : ";0;0");
        data.append(delayPrint ? ";2" : ";0");//x

        sendCommand((byte) 0x30, data.toString().getBytes());
    }

    /**
     * Opens  client invoice receipt
     *
     * @param oper     indicates the exact number operator
     * @param pass     string containing the certain operator password (1 to 9)
     * @param client   string containing client name - truncated to 26 characters when longer
     * @param receiver string containing recipient name - truncated to 16 characters when longer
     * @param taxnum   string containing client tax number - truncated to 13 characters when longer
     * @param bulstat  string containing client bulstat number - truncated to 13 characters when longer
     * @param address  string containing client address - truncated to 30 characters when longer
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void openInvoice(int oper, String pass, String client, String receiver,
                            String taxnum, String bulstat, String address) throws ZFPException
    {
        if ((m_Operators < oper) || (1 > oper))
            throw new ZFPException(0x101, m_lang);

        String opDigitsStr = String.valueOf(Integer.toString(m_Operators).length());
        StringBuffer data = new StringBuffer(new PrintfFormat("%0" + opDigitsStr + "d").sprintf(oper));

        data.append(";");
        data.append(new PrintfFormat("%-4s").sprintf(nstrcpy(pass, 4)));
        data.append(";0;0;1;");
        data.append(new PrintfFormat("%-26s").sprintf(nstrcpy(client, 26)));
        data.append(";");
        data.append(new PrintfFormat("%-16s").sprintf(nstrcpy(receiver, 16)));
        data.append(";");
        data.append(new PrintfFormat("%-13s").sprintf(nstrcpy(taxnum, 13)));
        data.append(";");
        data.append(new PrintfFormat("%-13s").sprintf(nstrcpy(bulstat, 13)));
        data.append(";");
        data.append(new PrintfFormat("%-30s").sprintf(nstrcpy(address, 30)));

        try
        {
            sendCommand((byte) 0x30, data.toString().getBytes(CP));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
    }

    /**
     * Closes the opened client receipt
     *
     * @throws ZFPException in case of communication error
     */
    public void closeFiscalBon() throws ZFPException
    {
        openComm();
        try
        {
            sendCommand((byte) 0x38, null);
        }
        finally
        {
            closeComm();
        }
    }

    /**
     * Cancel the opened client receipt
     *
     * @throws ZFPException in case of communication error
     */
    public void cancelFiscalBon() throws ZFPException
    {
        sendCommand((byte) 0x39, null);
    }

    /**
     * Closes the opened client invoice receipt
     *
     * @throws ZFPException in case of communication error
     */
    public void closeInvoice() throws ZFPException
    {
        closeFiscalBon();
    }

    public void closeFiscalBonWithAutoPayment() throws ZFPException //x
    {
        sendCommand((byte) 0x36, null);
    }


    /**
     * Registers item sell from PC database
     *
     * @param name     string containing item description - truncated to 36 characters when longer
     * @param taxgrp   character characterizing the item tax group attachment (0, 1, 2, '0', '1', '2' for Bulgarian FP version)
     * @param price    item price
     * @param quantity item quantity
     * @param discount discount/addition in percents
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void sellFree(String name, char taxgrp, double price, float quantity, float discount) throws ZFPException
    {
        if ((-99999999.0f > price) || (99999999.0f < price) || (0.0f > quantity) ||
                (999999.999f < quantity) || (-999.0f > discount) || (999.0f < discount))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%-36s").sprintf(nstrcpy(name, 36)));
        data.append(";");
        data.append(taxgrp);
        data.append(";");
        data.append(getFloatFormat(price, 2));
        data.append("*");
        data.append(getFloatFormat(quantity, 3));
        if (0.0f != discount)
        {
            data.append(",");
            data.append(new PrintfFormat("%6.2f").sprintf(discount));
            data.append("%");
        }
        try
        {
            sendCommand((byte) 0x31, data.toString().getBytes(CP));//x
        }
        catch (Exception e)
        {
            throw new ZFPException(e);
        }
    }

    public void sellDepartment(String name, int depnum, double price, float quantity) throws ZFPException
    {
        if ((-99999999.0f > price) || (99999999.0f < price) || (0.0f > quantity) ||
                (999999.999f < quantity) || depnum < 0 || depnum > 20)
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%-36s").sprintf(nstrcpy(name, 36)));
        data.append(";");
        try
        {
            data.append(new String(new byte[]{(byte) (0x80 + depnum)}, CP));//  data.append((char)(0x80+depnum));
        }
        catch (Exception e)
        {
            throw new ZFPException(e);
        }
        data.append(";");
        data.append(getFloatFormat(price, 2));
        data.append("*");
        data.append(getFloatFormat(quantity, 3));

        String s = data.toString();
        try
        {
            sendCommand((byte) 0x34, s.getBytes(CP));//x CP//"UTF-8"
        }
        catch (Exception e)
        {
            throw new ZFPException(e);
        }
    }

    public void sellDepartment(String name, int depnum, double price, float quantity, float discount) throws ZFPException
    {
        if ((-99999999.0f > price) || (99999999.0f < price) || (0.0f > quantity) ||
                (999999.999f < quantity) || (-999.0f > discount) || (999.0f < discount)
                || depnum < 0 || depnum > 20)
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%-36s").sprintf(nstrcpy(name, 36)));
        data.append(";");
        try
        {
            data.append(new String(new byte[]{(byte) (0x80 + depnum)}, CP));//  data.append((char)(0x80+depnum));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
        data.append(";");
        data.append(getFloatFormat(price, 2));
        data.append("*");
        data.append(getFloatFormat(quantity, 3));
        if (0.0f != discount)
        {
            data.append(",");
            data.append(new PrintfFormat("%6.2f").sprintf(discount));
            data.append("%");
        }
        try
        {
            String s = data.toString();
            sendCommand((byte) 0x34, s.getBytes(CP));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
    }

    /**
     * Registers item sell from FP internal database
     *
     * @param isVoid   flag specifing is it item sell or void
     * @param number   item database number
     * @param quantity item quantity
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void sellDB(boolean isVoid, int number, float quantity) throws ZFPException
    {
        if ((0 > quantity) || (9999999999.0f < quantity) || (0 > number))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer();
        data.append(isVoid ? '-' : '+');
        // data.append(";");
        data.append(new PrintfFormat("%05d").sprintf(number));//%05u
        data.append("*");
        data.append(getFloatFormat(quantity, 3));
        // data.append(new PrintfFormat("%.3f").sprintf(quantity));

        sendCommand((byte) 0x32, data.toString().getBytes());
    }

    /**
     * Registers item sell from FP internal database
     *
     * @param isVoid   flag specifing is it item sell or void
     * @param number   item database number
     * @param quantity item quantity
     * @param discount discount/addition in percents
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void sellDB(boolean isVoid, int number, float quantity, float discount) throws ZFPException
    {
        if ((0 > quantity) || (9999999999.0f < quantity) ||
                (-999.0f > discount) || (999.0f < discount) || (0 > number))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer();
        data.append(isVoid ? '-' : '+');
        // data.append(";");
        data.append(new PrintfFormat("%05d").sprintf(number));//%05u
        data.append("*");
        data.append(getFloatFormat(quantity, 3));
        // data.append(new PrintfFormat("%.3f").sprintf(quantity));
        if (0.0f != discount)
        {
            data.append(",");
            data.append(new PrintfFormat("%6.2f").sprintf(discount));
            data.append("%"); //TODO стойностна отстъпка/надбавка
        }

        sendCommand((byte) 0x32, data.toString().getBytes());
    }

    /**
     * Calculates the sub total sum of the receipt
     *
     * @param print     flag for print the sub total sum
     * @param show      flag for show the sub total sum on the external display
     * @param isPercent flag for percentage discount/addition
     * @param discount  discount/addition value
     * @param taxgrp    specifies the tax group - ignored in Bulgarian FP version
     * @return returns the sub total sum
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public double calcIntermediateSum(boolean print, boolean showOnDisplay, boolean isPercent,
                                      float discount) throws ZFPException
    {
        StringBuffer data = new StringBuffer();
        data.append(print ? '1' : '0');
        data.append(";");
        data.append(showOnDisplay ? '1' : '0');
        if (0.0f != discount)
        {
            if (isPercent)
            {
                data.append(",");
                data.append(new PrintfFormat("%6.2f").sprintf(discount));
                data.append("%");
            }
            else
            {
                data.append(":");
                data.append(getFloatFormat(discount, 2));
            }
        }
        sendCommand((byte) 0x33, data.toString().getBytes());
        String res = getStringResult();
        return Double.parseDouble(res);
    }

    /**
     * Registers payment of the receipt
     *
     * @param sum    paid sum (-1 voiding done payments )///
     * @param type   specifies the payment type number (0 to 3)
     * @param noRest specifies that no change is due to client when true (takes effect only with certain payment types)
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void payment(double sum, int type, boolean noRest) throws ZFPException //float
    {
        if ((0 > type) || (4 < type) || (9999999999.0f < sum))//|| (0.0f > sum)
            throw new ZFPException(0x101, m_lang);

        String data = Integer.toString(type);
        data += noRest ? ";1;" : ";0;";
        data += sum == -1f ? "\"" : getFloatFormat(sum, 2);///
        sendCommand((byte) 0x35, data.getBytes());
    }

    /**
     * Calcualtes the VAT of the receipt and transfers it in VAT Account
     *
     * @throws ZFPException in case of communication error
     */
    public void payVAT() throws ZFPException
    {
        sendCommand((byte) 0x36, null);
    }

    /**
     * Prints text
     *
     * @param text  string containing the text to be printed - truncated to 34 characters when longer
     * @param align text alignment
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     * @see #ZFP_TEXTALIGNLEFT
     * @see #ZFP_TEXTALIGNRIGHT
     * @see #ZFP_TEXTALIGNCENTER
     */
    public void printText(String text, int align) throws ZFPException
    {
        int newalign = align;
        if (34 <= text.length())
            newalign = ZFP_TEXTALIGNLEFT;

        String data;
        switch (newalign)
        {
            case ZFP_TEXTALIGNRIGHT:
                data = new PrintfFormat("%34s").sprintf(nstrcpy(text, 34));
                break;

            case ZFP_TEXTALIGNCENTER:
            {
                StringBuffer buf = new StringBuffer("                                  "); // 34 spaces
                int pos = (34 - text.length()) / 2;
                data = buf.replace(pos, pos + text.length(), text).toString();
            }
            break;

            default:
                data = new PrintfFormat("%-34s").sprintf(nstrcpy(text, 34));
                break;
        }

		byte[] buff;
		try
		{
			buff = data.getBytes(CP);
		}
		catch(UnsupportedEncodingException ex)
		{
			buff = data.getBytes();
		}	
        sendCommand((byte) 0x37, buff);
    }

    public void printTextESC(String[] textLines, boolean reversed, boolean lowFont) throws ZFPException
    {
        openComm();
        try
        {
            String text = "";

            for (int l = 0; l < textLines.length; l++)
            {
                text += textLines[l];
                if (l + 1 < textLines.length)
                    text += '\n';
            }
            byte[] all = new byte[text.length() + textLines.length + 4];
            byte[] data; // = new byte[text.length()];
            try
            {
                data = text.getBytes(CP);
            }
            catch(UnsupportedEncodingException ex)
            {
                data = text.getBytes();
            }
            byte flag = (byte)(0x80 | (((reversed) ? 1 : 0) << 1) | (((lowFont) ? 1 : 0)));
            all[0] = 0x1B;
            all[1] = 0x4C;
            all[2] = flag;
            System.arraycopy(data, 0, all, 3, data.length);
            all[all.length - 2] = 0x1B;
            all[all.length - 1] = 0x0C;

            checkForZFP();
            checkForZFPBusy();

            if (m_fpLogger != null) //x
            {
                try
                {
                    m_fpLogger.Log(new String(data, CP), true);
                }
                catch (UnsupportedEncodingException e)
                {
                    m_fpLogger.Log(new String(data), true);
                }
            }
            try
            {
                m_port.write(all);
            }
            catch (Exception e)
            {
                throw new ZFPException(e);
            }
        }
        finally
        {
            closeComm();
        }
    }

    public void printBarcode(ZFPBarcodeType type, String barcode, boolean centered) throws ZFPException
    {
        String barcodeFull = type.getDataStartingString() + barcode;
        StringBuffer data = new StringBuffer();
        data.append("P;");
        data.append(type.getBarcodeType());
        data.append(";");
        data.append(String.valueOf(barcodeFull.length()));
        data.append(";");
        data.append(barcodeFull);
        if(centered)
            data.append(";1");

        sendCommand((byte) 0x51, data.toString().getBytes());
    }

    /**
     * Prints duplicate of the last client receipt
     *
     * @throws ZFPException in case of communication error
     */
    public void printDuplicate() throws ZFPException
    {
        sendCommand((byte) 0x3A, null);
    }

    /**
     * Registers official paid out and received on account sums
     *
     * @param oper indicates the exact number operator (1 to 9)
     * @param pass string containing the certain operator password (4 characters)
     * @param type specifies the payment type number (0 to 3)
     * @param sum  specifies the sum
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void officialSums(int oper, String pass, int type, float sum) throws ZFPException
    {
        if ((m_Operators < oper) || (1 > oper) || (3 < type) || (0 > type) || (-999999999.0f > sum) || (9999999999.0f < sum))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(oper);
        data.append(";");
        data.append(new PrintfFormat("%-4s").sprintf(nstrcpy(pass, 4)));
        data.append(";");
        data.append(type);
        data.append(";");
        data.append(getFloatFormat(sum, 2));

        sendCommand((byte) 0x3B, data.toString().getBytes());
    }

    /**
     * Gets item information from FP internal database
     *
     * @param number specifies the item database number (0 to 1000)
     * @return ZFPArticle class - information of the specified item
     * @throws ZFPException                 if the input parameters are incorrect or in case of communication error
     * @see ZFPArticle
     */
    public ZFPArticle getArticleInfo(int number) throws ZFPException
    {
        if ((1000 < number) || (0 > number))
            throw new ZFPException(0x101, m_lang);

        String data = new PrintfFormat("%05d").sprintf(number);
        sendCommand((byte) 0x6B, data.getBytes());
        return new ZFPArticle(number, m_receiveBuf, m_receiveLen, m_lang);
    }

    public ZFPDepartment getDepartmentInfo(int number) throws ZFPException
    {
        if ((100 < number) || (0 > number))
            throw new ZFPException(0x101, m_lang);

        String data = new PrintfFormat("%02d").sprintf(number);
        sendCommand((byte) 0x67, data.getBytes());
        return new ZFPDepartment(number, m_receiveBuf, m_receiveLen, m_lang);
    }

    /**
     * Gets daily sums information for each tax group
     *
     * @return ZFPTaxNumbers class - sums for each tax group
     * @throws ZFPException in case of communication error
     * @see ZFPTaxNumbers
     */
    public ZFPTaxNumbers getDailySums() throws ZFPException
    {
        sendCommand((byte) 0x6D, null);
        return new ZFPTaxNumbers(m_receiveBuf, m_receiveLen, m_lang, ";");
    }

    /**
     * Gets the number of the last issued receipt
     *
     * @return number of the last issued receipt
     * @throws ZFPException in case of communication error
     */
    public int getBonNumber() throws ZFPException
    {
        sendCommand((byte) 0x71, null);
        return Integer.parseInt(getArrayResult()[0]);
    }

    //////////////////////////////////////////////////////////////////////
    // Setup commands & tools
    //////////////////////////////////////////////////////////////////////

    /**
     * Sets the additional payment type names
     *
     * @param type payment type number (1 to 3)
     * @param line payment type name - maximum 10 characters
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setPayType(int type, String line) throws ZFPException
    {
        if ((1 > type) || (3 < type))
            throw new ZFPException(0x101, m_lang);

        String data = Integer.toString(type);
        data += ";";
        data += nstrcpy(line, 10);

        sendCommand((byte) 0x44, data.getBytes());
    }

    /**
     * Sets Fiscal Printer general parameters
     *
     * @param fpnum       Set the Zeka FP POS number (0 to 9999)
     * @param logo        logo printing status (false = not printed, else printed)
     * @param till        cash drawer status (false = no till, else till presence)
     * @param autocut     auto cutter status (false = don't cut receipt in the end automaticaly, else cut the receipt in the end automaticaly)
     * @param transparent transparent external display status (false = not transparernt, else transparent)
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setParameters(int fpnum, boolean logo, boolean till,
                              boolean autocut, boolean transparent) throws ZFPException
    {
        if ((0 > fpnum) || (9999 < fpnum))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%04u").sprintf(fpnum));
        data.append(";");
        data.append(logo ? '1' : '0');
        data.append(";");
        data.append(till ? '1' : '0');
        data.append(";");
        data.append(autocut ? '1' : '0');
        data.append(";");
        data.append(transparent ? '1' : '0');

        sendCommand((byte) 0x45, data.toString().getBytes());
    }

    /**
     * Sets the system date and time of Zeka FP
     *
     * @param cal Calendar class representing the time and date to be set
     * @throws ZFPException in case of communication error
     */
    public void setDateTime(Calendar cal) throws ZFPException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        String data = (sdf.format(cal.getTime()));

        sendCommand((byte) 0x48, data.getBytes());
    }

    /**
     * Sets Zeka FP Header and Footer lines
     *
     * @param line speciffies the exact line to set (1 - 8)
     * @param text header or footer text - truncated to 38 characters when longer
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setClicheLine(int line, String text) throws ZFPException
    {
        if ((1 > line) || (8 < line))
            throw new ZFPException(0x101, m_lang);

        String data = Integer.toString(line);
        data += ";";
        data += nstrcpy(text, 38);

        sendCommand((byte) 0x49, data.getBytes());
    }

    /**
     * Sets Zeka FP operators passwords
     *
     * @param oper speciffies the operator number (1 - 9)
     * @param name string with desired name - truncated to 20 characters when longer
     * @param pass string with desired password to set - truncated to 4 characters when longer
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setOperatorUserPass(int oper, String name, String pass) throws ZFPException
    {
        if ((1 > oper) || (m_Operators < oper))
            throw new ZFPException(0x101, m_lang);

        String opDigitsStr = String.valueOf(Integer.toString(m_Operators).length());
        StringBuffer data = new StringBuffer(new PrintfFormat("%0" + opDigitsStr + "d").sprintf(oper));

        data.append(";");
        data.append(new PrintfFormat("%-20s").sprintf(nstrcpy(name, 20)));
        data.append(";");
        data.append(new PrintfFormat("%-4s").sprintf(nstrcpy(pass, 4)));

        sendCommand((byte) 0x4A, data.toString().getBytes());
    }

    /**
     * Sets Zeka FP item from the internal database
     *
     * @param number the item number in the internal database (0 to 1000)
     * @param name   string with desired item name - truncated to 20 characters when longer
     * @param price  the item price
     * @param taxgrp item tax group attachment
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setArticleInfo(int number, String name, float price, char taxgrp) throws ZFPException
    {
        if ((0 > number) || (5000 < number) || (-999999999.0f > price) || (9999999999.0f < price))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer();
        data.append(new PrintfFormat("%05d").sprintf(number));
        data.append(";");
        data.append(new PrintfFormat("%-20s").sprintf(nstrcpy(name, 20)));
        data.append(";");
        data.append(getFloatFormat(price, 2));
        data.append(";");
        data.append(taxgrp);
        try
        {
            sendCommand((byte) 0x4B, data.toString().getBytes(CP));
        }
        catch(UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
    }

    /**
     * Sets Zeka FP item from the internal database
     *
     * @param number the item number in the internal database (0 to 1000)
     * @param name   string with desired item name - truncated to 20 characters when longer
     * @param price  the item price
     * @param taxgrp item tax group attachment
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setArticleInfo(int number, String name, float price, char taxgrp, int depNo) throws ZFPException
    {
        if ((0 > number) || (5000 < number) || (-999999999.0f > price) || (9999999999.0f < price))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer();
        data.append(new PrintfFormat("%05d").sprintf(number));
        data.append(";");
        data.append(new PrintfFormat("%-20s").sprintf(nstrcpy(name, 20)));
        data.append(";");
        data.append(getFloatFormat(price, 2));
        data.append(";");
        data.append(taxgrp);
        byte b[] = new byte[1];
        b[0] = (byte) (depNo + 0x80);
        try
        {
            data.append(new String(b, CP));
            sendCommand((byte) 0x4B, data.toString().getBytes(CP));
        }
        catch(UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
    }

    /**
     * Sets Zeka FP bitmap logo
     *
     * @param filename name of the file to be uploaded (.BMP)
     * @throws ZFPException if the input parameters are incorrect
     */
    public void setLogoFile(String filename) throws ZFPException
    {
        byte[] buf = new byte[3906];
        buf[0] = (byte) 0x02;
        buf[1] = (byte) 0x39;
        buf[2] = (byte) 0x37;
        buf[3] = (byte) 0x4C;

        try
        {
            FileInputStream fs = new FileInputStream(filename);
            if (3902 != fs.read(buf, 4, 3902))
            {
                fs.close();
                throw new ZFPException(0x108, m_lang);
            }
            fs.close();
            m_port.write(buf);
        }
        catch (Exception e)
        {
            new ZFPException(e);
        }
    }

    /**
     * Sets Zeka FP system date and time based on the PC system clock
     *
     * @throws ZFPException in case of communication error
     */
    public void setLocalDateTime() throws ZFPException
    {
        setDateTime(Calendar.getInstance());
    }

    //////////////////////////////////////////////////////////////////////
    // Reports
    //////////////////////////////////////////////////////////////////////

    /**
     * Starts tax memory special report
     *
     * @throws ZFPException in case of communication error
     */
    public void reportSpecialFiscal() throws ZFPException
    {
        sendCommand((byte) 0x77, null);
    }

    /**
     * Starts tax memory report by block numbers
     *
     * @param detailed    flag for brief or detailed report
     * @param startNumber start block number (0 to 9999)
     * @param endNumber   end block number (0 to 9999)
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void reportFiscalByBlock(boolean detailed, int startNumber, int endNumber) throws ZFPException
    {
        if ((0 > startNumber) || (9999 < startNumber) || (0 > endNumber) || (9999 < endNumber))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%04u").sprintf(startNumber));
        data.append(";");
        data.append(new PrintfFormat("%04u").sprintf(endNumber));

        sendCommand(detailed ? (byte) 0x78 : (byte) 0x79, data.toString().getBytes());
    }

    /**
     * Starts tax memory report by date
     *
     * @param detailed flag for brief or detailed report
     * @param start    start date
     * @param end      end date
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void reportFiscalByDate(boolean detailed, Calendar start, Calendar end) throws ZFPException
    {
        if (start.after(end))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%02u").sprintf(start.get(Calendar.DAY_OF_MONTH)));
        data.append(new PrintfFormat("%02u").sprintf(start.get(Calendar.MONTH)));
        data.append(new PrintfFormat("%02u").sprintf(start.get(Calendar.YEAR)));
        data.append(";");
        data.append(new PrintfFormat("%02u").sprintf(end.get(Calendar.DAY_OF_MONTH)));
        data.append(new PrintfFormat("%02u").sprintf(end.get(Calendar.MONTH)));
        data.append(new PrintfFormat("%02u").sprintf(end.get(Calendar.YEAR)));

        sendCommand(detailed ? (byte) 0x7A : (byte) 0x7B, data.toString().getBytes());
    }

    /**
     * Starts Daily report
     *
     * @param zero     speciffies the report as Zero Daily or X daily ('Z' or 'X')
     * @param extended speciffies the report as brief or detailed
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void reportDaily(boolean zero, boolean extended) throws ZFPException
    {
        String data = zero ? "Z" : "X";
        sendCommand(extended ? (byte) 0x7F : (byte) 0x7C, data.getBytes());
    }

    /**
     * Starts Departments report
     *
     * @param zero     speciffies the report as Zero Daily or X daily ('Z' or 'X')
     * @param extended speciffies the report as brief or detailed
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void reportDepartments(boolean zero) throws ZFPException
    {
        String data = zero ? "Z" : "X";
        sendCommand((byte) 0x76, data.getBytes());
    }

    /**
     * Starts Operators report
     *
     * @param zero true speciffies the report as 'Z' (zero report), false 'X' (information report)
     * @param oper speciffies the operator number (0 - 9; 0 is for all operators)
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void reportOperator(boolean zero, int oper) throws ZFPException
    {
        if ((0 > oper) || (9 < oper))
            throw new ZFPException(0x101, m_lang);

        String data = zero ? "Z" : "X";
        data += ";";
        data += Integer.toString(oper);
        sendCommand((byte) 0x7D, data.getBytes());
    }

    /**
     * Starts Operators report
     *
     * @param zero - true speciffies the report as 'Z' (zero report) false 'X' (information report)
     * @throws ZFPException in case of communication error
     */
    public void reportArticles(boolean zero) throws ZFPException
    {
        String data = zero ? "Z" : "X";
        sendCommand((byte) 0x7E, data.getBytes());
    }

    //////////////////////////////////////////////////////////////////////
    // Service
    //////////////////////////////////////////////////////////////////////

    /**
     * Programming of external display with direct data
     *
     * @param password programming password
     * @param data     data to be programmed (see manual for details)
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setExternalDisplayData(String password, byte[] data) throws ZFPException
    {
        if (101 < data.length)
            throw new ZFPException(0x101, m_lang);

        byte[] buf = new byte[6 + data.length];
        String pass = new PrintfFormat("%-6s").sprintf(nstrcpy(password, 6));
        byte[] passBuf = pass.getBytes();

        System.arraycopy(passBuf, 0, buf, 0, 6);
        System.arraycopy(data, 0, buf, 6, data.length);

        sendCommand((byte) 0x7E, buf);
    }

    /**
     * Programming of external display with external data file
     *
     * @param password programming password
     * @param filename name of file to be send for programming of external display (see manual for details)
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setExternalDisplayFile(String password, String filename) throws ZFPException
    {
        byte[] buf = new byte[101];

        try
        {
            FileInputStream fs = new FileInputStream(filename);
            int read = fs.read(buf, 0, 101);
            if (101 > read)
            {
                byte[] buf2 = new byte[read];
                System.arraycopy(buf, 0, buf2, 0, read);
                setExternalDisplayData(password, buf2);
            } else
                setExternalDisplayData(password, buf);

            fs.close();
        }
        catch (IOException e)
        {
            new ZFPException(e);
        }
    }

    public void setDBdataForClients(int ClientNo, String ClientName, String BuyerName, String ZDDSNum, String Bulstat, String Address) throws ZFPException
    {
        StringBuffer data = new StringBuffer();
        data.append('P');
        data.append(";");
        data.append(new PrintfFormat("%04d").sprintf(ClientNo));
        data.append(";");
        data.append(new PrintfFormat("%-26s").sprintf(nstrcpy(ClientName, 26)));
        data.append(";");
        data.append(new PrintfFormat("%-16s").sprintf(nstrcpy(BuyerName, 16)));
        data.append(";");
        data.append(new PrintfFormat("%-13s").sprintf(nstrcpy(ZDDSNum, 13)));
        data.append(";");
        data.append(new PrintfFormat("%-13s").sprintf(nstrcpy(Bulstat, 13)));
        data.append(";");
        data.append(new PrintfFormat("%-30s").sprintf(nstrcpy(Address, 30)));
        data.append(";");

        try
        {
            sendCommand((byte) 0x52, data.toString().getBytes(CP));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
    }

    public ZFPClientsDBdata getDBclientsData(int number) throws ZFPException
    {
        if ((1000 < number) || (0 > number))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer();
        data.append('R');
        data.append(";");
        data.append(new PrintfFormat("%04d").sprintf(number));
        openComm();

        try
        {
            sendCommand((byte) 0x52, data.toString().getBytes(CP));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ZFPException(e);
        }
        return new ZFPClientsDBdata(number, m_receiveBuf, m_receiveLen, m_lang);
    }

    /**
     * Read the tax memory contents in external data file
     *
     * @param filename filename of target file where the tax memory records are stored
     *                 file format: <br><pre>
     *                  [NBL][CMD][segment number];[record code];[record date];[status];[data]
     *                  �.
     *                  �..
     *                  [NBL][CMD][segment number];[record code];[record date];[status];[data]
     *                  [NBL][CMD][segment number];[@] - end of records
     *                  record code / record type
     *                  00 Manifacture record
     *                  01 Put into operation
     *                  04 Daily report
     *                  05 RAM Reset
     *                  06 Tax percents change
     *                  07 Decimal point change
     *                  </pre>
     * @throws ZFPException if the input parameters are incorrect
     */
    public void readFiscalMemory(String filename) throws ZFPException
    {
        // ToDo
    }

    /**
     * Gets the number of free tax memory  blocks
     *
     * @return the number of free tax memory blocks
     * @throws ZFPException in case of communication error
     */
    public int getFreeFiscalSpace() throws ZFPException
    {
        openComm();
        sendCommand((byte) 0x74, null);
        return Integer.parseInt(getArrayResult()[0]);
    }

    /**
     * Sets manifacture and tax memory numbers of the device
     *
     * @param password       manifacture password - 6 characters
     * @param manifactureNum manifacture number - 6 characters
     * @param fiscalNum      tax memory number - 6 characters
     * @param controlSum     check sum
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setSerialNumber(String password, String manifactureNum,
                                String fiscalNum, String controlSum) throws ZFPException
    {
        StringBuffer data = new StringBuffer(new PrintfFormat("%-6s").sprintf(nstrcpy(password, 6)));
        data.append(";");
        data.append(new PrintfFormat("%-6s").sprintf(nstrcpy(manifactureNum, 6)));
        data.append(";");
        data.append(new PrintfFormat("%-6s").sprintf(nstrcpy(fiscalNum, 6)));
        data.append(";");
        data.append(new PrintfFormat("%-6s").sprintf(nstrcpy(controlSum, 6)));

        sendCommand((byte) 0x40, data.toString().getBytes());
    }

    /**
     * Sets tax number and tax memory numbers of the device
     *
     * @param password  manifacture password - 6 characters
     * @param taxNum    tax number - 15 characters
     * @param fiscalNum tax memory number - 12 characters
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setTaxNumber(String password, String taxNum, String fiscalNum) throws ZFPException
    {
        StringBuffer data = new StringBuffer(new PrintfFormat("%-6s").sprintf(nstrcpy(password, 6)));
        data.append(";1;");
        data.append(new PrintfFormat("%-15s").sprintf(nstrcpy(taxNum, 15)));
        data.append(";");
        data.append(new PrintfFormat("%-12s").sprintf(nstrcpy(fiscalNum, 12)));

        sendCommand((byte) 0x41, data.toString().getBytes());
    }

    /**
     * Puts the device into operation and activates the tax memory
     *
     * @param password service password - 6 characters
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void makeFiscal(String password) throws ZFPException
    {
        String data = new PrintfFormat("%-6s").sprintf(nstrcpy(password, 6));
        data += ";2";

        sendCommand((byte) 0x41, data.getBytes());
    }

    /**
     * Sets the tax percents
     *
     * @param password service password - 6 characters
     * @param tgr1     tax group 1 percentage
     * @param tgr2     tax group 2 percentage
     * @param tgr3     tax group 3 percentage
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setTaxPercents(String password, float tgr1, float tgr2, float tgr3) throws ZFPException
    {
        if ((100.0f < tgr1) || (100.0f < tgr2) || (100.0f < tgr3))
            throw new ZFPException(0x101, m_lang);

        StringBuffer data = new StringBuffer(new PrintfFormat("%-6s").sprintf(nstrcpy(password, 6)));
        data.append(";");
        data.append(new PrintfFormat("%.2f").sprintf(tgr1));
        data.append("%;");
        data.append(new PrintfFormat("%.2f").sprintf(tgr2));
        data.append("%;");
        data.append(new PrintfFormat("%.2f").sprintf(tgr3));
        data.append("%");

        sendCommand((byte) 0x42, data.toString().getBytes());
    }

    /**
     * Sets the decimal point position
     *
     * @param password service password - 6 characters
     * @param point    the decimal point position (0 or 2)
     * @throws ZFPException if the input parameters are incorrect or in case of communication error
     */
    public void setDecimalPoint(String password, int point) throws ZFPException
    {
        if ((0 > point) || (9 < point))
            throw new ZFPException(0x101, m_lang);

        String data = new PrintfFormat("%-6s").sprintf(nstrcpy(password, 6));
        data += ";";
        data += Integer.toString(point);

        sendCommand((byte) 0x43, data.toString().getBytes());
    }

    /**
     * Gets information about current opened receipt
     *
     * @return ZFPReceiptInfo class information about the current receipt
     * @throws ZFPException in case of communication error
     */
    public ZFPReceiptInfo getCurrentReceiptInfo() throws ZFPException
    {
        sendCommand((byte) 0x72, null);
        return new ZFPReceiptInfo(m_receiveBuf, m_receiveLen, m_lang);
    }
}