package com.zutubi.pulse.acceptance;

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
}
