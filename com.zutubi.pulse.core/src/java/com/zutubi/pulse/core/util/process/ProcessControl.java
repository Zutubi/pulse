package com.zutubi.pulse.core.util.process;

import com.jezhumble.javasysmon.JavaSysMon;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.logging.Logger;

import java.lang.reflect.Field;

/**
 * Utilities for controlling external processes.  Includes some
 * platform-specific magic to go beyond the simplistic Java APIs.
 */
public class ProcessControl
{
    private static final Logger LOG = Logger.getLogger(ProcessControl.class);

    private static Kernel32 KERNEL32;
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
        return isPidAvailable() && new JavaSysMon().supportedPlatform();
    }

    /**
     * Attempts to end the given external process, and clean up after it.  On
     * some platforms this includes native code to ensure the whole process
     * tree is killed.  At the very least, this method calls {@link Process#destroy()}
     * on the process.
     * 
     * @param p the process to destroy (if null, this method is a no-op)
     * @return true if native process termination is being used
     */
    public static boolean destroyProcess(Process p)
    {
        init();
        boolean usingNative = false;
        if (p != null)
        {
            if (isPidAvailable())
            {
                int pid = getPid(p);
                if (pid != 0)
                {
                    JavaSysMon monitor = new JavaSysMon();
                    if (monitor.supportedPlatform())
                    {
                        monitor.killProcessTree(pid, false);
                        usingNative = true;
                    }
                }
            }

            // Always called, so that any additional cleanup is done.
            p.destroy();
        }
        
        return usingNative;
    }
}
