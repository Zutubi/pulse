package com.zutubi.pulse.prototype;

/**
 *
 *
 */
public class CvsConfiguration extends BaseScmConfiguration
{
    private String root;
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
}
