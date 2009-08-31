package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.acceptance.Constants;

/**
 * A project configuration that will produce build failures.
 */
public class FailAntProject extends ProjectConfigurationHelper
{
    public FailAntProject(ProjectConfiguration config)
    {
        super(config);
    }

    public ScmConfiguration createDefaultScm()
    {
        SubversionConfiguration svn = new SubversionConfiguration();
        svn.setCheckoutScheme(CheckoutScheme.CLEAN_CHECKOUT);
        svn.setMonitor(false);
        svn.setUrl(Constants.FAIL_ANT_REPOSITORY);
        return svn;
    }

}
