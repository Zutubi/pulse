package com.cinnamonbob.shell;

import com.cinnamonbob.util.RandomUtils;
import com.cinnamonbob.util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The Shell provides a native command execution environment.
 */
public abstract class Shell
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
    // property at the moment that the ShellOutputReader was created.
    private final String lineSeparator = (String) java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));

    private ShellOutputReader reader;

    private PrintWriter writer;

    private Process process;

    /**
     *
     */
    private final Map<String, String> environment = new HashMap<String, String>();

    private String path;

    private File directory;

    private InputStream input;

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
                addPath(env.get("PATH"));  //TODO: abstract this.
                env.put("PATH", path);
            }

            process = builder.start();

            input = new PipedInputStream();
            OutputStream output = new PipedOutputStream((PipedInputStream)input);

            reader = new ShellOutputReader(process.getInputStream(), output);
            reader.setLineSeparator(lineSeparator);
            reader.start();

            writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));

            isOpen = true;
        }
        finally
        {
            if (!isOpen)
            {
                // ensure that we close any resources that we have opened
                cleanup();
            }
        }
    }

    /**
     * Execute the specified command within a native os command shell.
     * <p/>
     * This method will block until the execution of the command has been completed.
     *
     * @param cmd
     * @return the commands exit status, or EXIT_STATUS_UNKNOWN if the exit
     *         status could not be determined.
     * @throws IllegalStateException if this shell is not open
     * @see #isOpen()
     * @see #open()
     */
    public int execute(String cmd) throws InterruptedException
    {
        if (!isOpen())
        {
            throw new IllegalStateException("Shell must be opened before it can execute commands.");
        }

        // use the random string to 'tag' the end of the command, allowing us to detect
        // when a command has finished.
        String randomString = RandomUtils.randomString(10);
        reader.setCommandTerminationString(randomString);

        internalExecute(cmd);

        // determine exit status of command... this may not always be possible.
        internalExecute("echo " + randomString + " " + getExitStatusVariable());

        reader.waitFor();

        return reader.getExitStatus();
    }

    public InputStream getInput()
    {
        return input;
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

        cleanup();

        isOpen = false;
    }

    /**
     * Cleanup all of the resources held by this object.
     */
    private void cleanup()
    {
        if (writer != null)
        {
            writer.flush();
            writer.close();
            writer = null;
        }
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
        if (reader != null)
        {
            try
            {
                // do not need to wait long since the process feeding the input stream has closed.
                reader.join(100);
            }
            catch (InterruptedException e)
            {
                // noop.
            }
            finally
            {
                reader = null;
            }
        }
        if (input != null)
        {
            IOUtils.close(input);
        }
    }

    public abstract String getOpenShellCommand();

    public abstract String getCloseShellCommand();

    public abstract String getExitStatusVariable();
}
