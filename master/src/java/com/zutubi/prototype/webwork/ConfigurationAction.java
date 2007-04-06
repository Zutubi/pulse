package com.zutubi.prototype.webwork;

import com.zutubi.pulse.web.ActionSupport;

/**
 *
 *
 */
public class ConfigurationAction extends ActionSupport
{
/*
    */
/**
     * The path identifying the configuration presented by this action.
     */
/*
    private String path;

    private Configuration configuration;

    public Configuration getConfiguration()
    {
        return configuration;
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

        Type type = configuration.getType();

        if (type instanceof CompositeType)
        {
            return "composite";
        }
        if (type instanceof ListType)
        {
            return "list";
        }
        if (type instanceof MapType)
        {
            return "map";
        }

        return SUCCESS;
    }
*/
}
