/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10)
// Source File Name:   THttpClient.java

package org.apache.thrift.transport;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.*;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

// Referenced classes of package org.apache.thrift.transport:
//            TTransport, TTransportException

public class THttpClient extends TTransport
{

    public THttpClient(String url)
        throws TTransportException
    {
        url_ = null;
        inputStream_ = null;
        connectTimeout_ = 0;
        readTimeout_ = 0;
        customHeaders_ = null;
        try
        {
            url_ = new URL(url);
        }
        catch(IOException iox)
        {
            throw new TTransportException(iox);
        }
    }

    public void setConnectTimeout(int timeout)
    {
        connectTimeout_ = timeout;
    }

    public void setReadTimeout(int timeout)
    {
        readTimeout_ = timeout;
    }

    public void setCustomHeaders(Map headers)
    {
        customHeaders_ = headers;
    }

    public void setCustomHeader(String key, String value)
    {
        if(customHeaders_ == null)
            customHeaders_ = new HashMap();
        customHeaders_.put(key, value);
    }

	 // プロキシ対応
    public void setProxy(String hostname, int portNumber)
    {
    	proxy_ = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, portNumber));
    }

    public void open()
    {
    }

    public void close()
    {
        if(inputStream_ != null)
        {
            try
            {
                inputStream_.close();
            }
            catch(IOException ioexception) { }
            inputStream_ = null;
        }
    }

    public boolean isOpen()
    {
        return true;
    }

    public int read(byte buf[], int off, int len)
        throws TTransportException
    {
        if(inputStream_ == null)
            throw new TTransportException("Response buffer is empty, no request.");
        try
        {
            int ret = inputStream_.read(buf, off, len);
            if(ret == -1)
                throw new TTransportException("No more data available.");
            else
                return ret;
        }
        catch(IOException iox)
        {
            throw new TTransportException(iox);
        }
    }

    public void write(byte buf[], int off, int len)
    {
        requestBuffer_.write(buf, off, len);
    }

    public void flush()
        throws TTransportException
    {
        byte data[] = requestBuffer_.toByteArray();
        requestBuffer_.reset();
        try
        {
        	HttpURLConnection connection;
        	if (proxy_ != null) {
        		 // プロキシ対応
                connection = (HttpURLConnection)url_.openConnection(proxy_);
        	} else {
                connection = (HttpURLConnection)url_.openConnection();
        	}
            if(connectTimeout_ > 0)
                connection.setConnectTimeout(connectTimeout_);
            if(readTimeout_ > 0)
                connection.setReadTimeout(readTimeout_);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-thrift");
            connection.setRequestProperty("Accept", "application/x-thrift");
            connection.setRequestProperty("User-Agent", "Java/THttpClient");
            if(customHeaders_ != null)
            {
                java.util.Map.Entry header;
                for(Iterator iterator = customHeaders_.entrySet().iterator(); iterator.hasNext(); connection.setRequestProperty((String)header.getKey(), (String)header.getValue()))
                    header = (java.util.Map.Entry)iterator.next();

            }
            connection.setDoOutput(true);
            connection.connect();
            connection.getOutputStream().write(data);
            int responseCode = connection.getResponseCode();
            if(responseCode != 200)
                throw new TTransportException((new StringBuilder("HTTP Response code: ")).append(responseCode).toString());
            inputStream_ = connection.getInputStream();
        }
        catch(IOException iox)
        {
            throw new TTransportException(iox);
        }
    }

    private URL url_;
    private final ByteArrayOutputStream requestBuffer_ = new ByteArrayOutputStream();
    private InputStream inputStream_;
    private int connectTimeout_;
    private int readTimeout_;
    private Map customHeaders_;
    private Proxy proxy_; // プロキシ対応
}


/*
	DECOMPILATION REPORT

	Decompiled from: C:\Program Files\eclipse\workspace\evernote\lib\libthrift.jar
	Total time: 16 ms
	Jad reported messages/errors:
	Exit status: 0
	Caught exceptions:
*/