package com.zutubi.pulse.core.util.process;

import com.jezhumble.javasysmon.JavaSysMon;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;

/**
 * Utilities for controlling external processes.  Includes some
 * platform-specific magic to go beyond the simplistic Java APIs.
 */
public class ProcessControl
{
    private static final Logger LOG = Logger.getLogger(ProcessControl.class);

    private static final String COMMAND_TASKKILL = "taskkill";
    private static final String FLAG_PID = "/PID";
    private static final String FLAG_FORCE = "/F";
    private static final String FLAG_TREE = "/T";

    private static boolean javasysmonAvailable;

    private static Kernel32 KERNEL32;
    private static boolean taskkillAvailable;
    /**
     * On UNIX-like platforms, the PID is held in a pid field in the
     * UNIXProcess class.  On Windows this is a handle field on the
     * ProcessImpl class.
     */
    private static Field pidField;
    private static boolean initialised;

    synchronized static void init()
    {
        if (!initialised)
        {
            javasysmonAvailable = new JavaSysMon().supportedPlatform();

            if (SystemUtils.IS_WINDOWS)
            {
                try
                {
                    KERNEL32 = (Kernel32)Native.loadLibrary("kernel32", Kernel32.class);

                    Class clazz = ProcessControl.class.getClassLoader().loadClass("java.lang.ProcessImpl");
                    pidField = clazz.getDeclaredField("handle");
                    pidField.setAccessible(true);
                }
                catch (UnsatisfiedLinkError e)
                {
                    LOG.warning("Unable to load Kernel32: " + e.getMessage(), e);
                }
                catch (Throwable e)
                {
                    LOG.warning("Unable to get handle field of Process: " + e.getMessage(), e);
                }

                taskkillAvailable = SystemUtils.findInPath(COMMAND_TASKKILL) != null;
            }
            else
            {
                try
                {
                    Class clazz = ProcessControl.class.getClassLoader().loadClass("java.lang.UNIXProcess");
                    pidField = clazz.getDeclaredField("pid");
                    pidField.setAccessible(true);
                }
                catch (Exception e)
                {
                    LOG.finest("Unable to access UNIXProcess.pid: " + e.getMessage(), e);
                }
            }

            initialised = true;
        }
    }

    /**
     * Indicates if it is possible to get PIDs for processors on this platform.
     * 
     * @return true if process ids (PIDs) are available
     */
    public static boolean isPidAvailable()
    {
        init();
        return pidField != null;
    }

    /**
     * Retrieves the process id (PID) of the given process, if it is possible
     * on this platform.  Note that the process may have already ended, so the
     * returned PID may not refer to a running process.
     * 
     * @param p the process to get the PID for
     * @return the PID of the given process, or zero if the PID cannot be
     *         determined
     */
    public static int getPid(Process p)
    {
        init();
     
        if (p != null && pidField != null)
        {
            try
            {
                if (SystemUtils.IS_WINDOWS)
                {
                    long handle = pidField.getLong(p);
                    return KERNEL32.GetProcessId(new WinNT.HANDLE(new Pointer(handle)));
                }
                else
                {
                    return pidField.getInt(p);
                }
            }
            catch (Throwable e)
            {
                // Fall through.
                LOG.warning(e);
            }
        }
        
        return 0;
    }

    /**
     * Indicates if native destroy is available.
     *
     * @return true if native destroy code is available
     */
    public static boolean isNativeDestroyAvailable()
    {
        return isPidAvailable() && (taskkillAvailable || javasysmonAvailable);
    }

    /**
     * Attempts to end the given external process, and clean up after it.  On
     * some platforms this includes native code to ensure the whole process
     * tree is killed.  At the very least, this method calls {@link Process#destroy()}
     * on the process.
     * 
     * @param p the process to destroy (if null, this method is a no-op)
     * @return returns a short string indicating the method used to terminate
     *         the process (e.g. "Java APIs", "taskkill").
     */
    public static String destroyProcess(Process p)
    {
        init();
        String method = "Java APIs";
        if (p != null)
        {
            if (isPidAvailable())
            {
                int pid = getPid(p);
                if (pid != 0)
                {
                    if (taskkillAvailable)
                    {
                        method = "taskkill utility";
                        killWithTaskkill(pid);
                    }
                    else if (javasysmonAvailable)
                    {
                        method = "native code";
                        JavaSysMon monitor = new JavaSysMon();
                        monitor.killProcessTree(pid, false);
                    }
                }
            }

            // Always called, so that any additional cleanup is done.
            p.destroy();
        }
        
        return method;
    }

    private static void killWithTaskkill(int pid)
    {
        Process process = null;
        try
        {
            ProcessBuilder builder = new ProcessBuilder(COMMAND_TASKKILL, FLAG_PID, Integer.toString(pid), FLAG_FORCE, FLAG_TREE);
            builder.redirectErrorStream(true);
            process = builder.start();
            process.getOutputStream().close();
            InputStreamReader outputReader = new InputStreamReader(process.getInputStream());
            StringWriter outputWriter = new StringWriter();
            IOUtils.joinReaderToWriter(outputReader, outputWriter);
            int exitCode = process.waitFor();
            outputReader.close();
            if (exitCode != 0)
            {
                LOG.warning("Unable to kill process " + pid + " with taskkill: taskkill returned exit code " + exitCode + ", output:\n" + outputWriter.toString());
            }
        }
        catch (IOException e)
        {
            LOG.warning("Unable to kill process " + pid + " with taskkill: " + e.getMessage(), e);
        }
        catch (InterruptedException e)
        {
            LOG.warning("Interrupted while trying to kill process " + pid + " with taskkill: " + e.getMessage(), e);
        }
        finally
        {
            if (process != null)
            {
                IOUtils.close(process.getErrorStream());
                IOUtils.close(process.getInputStream());
                process.destroy();
            }
        }
    }
}
