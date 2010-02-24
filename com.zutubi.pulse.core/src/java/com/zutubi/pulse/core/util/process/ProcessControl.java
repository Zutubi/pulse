package com.zutubi.pulse.core.util.process;

import com.zutubi.pulse.command.PulseCtl;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Utilities for controlling external processes.  Includes some
 * platform-specific magic to go beyond the simplistic Java APIs.
 */
public class ProcessControl
{
    private static final Logger LOG = Logger.getLogger(ProcessControl.class);

    static final String NATIVE_PROCESS_KILL = "pulse.use.native.process.kill";
    
    private static boolean nativeDestroyAvailable = false;
    /**
     * On Windows, the PID is held in a long in the ProcessImpl class. 
     */
    private static Field handleField;
    /**
     * On UNIX-like platforms, the PID is held in a pid field in the
     * UNIXProcess class.
     */
    private static Field pidField;
    private static boolean initialised;

    synchronized static void init()
    {
        if (!initialised)
        {
            File dll = findDLL();
            if (SystemUtils.IS_WINDOWS)
            {
                if (SystemUtils.getBooleanProperty(NATIVE_PROCESS_KILL, true) && dll != null)
                {
                    try
                    {
                        System.load(dll.getAbsolutePath());
                        Class clazz = ProcessControl.class.getClassLoader().loadClass("java.lang.ProcessImpl");
                        handleField = clazz.getDeclaredField("handle");
                        handleField.setAccessible(true);
                        nativeDestroyAvailable = true;
                    }
                    catch (UnsatisfiedLinkError e)
                    {
                        LOG.warning("Unable to load native component: " + e.getMessage(), e);
                    }
                    catch (NoSuchFieldException e)
                    {
                        LOG.warning("Unable to get handle field of Process: " + e.getMessage(), e);
                    }
                    catch (ClassNotFoundException e)
                    {
                        LOG.warning("Unable to load Process class: " + e.getMessage(), e);
                    }
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

    private static File findDLL()
    {
        File dll = null;
        String homePath = System.getProperty(PulseCtl.VERSION_HOME);
        if (homePath == null)
        {
            // Development version?
            dll = new File("com.zutubi.pulse.core/etc/pulse.dll");
        }
        else
        {
            File lib = new File(homePath, "lib");
            if (lib.isDirectory())
            {
                dll = new File(lib, "pulse.dll");
            }
        }

        if (dll == null || !dll.exists())
        {
            return null;
        }

        return dll;
    }

    /**
     * Indicates if platform-specific code for process destruction is
     * available.  Most clients can just call {@link #destroyProcess(Process)}
     * and let it determine the best method of destruction available.
     * 
     * @return true if native destruction is available
     */
    public static boolean isNativeDestroyAvailable()
    {
        init();
        return nativeDestroyAvailable;
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
                return pidField.getInt(p);
            }
            catch (IllegalAccessException e)
            {
                // Fall through.
            }
        }
        
        return 0;
    }

    /**
     * Attempts to end the given external process, and clean up after it.  On
     * some platforms this includes native code to ensure the whole process
     * tree is killed.  At the very least, this method calls {@link Process#destroy()}
     * on the process.
     * 
     * @param p the process to destroy (if null, this method is a no-op)
     */
    public static void destroyProcess(Process p)
    {
        init();

        if (p != null)
        {
            if (nativeDestroyAvailable)
            {
                try
                {
                    long handle = handleField.getLong(p);
                    destroy(handle);
                }
                catch (IllegalAccessException e)
                {
                    // Fall through
                }
            }

            // Always called, so that any additional cleanup is done.
            p.destroy();
        }
    }

    private static native boolean destroy(long handle);
}
