package com.zutubi.pulse.bootstrap.conf;

import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.Constants;

import java.io.*;
import java.util.Properties;
import java.util.Enumeration;

/**
 * <class-comment/>
 */
public class PropertiesWriter
{
    public void write(File config, Properties props) throws IOException
    {
        // each time we write a property value, we want to remove it from the properties remaining to be
        // written. So that we do not modify the original properties object, we create a copy.
        Properties copy = new Properties();
        copy.putAll(props);

        // convert the property values according to the Properties object convensions.
        convert(copy);

        StringBuffer contents = new StringBuffer();

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(config)));
            String line;
            while ((line = reader.readLine()) != null)
            {
                // is there a property on this line that we need to update?
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("#"))
                {
                    // is a comment, just append it.
                    contents.append(line);
                }
                else if (trimmedLine.length() == 0)
                {
                    contents.append(line);
                }
                else
                {
                    // we are dealing with a property here.
                    String propertyName = trimmedLine.substring(0, trimmedLine.indexOf('='));
                    if (copy.containsKey(propertyName))
                    {
                        contents.append(propertyName).append("=").append(copy.getProperty(propertyName));
                        copy.remove(propertyName);
                    }
                    else
                    {
                        contents.append(line);
                    }
                }
                contents.append(Constants.LINE_SEPARATOR);
            }

            // write the remaining properties to the end of the file.
            Enumeration remainingNames = copy.propertyNames();
            while (remainingNames.hasMoreElements())
            {
                String propertyName = (String) remainingNames.nextElement();
                contents.append(propertyName).append("=").append(copy.getProperty(propertyName));
                contents.append(Constants.LINE_SEPARATOR);
            }
        }
        finally
        {
            IOUtils.close(reader);
        }

        // write the updated contents.
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config)));
            writer.append(contents.toString());
            writer.flush();
        }
        finally
        {
            IOUtils.close(writer);
        }

    }

    private void convert(Properties copy)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try
        {
            copy.store(os, null);
            BufferedReader reader = new BufferedReader(new StringReader(os.toString()));
            String line = reader.readLine();
            while (line != null)
            {
                if (line.indexOf('=') != -1)
                {
                    String propertyName = line.substring(0, line.indexOf('='));
                    String value = line.substring(line.indexOf('=') + 1);
                    copy.setProperty(propertyName, value);
                }
                line = reader.readLine();
            }
        }
        catch (IOException e)
        {
            // ignore
        }
    }
}
