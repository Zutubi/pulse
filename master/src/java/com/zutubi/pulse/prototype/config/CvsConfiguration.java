package com.zutubi.pulse.prototype.config;

/**
 *
 *
 */
public class CvsConfiguration extends BaseScmConfiguration
{
    private String root;
    private String module;
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
