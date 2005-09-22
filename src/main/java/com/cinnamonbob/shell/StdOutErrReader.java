package com.cinnamonbob.shell;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
class StdOutErrReader extends Thread
{
    /**
     *
     */
    private static final Logger LOG = Logger.getLogger(StdOutErrReader.class.getName());

    /**
     * The standard out / standard error stream from the shell process.
     */
    private final InputStream input;

    /**
     * The output stream to which this reader writes data it receives from the shell process.
     */
    private final OutputStream output;

    private String lineSeparator = null;

    private String commandTerminationString = null;

    private boolean commandComplete;

    private int commandExitStatus;

    /**
     * @param input
     * @param output
     */
    protected StdOutErrReader(InputStream input, OutputStream output)
    {
        this.input = input;
        this.output = output;
    }

    protected void setLineSeparator(String str)
    {
        lineSeparator = str;
    }

    protected void setCommandTerminationString(String str)
    {
        commandTerminationString = str;
        commandComplete = false;
        commandExitStatus = Shell.EXIT_STATUS_UNKNOWN;
    }

    protected synchronized void waitFor()
    {
        while (!commandComplete)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                // noop.
            }
        }
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
                // look for command termination string.
                if (line.contains(commandTerminationString))
                {
                    if (line.startsWith(commandTerminationString))
                    {
                        synchronized (this)
                                {
                                    commandComplete = true;
                                    String exitStatus = line.substring(commandTerminationString.length() + 1);
                                    try
                                    {
                                        commandExitStatus = Integer.parseInt(exitStatus);
                                    }
                                    catch (NumberFormatException e)
                                    {
                                        commandExitStatus = Shell.EXIT_STATUS_UNKNOWN;
                                    }
                                    writer.flush();
                                    notifyAll();
                                }
                    }
                    // hack to make the out put a little cleaner..
                    else if (line.contains("echo " + commandTerminationString))
                    {
                        // ignore it, its just windows echoing the 'echo' command.
                    } else
                    {
                        // legit output.
                        writer.write(line);
                        writer.write(lineSeparator);
                    }
                } else
                {
                    writer.write(line);
                    writer.write(lineSeparator);
                }
            }
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Error reading input.", e);
        }
        finally
        {
            // make sure that we notify any threads currently waiting.
            synchronized (this)
                    {
                        commandComplete = true;
                        commandExitStatus = Shell.EXIT_STATUS_UNKNOWN;
                        notifyAll();
                    }
        }
    }

    public int getExitStatus()
    {
        return commandExitStatus;
    }

    public synchronized void waitFor(long millis)
    {
        try
        {
            wait(millis);
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }
}

