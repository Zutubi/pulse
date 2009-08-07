package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;

/**
 * This action provides support for rendering the admins configuration pages.
 */
public class DisplayScopeAction extends ActionSupport
{
    private ConfigurationTemplateManager configurationTemplateManager;

    /**
     * Section of the UI we live in.  Overridden in
     * {@link com.zutubi.pulse.master.webwork.dispatcher.mapper.PulseActionMapper} where necessary.
     */
    private String section = "administration";
    /**
     * Tab of the UI we live in.  Defaults to the scope, overridden in
     * {@link com.zutubi.pulse.master.webwork.dispatcher.mapper.PulseActionMapper} where necessary.
     */
    private String tab;
    /**
     * Prefixed to the path that comes in from the client.  Overridden in
     * {@link com.zutubi.pulse.master.webwork.dispatcher.mapper.PulseActionMapper} where necessary.
     */
    private String prefixPath = "";
    /**
     * The full path being displayed.
     */
    private String path = "";

    /**
     * The name of the owning entity being displayed.  For example, the name of the project or agent.
     */
    private String owner;

    private String scope;

    private String configTreePath;

    private String templateTreePath;
    private String configClassification;


    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    public String getTab()
    {
        return tab;
    }

    public void setTab(String tab)
    {
        this.tab = tab;
    }

    public String getPrefixPath()
    {
        return prefixPath;
    }

    public void setPrefixPath(String prefixPath)
    {
        this.prefixPath = prefixPath;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getOwner()
    {
        return owner;
    }

    public String getScope()
    {
        return scope;
    }

    public String getConfigTreePath()
    {
        return configTreePath;
    }

    public String getTemplateTreePath()
    {
        return templateTreePath;
    }

    public String getConfigClassification()
    {
        return configClassification;
    }

    public String execute() throws Exception
    {
        if(prefixPath.contains("${principle}"))
        {
            String principle = AcegiUtils.getLoggedInUsername();
            if (principle == null)
            {
                return "guest";
            }

            prefixPath = prefixPath.replace("${principle}", principle);
        }

        path = PathUtils.getPath(prefixPath, path);
        if (!StringUtils.stringSet(path))
        {
            addActionError("Path is required");
            return ERROR;
        }

        // This also serves as a validity check for the path: the user should
        // only be requesting a complex type that can be determined.  Note
        // that the path may not exist - although its parent must.
        ComplexType type = configurationTemplateManager.getType(path);

        String[] pathElements = PathUtils.getPathElements(path);
        if (pathElements.length > 0)
        {
            // extract the scope.
            scope = pathElements[0];
        }

        if(tab == null)
        {
            tab = scope;
        }
        
        boolean templated = configurationTemplateManager.isTemplatedPath(path);

        // if we have more than just the scope, evaluate the config tree and template tree paths.
        if (pathElements.length > 1)
        {
            owner = pathElements[1]; // the owner identifies the project/agent - scope level configuration.

            String[] configPath = new String[pathElements.length - 1];
            System.arraycopy(pathElements, 1, configPath, 0, configPath.length);
            configTreePath = PathUtils.getPath(configPath);

            // the template path is based on the owning path
            templateTreePath = configurationTemplateManager.getTemplatePath(PathUtils.getPath(scope, owner));
        }

        if(templated)
        {
            boolean concrete = true;
            if(pathElements.length > 1)
            {
                TemplateNode node = configurationTemplateManager.getTemplateHierarchy(scope).getNodeById(owner);
                concrete = node.isConcrete();
            }

            configClassification = concrete ? "concrete" : "template";
        }
        else
        {
            configClassification = ToveUtils.getClassification(type);
        }

        return templated ? "template" : "config";
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
