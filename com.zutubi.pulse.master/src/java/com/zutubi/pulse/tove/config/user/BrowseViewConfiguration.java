package com.zutubi.pulse.tove.config.user;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.config.AbstractConfiguration;
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
public class BrowseViewConfiguration extends AbstractConfiguration
{
    private boolean groupsShown = true;
    @ControllingCheckbox(dependentFields = "hiddenHierarchyLevels")
    private boolean hierarchyShown = true;
    @Numeric(min = 0)
    private int hiddenHierarchyLevels = 1;
    @Numeric(min = 1)
    private int buildsPerProject = 1;
    @ItemPicker(optionProvider = "BrowseViewColumnsOptionProvider")
    private List<String> columns = defaultColumns();

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

    public boolean isHierarchyShown()
    {
        return hierarchyShown;
    }

    public void setHierarchyShown(boolean hierarchyShown)
    {
        this.hierarchyShown = hierarchyShown;
    }

    public int getHiddenHierarchyLevels()
    {
        return hiddenHierarchyLevels;
    }

    public void setHiddenHierarchyLevels(int hiddenHierarchyLevels)
    {
        this.hiddenHierarchyLevels = hiddenHierarchyLevels;
    }

    public int getBuildsPerProject()
    {
        return buildsPerProject;
    }

    public void setBuildsPerProject(int buildsPerProject)
    {
        this.buildsPerProject = buildsPerProject;
    }

    public List<String> getColumns()
    {
        return columns;
    }

    public void setColumns(List<String> columns)
    {
        this.columns = columns;
    }

    public static List<String> defaultColumns()
    {
        return new LinkedList(Arrays.asList(BuildColumns.KEY_WHEN, BuildColumns.KEY_ELAPSED, BuildColumns.KEY_REASON, BuildColumns.KEY_TESTS));
    }
}
