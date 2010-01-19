package com.zutubi.pulse.core.scm.svn;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;

/**
 * Factory that creates {@link SubversionClient} instances from configuration.
 */
public class SubversionClientFactory implements ScmClientFactory<SubversionConfiguration>
{
    public ScmClient createClient(SubversionConfiguration config) throws ScmException
    {
        SubversionClient client;
        if (!TextUtils.stringSet(config.getKeyfile()))
        {
            if (TextUtils.stringSet(config.getUsername()))
            {
                client = new SubversionClient(config.getUrl(), config.isEnableHttpSpooling(), config.getUsername(), config.getPassword());
            }
            else
            {
                client = new SubversionClient(config.getUrl(), config.isEnableHttpSpooling());
            }
        }
        else
        {
            if (TextUtils.stringSet(config.getKeyfilePassphrase()))
            {
                client = new SubversionClient(config.getUrl(), config.isEnableHttpSpooling(), config.getUsername(), config.getPassword(), config.getKeyfile(), config.getKeyfilePassphrase());
            }
            else
            {
                client = new SubversionClient(config.getUrl(), config.isEnableHttpSpooling(), config.getUsername(), config.getPassword(), config.getKeyfile());
            }
        }

        client.setExcludedPaths(config.getFilterPaths());
        switch (config.getExternalsMonitoring())
        {
            case DO_NOT_MONITOR:
                client.setMonitorAllExternals(false);
                break;

            case MONITOR_ALL:
                client.setMonitorAllExternals(true);
                break;

            case MONITOR_SELECTED:
                client.setMonitorAllExternals(false);
                for(String path: config.getExternalMonitorPaths())
                {
                    client.addExternalPath(path);
                }
                break;
        }

        client.setVerifyExternals(config.getVerifyExternals());
        return client;
    }
}
