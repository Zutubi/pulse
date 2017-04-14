/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.user;

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
    public static final String KEY_COMPLETED = "completed";
    public static final String KEY_ELAPSED = "elapsed";
    public static final String KEY_ERRORS = "errors";
    public static final String KEY_REASON = "reason";
    public static final String KEY_REVISION = "rev";
    public static final String KEY_WHEN = "when";
    public static final String KEY_TESTS = "tests";
    public static final String KEY_VERSION = "version";
    public static final String KEY_WARNINGS = "warnings";
    public static final String KEY_MATURITY = "maturity";

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
        return new LinkedList<String>(Arrays.asList(KEY_REVISION, KEY_WHEN, KEY_ELAPSED, KEY_REASON, KEY_TESTS));
    }
}
