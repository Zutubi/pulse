package com.zutubi.pulse.web.prototype;

import com.zutubi.prototype.model.Config;
import com.zutubi.pulse.prototype.PrototypeConfigRegistry;
import com.zutubi.pulse.web.ActionSupport;

import java.util.StringTokenizer;

/**
 *
 *
 */
public class SummaryAction extends ActionSupport
{
    private PrototypeConfigRegistry configRegistry;

    private Config config;

    private String path;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public Config getConfig()
    {
        return config;
    }

    public String execute() throws Exception
    {
        // identify the data available at this path, and present a summary.

        config = new Config();

        // TODO: need to handle this via the type system, look up the symbolicName based on the path.
        for (String s : configRegistry.getRoot(getRootScope()))
        {
            config.addNestedProperty(s);
        }

        return SUCCESS;
    }

    private String getRootScope()
    {
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        return tokens.nextToken();
    }

    public void setConfigRegistry(PrototypeConfigRegistry configRegistry)
    {
        this.configRegistry = configRegistry;
    }
}
