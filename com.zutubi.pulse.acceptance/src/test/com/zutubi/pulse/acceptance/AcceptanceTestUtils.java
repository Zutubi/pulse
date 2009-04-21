package com.zutubi.pulse.acceptance;

import com.zutubi.util.Condition;
import com.zutubi.util.TextUtils;

import java.io.File;

public class AcceptanceTestUtils
{
    public static File getWorkingDirectory()
    {
        File workingDir = new File("./working"); // from IDEA, the working directory is located in the same directory as where the projects are run.
        if (System.getProperties().contains("work.dir"))
        {
            // from the acceptance test suite, the work.dir system property is specified
            workingDir = new File(System.getProperty("work.dir"));
        }
        return workingDir;
    }

    /**
     * Returns the location of a Pulse package, based on the pulse.package
     * system property.
     *
     * @return file reference to the pulse package
     * @throws IllegalStateException if pulse.package os not set or does not
     *         refer to a valid file
     */
    public static File getPulsePackage()
    {
        String pulsePackage = System.getProperty("pulse.package");
        if (!TextUtils.stringSet(pulsePackage))
        {
            throw new IllegalStateException("No pulse package specified (use the system property pulse.package)");
        }
        File pkg = new File(pulsePackage);
        if (!pkg.isFile())
        {
            throw new IllegalStateException("Unexpected invalid pulse.package: " + pulsePackage + " does not reference a file.");
        }
        return pkg;
    }
}
