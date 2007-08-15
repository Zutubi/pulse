package com.zutubi.pulse.core.scm.svn;

import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.svn.config.SvnConfiguration;
import com.zutubi.util.StringUtils;
import com.opensymphony.util.TextUtils;

/**
 *
 *
 */
public class SvnClientFactory implements ScmClientFactory<SvnConfiguration>
{
    public ScmClient createClient(SvnConfiguration config) throws ScmException
    {
        SvnClient client;
        if (!TextUtils.stringSet(config.getKeyfile()))
        {
            if (TextUtils.stringSet(config.getUsername()))
            {
                client = new SvnClient(config.getUrl(), config.getUsername(), config.getPassword());
            }
            else
            {
                client = new SvnClient(config.getUrl());
            }
        }
        else
        {
            if (TextUtils.stringSet(config.getKeyfilePassphrase()))
            {
                client = new SvnClient(config.getUrl(), config.getUsername(), config.getPassword(), config.getKeyfile(), config.getKeyfilePassphrase());
            }
            else
            {
                client = new SvnClient(config.getUrl(), config.getUsername(), config.getPassword(), config.getKeyfile());
            }
        }

        client.setExcludedPaths(config.getFilterPaths());

        if(TextUtils.stringSet(config.getExternalMonitorPaths()))
        {
            for(String path: StringUtils.split(config.getExternalMonitorPaths()))
            {
                client.addExternalPath(path);
            }
        }

        client.setVerifyExternals(config.getVerifyExternals());
        return client;
    }
}
