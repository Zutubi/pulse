package com.cinnamonbob.shell;

import com.cinnamonbob.util.RandomUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The Shell provides a native command execution environment.
 */
public class Shell
{
    /**
     * This indicates that the command exit status is unknown.
     */
    public static final int EXIT_STATUS_UNKNOWN = Integer.MIN_VALUE;

    /**
     * Indicates the shells open status.
     */
    private boolean isOpen;

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the StdOutErrReader was created.
    private final String lineSeparator = (String) java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));

    private StdOutErrReader stdOutReader;

    private PrintWriter writer;

    private Process process;

    private final NativeShellConfiguration config = new NativeShellConfiguration();

    /**
     *
     */
    private final Map<String, String> environment = new HashMap<String, String>();

    private String path;

    private File directory;

    private PipedInputStream input;

    public Shell()
    {
        // initialise the environment.
        environment.putAll(System.getenv());
    }

    public Map<String, String> getEnvironment()
    {
        return this.environment;
    }

    public void addPath(String path)
    {
        if (this.path == null)
        {
            this.path = "";
        }
        this.path = this.path + File.pathSeparator + path;
    }

    /**
     * Set the initial working directory of the native shell.
     *
     * @param directory
     */
    //TODO: Need to set a default for the initial working directory.
    public void setDirectory(File directory)
    {
        this.directory = directory;
    }

    /**
     * @return true if this shell has been opened and is currently open, false otherwise.
     * @see #open()
     * @see #close()
     */
    public boolean isOpen()
    {
        return isOpen;
    }

    /**
     * @throws IOException
     * @throws IllegalStateException if this shell is already open.
     * @see #isOpen()
     */
    public void open() throws IOException
    {
        if (isOpen())
        {
            throw new IllegalStateException("Shell is already open.");
        }

        try
        {
            ProcessBuilder builder = new ProcessBuilder(getOpenShellCommand());
            builder.redirectErrorStream(true);
            if (directory != null)
            {
                builder.directory(directory);
            }

            // update environment and path.
            Map<String, String> env = builder.environment();
            env.clear();
            env.putAll(environment);
            if (path != null)
            {
                addPath(env.get(config.getPathVariable()));
                env.put(config.getPathVariable(), path);
            }

            process = builder.start();

            input = new PipedInputStream();
            // will be cleaned up by the StdOutErrReader during its cleanup.
            PipedOutputStream output = new PipedOutputStream(input);

            stdOutReader = new StdOutErrReader(process.getInputStream(), output);
            stdOutReader.setLineSeparator(lineSeparator);
            stdOutReader.start();

            writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));

            isOpen = true;
        }
        finally
        {
            if (!isOpen)
            {
                // ensure that we close any resources that we have opened
                if (process != null)
                {
                    process.destroy();
                }
            }
        }
    }

    /**
     * Execute the specified command within a native os command shell.
     * <p/>
     * This method will block until the execution of the command has been completed.
     *
     * @param cmd

     * @throws IllegalStateException if this shell is not open
     * @see #isOpen()
     * @see #open()
     */
    public void execute(String cmd) throws InterruptedException
    {
        if (!isOpen())
        {
            throw new IllegalStateException("Shell must be opened before it can execute commands.");
        }

        // use the random string to 'tag' the end of the command, allowing us to detect
        // when a command has finished.
        String randomString = RandomUtils.randomString(10);
        stdOutReader.setCommandTerminationString(randomString);

        internalExecute(cmd);

        // determine exit status of command... this may not always be possible.
        internalExecute("echo " + randomString + " " + getExitStatusVariable());
    }

    public InputStream getInput()
    {
        return input;
    }

    public int getExitStatus()
    {
        return stdOutReader.getExitStatus();
    }

    public void waitFor()
    {
        stdOutReader.waitFor();
    }

    public void waitFor(long millis)
    {
        stdOutReader.waitFor(millis);
    }

    public void kill()
    {
        process.destroy();
        isOpen = false;
    }

    private void internalExecute(String command)
    {
        writer.write(command + lineSeparator);
        writer.flush();
    }

    /**
     * @throws InterruptedException
     */
    public void close() throws InterruptedException
    {
        if (!isOpen())
        {
            throw new IllegalStateException("Shell is not open.");
        }

        internalExecute(getCloseShellCommand());

        // the process exiting has a number of important side effects.
        // a) stdout/stderr/stdin streams close.
        // b) StdOutStdErrReader threads input stream closes, so it exits.
        // c) stdouterr thread closes its end of the PipedStream resulting in
        //    EOF for anyone reading from the input end.
        // so, everything is closed.
        if (process != null)
        {
            try
            {
                int shellExitStatus = process.waitFor();
            }
            catch (InterruptedException e)
            {
                // noop.
            }
            finally
            {
                process = null;
            }
        }

        isOpen = false;
    }

    public String getOpenShellCommand()
    {
        return config.getOpenShellCommand();
    }

    public String getCloseShellCommand()
    {
        return config.getCloseShellCommand();
    }

    public String getExitStatusVariable()
    {
        return config.getExitStatusVariable();
    }
}
