package com.zutubi.pulse.core.scm.process.api;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.util.process.AsyncProcess;
import com.zutubi.pulse.core.util.process.ByteHandler;
import com.zutubi.pulse.core.util.process.LineHandler;
import com.zutubi.util.Constants;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides support for running an external SCM process.  Handles feeding input
 * to the process and sending output to an {@link ScmLineHandler}.  Includes
 * optional support for checking exit codes and timing out on process
 * inactivity.
 */
public class ScmProcessRunner
{
    private String name;
    private ProcessBuilder processBuilder = new ProcessBuilder();
    private int inactivityTimeout = 0;
    private Charset charset = Charset.defaultCharset();

    /**
     * Creates a new runner which will run a command of the given name (e.g.
     * svn, p4, git).
     * 
     * @param name name of the process to run, returned in feedback messages
     */
    public ScmProcessRunner(String name)
    {
        this.name = name;
    }

    /**
     * Returns a map view of the environment used to run the child process.
     * The environment may be modified by updating the map.
     * 
     * @return a map view of the process environment
     * 
     * @see ProcessBuilder#environment()
     */
    public Map<String, String> getEnvironment()
    {
        return processBuilder.environment();
    }

    /**
     * Returns the working directory to use for the child process.
     * 
     * @return the working directory to use for the child process, may be null
     *         in which case the JVM's working directory will be used
     *         
     * @see ProcessBuilder#directory() 
     */
    public File getDirectory()
    {
        return processBuilder.directory();
    }

    /**
     * Sets the working directory to use for the child process.
     * 
     * @param directory directory to run the child process in
     * 
     * @see ProcessBuilder#directory(java.io.File) 
     */
    public void setDirectory(File directory)
    {
        processBuilder.directory(directory);
    }

    /**
     * Returns the current inactivity timeout.
     * 
     * @return the current inactivity timeout (in seconds)
     */
    public int getInactivityTimeout()
    {
        return inactivityTimeout;
    }

    /**
     * Sets the inactivity timeout to use for the child process.  If not zero,
     * the timeout is the maximum number of seconds to allow between detected
     * process activity before timing out the child (presuming it has hung).
     * Activity is defined as startup or output (including error output).
     * <p/>
     * Note that the resolution of the timeout is the nearest 10 seconds.
     * 
     * @param inactivityTimeout timeout to use, in seconds, may be zero to
     *                          disable this feature
     */
    public void setInactivityTimeout(int inactivityTimeout)
    {
        this.inactivityTimeout = inactivityTimeout;
    }

    /**
     * Returns the character set currently in use for converting process output
     * from bytes to strings.
     * 
     * @return the current character set used for process output
     */
    public Charset getCharset()
    {
        return charset;
    }

    /**
     * Sets the character set to use for converting process output bytes to
     * strings before passing it on to the output handler.
     * 
     * @param charset the character set to use for output conversion
     */
    public void setCharset(Charset charset)
    {
        this.charset = charset;
    }

    /**
     * Returns the last command line used for a child process.
     * 
     * @return the last command line used as a space-separated string
     */
    public String getLastCommandLine()
    {
        return StringUtils.join(" ", processBuilder.command());
    }

    /**
     * Runs the SCM child process with the given commands and no input, and
     * checks for a zero exit code.  If the exit code is non-zero an error is
     * raised.
     * 
     * @param handler       handler that will be notified of process output
     * @param commands      command line to use to launch the process, should
     *                      include the command itself along with any arguments
     * @return the exit code of the process
     * @throws ScmException on any error
     * 
     * @see #runProcess(ScmOutputHandler, byte[], boolean, String...) 
     */
    public int runProcess(final ScmLineHandler handler, String... commands) throws ScmException
    {
        return runProcess(handler, true, commands);
    }

    /**
     * Runs the SCM child process with the given commands and no input.
     * 
     * @param handler       handler that will be notified of process output
     * @param checkExitCode if true, the exit code will be checked and an error
     *                      raised if it is non-zero
     * @param commands      command line to use to launch the process, should
     *                      include the command itself along with any arguments
     * @return the exit code of the process
     * @throws ScmException on any error
     * 
     * @see #runProcess(ScmOutputHandler, byte[], boolean, String...) 
     */
    public int runProcess(final ScmLineHandler handler, boolean checkExitCode, String... commands) throws ScmException
    {
        return runProcess(handler, null, checkExitCode, commands);
    }

