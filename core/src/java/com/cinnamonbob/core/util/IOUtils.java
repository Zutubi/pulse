package com.cinnamonbob.core.util;

import com.cinnamonbob.util.logging.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * A utility class for standard IO operations.
 *
 * @author Daniel Ostermeier
 */
public class IOUtils
{
    private static final Logger LOG = Logger.getLogger(IOUtils.class);

    public static void close(Socket s)
    {
        try
        {
            if (s != null)
            {
                s.close();
            }
        }
        catch (IOException e)
        {
            LOG.finest(e);
        }
    }

    public static Properties read(File f) throws IOException
    {
        return read(new FileInputStream(f));
    }

    public static Properties read(InputStream input) throws IOException
    {
        try
        {
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    public static void write(Properties properties, File dest) throws IOException
    {
        write(properties, new FileOutputStream(dest));
    }

    public static void write(Properties properties, OutputStream out) throws IOException
    {
        try
        {
            properties.store(out, "");
        }
        finally
        {
            IOUtils.close(out);
        }
    }

    public static void close(InputStream i)
    {
        try
        {
            if (i != null)
            {
                i.close();
            }
        }
        catch (IOException e)
        {
            LOG.finest(e);
        }
    }

    public static void close(Reader r)
    {
        try
        {
            if (r != null)
            {
                r.close();
            }
        }
        catch (IOException e)
        {
            LOG.finest(e);
        }
    }

    public static void close(ServerSocket s)
    {
        try
        {
            if (s != null)
            {
                s.close();
            }
        }
        catch (IOException e)
        {
            LOG.finest(e);
        }
    }

    public static void close(OutputStream outStream)
    {
        try
        {
            if (outStream != null)
            {
                outStream.close();
            }
        }
        catch (IOException e)
        {
            LOG.finest(e);
        }
    }

    public static void close(Writer w)
    {
        try
        {
            if (w != null)
            {
                w.close();
            }
        }
        catch (IOException e)
        {
            LOG.finest(e);
        }
    }

    public static void joinStreams(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[1024];
        int n;

        while ((n = input.read(buffer)) > 0)
        {
            output.write(buffer, 0, n);
        }
    }

    public static void joinReaderToWriter(Reader reader, Writer writer) throws IOException
    {
        char[] buffer = new char[1024];
        int n;

        while ((n = reader.read(buffer)) > 0)
        {
            writer.write(buffer, 0, n);
        }
    }

    public static void copyFile(File fromFile, File toFile) throws IOException
    {
        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        try
        {
            inStream = new FileInputStream(fromFile);
            outStream = new FileOutputStream(toFile);
            joinStreams(inStream, outStream);
        }
        finally
        {
            close(inStream);
            close(outStream);
        }
    }

    public static String inputStreamToString(InputStream is) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        IOUtils.joinStreams(is, os);
        return os.toString();
    }

    public static String fileToString(File file) throws IOException
    {
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(file);
            return inputStreamToString(is);
        }
        finally
        {
            close(is);
        }
    }

}
