package com.zutubi.pulse.acceptance.support;

public class SupportUtils
{
    public static void shutdown(Pulse pulse)
    {
        if (pulse != null)
        {
            try
            {
                pulse.stop();
            }
            catch (Exception e)
            {
                System.out.println("Failed to shutdown pulse instance: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
