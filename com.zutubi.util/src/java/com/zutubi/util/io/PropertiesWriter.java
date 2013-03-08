package com.zutubi.util.io;

import com.zutubi.util.SystemUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 * The properties writer will keep the format of the file being written to.  That is, if the property
 * being written to the file already exists in that file, the value is replaced.  Any unknown properties
 * are placed at the end of the file.  This is very useful for properties files that contain some
 * documentation / comments that you wish to preserve.
 */
public class PropertiesWriter
{
    /**
     * Write the specified properties to the given file.  If the properties already exist within the file, then
     * the existing values will be replaced by the new values.  Any properties that are not present in the config file
     * will be written at the end.
     *
     * This method will ensure that any existing comments within the file are maintained.  
     *
     * @param config file to which the properties will be written.
     * @param props to be written to the file.
     *
     * @throws IOException if a problem occurs writing the properties to file.
     */
    public void write(File config, Properties props) throws IOException
    {
        // each time we write a property value, we want to remove it from the properties remaining to be
        // written. So that we do not modify the original properties object, we create a copy.
        Properties copy = new Properties();
        copy.putAll(props);

        // Convert the property values according to the Properties object conventions. Why? To ensure that
        // when we make a change to the config file the properties object is able to accurately read the values.
        convert(copy);

        StringBuilder builder = new StringBuilder();

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
                    builder.append(line);
                }
                else if (trimmedLine.length() == 0)
                {
                    builder.append(line);
                }
                else
                {
                    // We are dealing with a property here.
                    String propertyName = trimmedLine.substring(0, trimmedLine.indexOf('='));
                    if (copy.containsKey(propertyName))
                    {
                        builder.append(propertyName).append("=").append(copy.getProperty(propertyName));
                        copy.remove(propertyName);

                        // If the property we are replacing is a multi line property, we need to remove all of it.
                        boolean isMultiLine = line.endsWith("\\");
                        while (isMultiLine)
                        {
                            line = reader.readLine();
                            if (line == null) // reached the end of the while loop.
                            {
                                break;
                            }
                            isMultiLine = line.endsWith("\\");
                        }
                    }
                    else
                    {
                        // We are not updating this property, so write it out as is.
                        boolean isMultiLine = line.endsWith("\\");
                        while (isMultiLine)
                        {
                            builder.append(line);
                            builder.append(SystemUtils.LINE_SEPARATOR);
                            line = reader.readLine();
                            if (line == null) // reached the end of the while loop.
                            {
                                break;
                            }                            
                            isMultiLine = line.endsWith("\\");
                        }
                        builder.append(line);
                    }
                }
                builder.append(SystemUtils.LINE_SEPARATOR);
            }

            // Write the remaining properties to the end of the file.
            Enumeration remainingNames = copy.propertyNames();
            while (remainingNames.hasMoreElements())
            {
                String propertyName = (String) remainingNames.nextElement();
                builder.append(propertyName).append("=").append(copy.getProperty(propertyName));
                builder.append(SystemUtils.LINE_SEPARATOR);
            }
        }
        finally
        {
            IOUtils.close(reader);
        }

        // Write the updated contents.
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config)));
            writer.append(builder.toString());
            writer.flush();
        }
        finally
        {
            IOUtils.close(writer);
        }

    }

    /**
     * Convert the property values according to the Properties object convensions.  The most stable way to do
     * this is to use the properties object to render its content to a string, and then read that string.
     * 
     * @param properties instance whose contents are going to be converted according to the properties object conventions.
     */
    private void convert(Properties properties)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try
        {
            properties.store(os, null);
            BufferedReader reader = new BufferedReader(new StringReader(os.toString()));
            String line = reader.readLine();
            while (line != null)
            {
                if (line.indexOf('=') != -1)
                {
                    String propertyName = line.substring(0, line.indexOf('='));
                    String value = line.substring(line.indexOf('=') + 1);
                    properties.setProperty(propertyName, value);
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
