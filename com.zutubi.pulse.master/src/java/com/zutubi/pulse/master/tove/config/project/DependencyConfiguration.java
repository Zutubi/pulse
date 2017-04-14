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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

import static com.zutubi.pulse.core.dependency.ivy.IvyLatestRevisionMatcher.LATEST;

/**
 * A dependency defines a project and the artifacts built by that project that this project requires
 * for building. 
 */
@SymbolicName("zutubi.dependency")
@Table(columns = {"projectName", "revision", "stages", "transitive"})
@Form(fieldOrder = {"project", "revision", "customRevision", "transitive", "stageType", "stages"})
public class DependencyConfiguration extends AbstractConfiguration
{
    public static final String REVISION_LATEST_INTEGRATION = LATEST + IvyStatus.STATUS_INTEGRATION;
    public static final String REVISION_LATEST_MILESTONE = LATEST + IvyStatus.STATUS_MILESTONE;
    public static final String REVISION_LATEST_RELEASE = LATEST + IvyStatus.STATUS_RELEASE;
    public static final String REVISION_CUSTOM = "custom";

    public enum StageType
    {
        ALL_STAGES,
        CORRESPONDING_STAGES,
        SELECTED_STAGES
    }

    /**
     * The project being depended upon.
     */
    @Required @Reference(optionProvider = "DependencyProjectOptionProvider")
    @Constraint("CircularDependencyValidator")
    private ProjectConfiguration project;

    /**
     * The revision of this dependency.
     */
    @Required
    @ControllingSelect(enableSet = {REVISION_CUSTOM}, dependentFields = "customRevision", optionProvider = "DependencyConfigurationRevisionOptionProvider")
    private String revision = REVISION_LATEST_INTEGRATION;

    /**
     * The custom revision, used if the revision field is set to custom.
     */
    @Required
    private String customRevision = "";

    /**
     * Indicates whether or not to resolve this dependencies dependencies.
     */
    private boolean transitive = true;

    @ControllingSelect(enableSet = {"SELECTED_STAGES"}, dependentFields = {"stages"})
    @Required
    private StageType stageType = StageType.ALL_STAGES;

    @Reference(dependentOn = "project")
    private List<BuildStageConfiguration> stages = new LinkedList<BuildStageConfiguration>();

    public ProjectConfiguration getProject()
    {
        return project;
    }

    public void setProject(ProjectConfiguration project)
    {
        this.project = project;
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
    }

    public boolean isTransitive()
    {
        return transitive;
    }

    public void setTransitive(boolean transitive)
    {
        this.transitive = transitive;
    }

    public List<BuildStageConfiguration> getStages()
    {
        return stages;
    }

    public void setStages(List<BuildStageConfiguration> stages)
    {
        this.stages = stages;
    }

    public StageType getStageType()
    {
        return stageType;
    }

    public void setStageType(StageType stageType)
    {
        this.stageType = stageType;
    }

    public String getCustomRevision()
    {
        return customRevision;
    }

    public void setCustomRevision(String customRevision)
    {
        this.customRevision = customRevision;
    }

    @Transient
    public String getDependencyRevision()
    {
        if (revision != null && revision.equals(REVISION_CUSTOM))
        {
            return customRevision;
        }
        return revision;
    }
}
