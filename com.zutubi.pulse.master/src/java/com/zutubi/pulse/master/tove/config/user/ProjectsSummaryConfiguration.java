package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.BuildColumns;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.ItemPicker;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Numeric;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for configuration of views of that show summaries of project status.
 */
@SymbolicName("zutubi.projectSummaryConfig")
public abstract class ProjectsSummaryConfiguration extends AbstractConfiguration
{
    @ControllingCheckbox(checkedFields = "hiddenHierarchyLevels")
    private boolean hierarchyShown = true;
    @Numeric(min = 0)
    private int hiddenHierarchyLevels = 1;
    @Numeric(min = 1)
    private int buildsPerProject = 1;
    @ItemPicker(optionProvider = "BrowseViewColumnsOptionProvider")
    private List<String> columns = defaultColumns();

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
        return new LinkedList<String>(Arrays.asList(BuildColumns.KEY_REVISION, BuildColumns.KEY_WHEN, BuildColumns.KEY_ELAPSED, BuildColumns.KEY_REASON, BuildColumns.KEY_TESTS));
    }
}
