package com.cinnamonbob.shell;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <class-comment/>
 */
public class StreamReader extends Thread
{
    private static final Logger LOG = Logger.getLogger(StreamReader.class.getName());

    final InputStream input;
    final OutputStream output;

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the StreamReader was created.
    private final String lineSeparator = (String) java.security.AccessController.doPrivileged(
               new sun.security.action.GetPropertyAction("line.separator"));

    /**
     *
     * @param input
     * @param output
     */
    public StreamReader(InputStream input, OutputStream output)
    {
        this.input = input;
        this.output = output;
    }

    /**
     *
     */
    public void run()
    {
        try
        {
            OutputStreamWriter writer = new OutputStreamWriter(output);
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while ((line = br.readLine()) != null)
            {
                writer.write(line);
                writer.write(lineSeparator);
                writer.flush();
            }
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Error reading input.", e);
        }
        finally
        {
//            IOUtils.close(input);
        }
    }



}
