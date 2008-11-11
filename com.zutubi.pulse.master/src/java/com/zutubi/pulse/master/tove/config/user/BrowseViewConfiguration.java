package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.AbstractConfiguration;
import com.zutubi.pulse.master.model.BuildColumns;
import com.zutubi.validation.annotations.Numeric;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
