package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * User preferences for the browse page.
 */
@SymbolicName("zutubi.browseViewConfig")
@Classification(single = "browse")
@Form(fieldOrder = {"groupsShown", "hierarchyShown", "hiddenHierarchyLevels", "buildsPerProject", "columns"})
public class BrowseViewConfiguration extends ProjectsSummaryConfiguration
{
    private boolean groupsShown = true;

    public BrowseViewConfiguration()
    {
        setPermanent(true);
    }

    public boolean isGroupsShown()
    {
        return groupsShown;
    }

    public void setGroupsShown(boolean groupsShown)
    {
        this.groupsShown = groupsShown;
    }
}
