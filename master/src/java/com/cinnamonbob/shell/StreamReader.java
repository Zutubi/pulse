package com.cinnamonbob.shell;

import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.util.logging.Logger;

import java.io.*;

/**
 * <class-comment/>
 */
public class StreamReader extends Thread
{
    private static final Logger LOG = Logger.getLogger(StreamReader.class.getName());

    final InputStream input;
    final OutputStream output;

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
            String line;
            while ((line = br.readLine()) != null)
            {
                // examine the last char of the data.
                if (line.length() > 0)
                {
                    char lastChar = line.charAt(line.length() - 1);
                    if (lastChar == Shell.END_OF_COMMAND)
                    {
                        writer.write(Constants.LINE_SEPARATOR);
                        writer.flush();
                        continue;
                    }
                }
                writer.write(line);
                writer.write(Constants.LINE_SEPARATOR);
                writer.flush();
            }
        }
        catch (IOException e)
        {
            LOG.severe("Error reading input.", e);
        }
    }
}
