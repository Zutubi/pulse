package com.zutubi.util.io;

import com.google.common.io.ByteStreams;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.zip.ZipFile;

/**
 * A utility class for standard IO operations.
 *
 * @author Daniel Ostermeier
 */
public class IOUtils
{
    private static final Logger LOG = Logger.getLogger(IOUtils.class);

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
        write(properties, dest, "");
    }

    public static void write(Properties properties, File dest, String comment) throws IOException
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(dest);
            properties.store(out, comment);
        }
        finally
        {
            IOUtils.close(out);
        }
    }

    /**
     * This utility method calls {@link java.io.Flushable#flush()} on the
     * specified object, handling the case where the parameter is null and
     * swallowing any exceptions.
     *
     * @param flushable the instance on which flush will be called.
     */
    public static void flush(Flushable flushable)
    {
        try
        {
            if (flushable != null)
            {
                flushable.flush();
            }
        }
        catch (IOException e)
        {
            LOG.finest(e);
        }
    }

    public static void close(Closeable closeable)
    {
        try
        {
            if (closeable != null)
            {
                closeable.close();
            }
        }
        catch (IOException e)
        {
            LOG.finest(e);
        }
    }

    public static void close(ZipFile zipFile)
    {
        try
        {
            if (zipFile != null)
            {
                zipFile.close();
            }
        }
        catch (IOException e)
        {
            LOG.finest(e);
        }
    }

    public static void downloadFile(URL url, File destination) throws IOException
    {
        FileOutputStream fos = null;
        InputStream urlStream = null;

        try
        {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            int code = urlConnection.getResponseCode();
            if(code != 200)
            {
                String error = "Host returned code " + Integer.toString(code);
                String message = urlConnection.getResponseMessage();

                if(message != null)
                {
                    error += ": " + message;
                }

                throw new IOException(error);
            }

            // take url connection input stream and write contents to file
            fos = new FileOutputStream(destination);
            urlStream = urlConnection.getInputStream();
            ByteStreams.copy(urlStream, fos);
        }
        finally
        {
            IOUtils.close(urlStream);
            IOUtils.close(fos);
        }
    }

    /**
     * Copy the contents of the template file to the destination file.  This copy will filter out any lines
     * that begin with '###'
     * 
     * @param template
     * @param destination
     *
     * @throws IOException if there is a problem copying the template.
     */
    public static void copyTemplate(File template, File destination) throws IOException
    {
        File parentFile = destination.getParentFile();
        if (!parentFile.isDirectory() && !parentFile.mkdirs())
        {
            throw new IOException("Unable to create parent directory '" + parentFile.getAbsolutePath() + "' for config file");
        }
        if (!destination.createNewFile())
        {
            throw new IOException("Unable to create config file '" + destination.getAbsolutePath() + "'");
        }

        BufferedReader reader = null;
        BufferedWriter writer = null;

        try
        {
            reader = new BufferedReader(new FileReader(template));
            writer = new BufferedWriter(new FileWriter(destination));

            String line;
            boolean doneSkipping = false;

            while((line = reader.readLine()) != null)
            {
                if(doneSkipping || !line.startsWith("###"))
                {
                    doneSkipping = true;
                    writer.write(line);
                    writer.write('\n');
                }
            }
        }
        finally
        {
            IOUtils.close(reader);
            IOUtils.close(writer);
        }
    }
}
