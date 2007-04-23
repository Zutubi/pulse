package com.zutubi.pulse.servercore.config;

import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Password;
import com.zutubi.config.annotations.Text;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.servercore.scm.ScmClient;
import com.zutubi.pulse.servercore.scm.cvs.CvsClient;
import com.zutubi.pulse.servercore.validation.annotation.CvsRoot;
import com.zutubi.validation.annotations.Required;

import java.io.File;

/**
 *
 *
 */
@ConfigurationCheck("CvsConfigurationCheckHandler")
@Form(fieldOrder = {"root", "password", "module", "branch", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod"})
public class CvsConfiguration extends ScmConfiguration
{
    @Required @CvsRoot
    @Text(size = 50)
    private String root;

    private String module;
    
    @Password
    private String password;
    private String branch;

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getBranch()
    {
        return branch;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public String getModule()
    {
        return module;
    }

    public void setModule(String module)
    {
        this.module = module;
    }

    public String getType()
    {
        return "cvs";
    }

    public ScmClient createClient() throws ScmException
    {
        // use a manual autowire here since this object itself is not wired, and so
        // does not have access to the object factory.
        ConfigurationManager configurationManager = (ConfigurationManager) ComponentContext.getBean("configurationManager");
        File tmpRoot = configurationManager.getSystemPaths().getTmpRoot();
        return new CvsClient(root, module, password, branch, getFilterPaths(), tmpRoot);
    }
}
