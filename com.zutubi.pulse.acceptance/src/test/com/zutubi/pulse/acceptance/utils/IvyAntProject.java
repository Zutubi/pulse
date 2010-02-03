package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.acceptance.Constants;

public class IvyAntProject extends ProjectConfigurationHelper
{
    public IvyAntProject(ProjectConfiguration config)
    {
        super(config);
    }

    public ScmConfiguration createDefaultScm()
    {
        SubversionConfiguration svn = new SubversionConfiguration();
        svn.setCheckoutScheme(CheckoutScheme.CLEAN_CHECKOUT);
        svn.setMonitor(false);
        svn.setUrl(Constants.IVY_ANT_REPOSITORY);
        return svn;
    }
}
