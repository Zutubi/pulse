package com.zutubi.pulse.acceptance;

import com.zutubi.util.Condition;

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

    public static void waitForCondition(Condition condition, long timeout, String description)
    {
        long endTime = System.currentTimeMillis() + timeout;
        while(!condition.satisfied())
        {
            if(System.currentTimeMillis() > endTime)
            {
                throw new RuntimeException("Timed out waiting for " + description);
            }

            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException("Interrupted waiting for " + description);
            }
        }
    }
}