    /**
     * Runs the SCM child process with the given commands.  This method waits
     * until the process completes (or an error, including a timeout, is
     * raised).  Any input is fed to the process as soon as it is started, and
     * output is read by independent threads to prevent buffers being filled.
     * Received output is converted to strings using the current character set
     * before being passed to the output handler.  If the current inactivity
     * timeout is non-zero, it will be applied to detect process hangs.
     * 
     * @param handler       handler that will be notified of process output,
     *                      may be null in which case output is discarded
     * @param input         input bytes to feed to the process, may be null if
     *                      there are is no input to feed
     * @param checkExitCode if true, the exit code will be checked and an error
     *                      raised if it is non-zero
     * @param commands      command line to use to launch the process, should
     *                      include the command itself along with any arguments
     * @return the exit code of the process
     * @throws ScmException on any error
     */
    public int runProcess(ScmOutputHandler handler, byte[] input, boolean checkExitCode, String... commands) throws ScmException
    {
        final ScmOutputHandler safeHandler = handler == null ?  new ScmOutputHandlerSupport() : handler;

        processBuilder.command(commands);
        String commandLine = getLastCommandLine();
        safeHandler.handleCommandLine(commandLine);

        Process child = startProcess();

        streamInput(child, input);

        final AtomicBoolean activity = new AtomicBoolean(false);
        final StringBuilder stderr = new StringBuilder();
        int exitCode = completeProcess(wrapProcess(child, safeHandler, stderr, activity), safeHandler, activity);
        
        if (checkExitCode && exitCode != 0)
        {
            String message = name + " command: '" + commandLine + "' exited with non-zero exit code: " + exitCode;
            String error = stderr.toString().trim();
            if (StringUtils.stringSet(error))
            {
                message += " (" + error + ")";
            }
                
            throw new ScmException(message);
        }
     
        return exitCode;
    }

    private Process startProcess() throws ScmException
    {
        try
        {
            return processBuilder.start();
        }
        catch (IOException e)
        {
            throw new ScmException(getProcessErrorMessage(e), e);
        }
    }

    private void streamInput(Process child, byte[] input) throws ScmException
    {
        if (input != null)
        {
            try
            {
                OutputStream stdinStream = child.getOutputStream();
                stdinStream.write(input);
                stdinStream.close();
            }
            catch (IOException e)
            {
                throw new ScmException("Error writing to input of " + name + " process", e);
            }
        }
    }

    private AsyncProcess wrapProcess(Process child, final ScmOutputHandler handler, final StringBuilder stderr, final AtomicBoolean activity)
    {
        AsyncProcess async;
        if (handler instanceof ScmLineHandler)
        {
            final ScmLineHandler lineHandler = (ScmLineHandler) handler;
            async = new AsyncProcess(child, new LineHandler()
            {
                public Charset getCharset()
                {
                    return charset;
                }

                public void handle(String line, boolean error)
                {
                    activity.set(true);
                    if (error)
                    {
                        stderr.append(line);
                        stderr.append('\n');
                        lineHandler.handleStderr(line);
                    }
                    else
                    {
                        lineHandler.handleStdout(line);
                    }
                }
            }, true);
        }
        else if (handler instanceof ScmByteHandler)
        {
            async = new AsyncProcess(child, new ByteHandler()
            {
                final ScmByteHandler byteHandler = (ScmByteHandler) handler;
                public void handle(byte[] buffer, int n, boolean error) throws Exception
                {
                    activity.set(true);
                    if (error)
                    {
                        stderr.append(new String(buffer, 0, n, charset.name()));
                        byteHandler.handleStderr(buffer, n);
                    }
                    else
                    {
                        byteHandler.handleStdout(buffer, n);
                    }
                }
            }, true);
        }
        else
        {
            async = new AsyncProcess(child, new ByteHandler()
            {
                public void handle(byte[] buffer, int n, boolean error) throws Exception
                {
                    activity.set(true);
                }
            }, true);
        }
        return async;
    }

    private int completeProcess(AsyncProcess async, ScmOutputHandler safeHandler, AtomicBoolean activity) throws ScmException
    {
        try
        {
            long lastActivityTime = System.currentTimeMillis();
            Integer exitCode;
            do
            {
                safeHandler.checkCancelled();
                exitCode = async.waitFor(10, TimeUnit.SECONDS);
                if (activity.getAndSet(false))
                {
                    lastActivityTime = System.currentTimeMillis();
                }
                else
                {
                    if (inactivityTimeout > 0)
                    {
                        long secondsSinceActivity = (System.currentTimeMillis() - lastActivityTime) / Constants.SECOND;
                        if (secondsSinceActivity >= inactivityTimeout)
                        {
                            async.destroy();
                            throw new ScmException("Timing out " + name + " process after " + secondsSinceActivity + " seconds of inactivity");
                        }
                    }
                }
            }
            while (exitCode == null);

            safeHandler.handleExitCode(exitCode);
            return exitCode;
        }
        catch (InterruptedException e)
        {
            throw new ScmException("Interrupted running " + name + " process", e);
        }
        catch (IOException e)
        {
            throw new ScmException("Error reading output of " + name + " process", e);
        }
        finally
        {
            async.destroy();
        }
    }

    private String getProcessErrorMessage(IOException e)
    {
        String message = "Could not start " + name + " process: " + e.getMessage() + " (";
        Map<String, String> env = processBuilder.environment();
        String pathKey = findPathKey(env);
        if (pathKey == null)
        {
            message += "No PATH found in environment";
        }
        else
        {
            message += "PATH: '" + env.get(pathKey) + "'";
        }

        if (processBuilder.directory() != null)
        {
            message += "; Working Directory: '" + processBuilder.directory().getAbsolutePath() + "'";
        }
        message += ")";
        return message;
    }

    private String findPathKey(Map<String, String> environment)
    {
        for (String key: environment.keySet())
        {
            if (key.equalsIgnoreCase("PATH"))
            {
                return key;
            }
        }

        return null;
    }
}
