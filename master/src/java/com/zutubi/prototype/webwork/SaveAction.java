package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ValidationAware;
import com.zutubi.prototype.config.ConfigurationCrudSupport;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.XWorkValidationAdapter;

/**
 *
 *
 */
public class SaveAction extends ActionSupport
{
    private String symbolicName;
    private String path;
    private Configuration configuration;

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws Exception
    {
        configuration = new Configuration(path);
        configuration.analyse();

        if (!TextUtils.stringSet(symbolicName))
        {
            return INPUT;
        }

        ConfigurationCrudSupport crud = new ConfigurationCrudSupport();
        if (!crud.save(symbolicName, path, ActionContext.getContext().getParameters(), this))
        {
            return INPUT;
        }

        return SUCCESS;
    }
}
