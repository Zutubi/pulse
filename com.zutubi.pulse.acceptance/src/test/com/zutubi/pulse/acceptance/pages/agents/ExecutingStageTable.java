package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.table.PropertyTable;

/**
 * A property table that shows a build stage executing on an agent.
 */
public class ExecutingStageTable extends PropertyTable
{
    public ExecutingStageTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }
    
    public long getNumber()
    {
        return Long.parseLong(getValue("number").replace("build ", ""));
    }
    
    public String getProject()
    {
        return getValue("project");
    }
    
    public String getOwner()
    {
        return getValue("owner");
    }
    
    public String getStage()
    {
        return getValue("name");
    }
    
    public String getRecipe()
    {
        return getValue("recipe");
    }
}
