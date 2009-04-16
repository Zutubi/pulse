package com.zutubi.pulse.core.util.process;

import com.zutubi.pulse.command.PulseCtl;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.lang.reflect.Field;

/**
 */
public class ProcessControl
{
    private static final Logger LOG = Logger.getLogger(ProcessControl.class);

    static final String NATIVE_PROCESS_KILL = "pulse.use.native.process.kill";
    
    static boolean nativeAvailable = false;
    private static Field handleField;
    private static boolean initialised;

    synchronized static boolean init()
    {
        if (!initialised)
        {
            File dll = findDLL();
            if (SystemUtils.getBooleanProperty(NATIVE_PROCESS_KILL, true) && SystemUtils.IS_WINDOWS && dll != null)
            {
                try
                {
                    System.load(dll.getAbsolutePath());
                    Class clazz = ProcessControl.class.getClassLoader().loadClass("java.lang.ProcessImpl");
                    handleField = clazz.getDeclaredField("handle");
                    handleField.setAccessible(true);
                    nativeAvailable = true;
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
                    LOG.warning("Unable to get load Process class: " + e.getMessage(), e);
                }
            }

            initialised = true;
        }

        return nativeAvailable;
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

    public static void destroyProcess(Process p)
    {
        init();

        if (p != null)
        {
            if (nativeAvailable)
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
