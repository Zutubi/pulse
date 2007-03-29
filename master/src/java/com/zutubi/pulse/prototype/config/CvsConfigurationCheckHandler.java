package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.ConfigurationCheckHandler;

/**
 *
 *
 */
public class CvsConfigurationCheckHandler implements ConfigurationCheckHandler<CvsConfiguration>
{
    public void test(CvsConfiguration configuration)
    {
        // send a test email to check the email server configuration.
    }
}
