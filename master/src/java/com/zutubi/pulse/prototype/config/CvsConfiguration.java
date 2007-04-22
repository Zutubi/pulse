package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.validation.annotation.CvsRoot;
import com.zutubi.pulse.scm.ScmClient;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.servercore.config.ScmConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
@ConfigurationCheck("CvsConfigurationCheckHandler")
@Form(fieldOrder = {"root", "password", "module", "branch"})
public class CvsConfiguration extends ScmConfiguration
{
    @Required @CvsRoot @Text(size = 50)
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

    public ScmClient createClient() throws SCMException
    {
        // FIXME
        throw new RuntimeException("Method not yet implemented.");
    }
}
