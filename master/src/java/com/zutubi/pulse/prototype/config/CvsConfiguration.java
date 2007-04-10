package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.ConfigurationCheck;
import com.zutubi.prototype.annotation.Form;
import com.zutubi.prototype.annotation.Password;
import com.zutubi.prototype.annotation.Text;
import com.zutubi.validation.annotations.Required;
import com.zutubi.pulse.validation.annotation.CvsRoot;

/**
 *
 *
 */
@ConfigurationCheck(CvsConfigurationCheckHandler.class)
@Form(fieldOrder = {"root", "password", "module", "branch"})
public class CvsConfiguration extends ScmConfiguration
{
    @Required @CvsRoot @Text(size = 50)
    private String root;

    private String module;
    
    @Password
    private String password;
    private String branch;
    private Integer quietPeriod;

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

    public Integer getQuietPeriod()
    {
        return quietPeriod;
    }

    public void setQuietPeriod(Integer quietPeriod)
    {
        this.quietPeriod = quietPeriod;
    }
}
