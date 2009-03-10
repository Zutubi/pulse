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
                e.printStackTrace();
            }
        }
    }
}
