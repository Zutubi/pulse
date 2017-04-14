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

package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.scheduling.BuildCompletedEventFilter;
import com.zutubi.pulse.master.scheduling.EventTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.validation.annotations.Required;

import java.util.List;

/**
 * A trigger that fires when some project build completes, possibly filtered
 * by state.
 */
@Form(fieldOrder = { "name", "project", "states", "propagateRevision", "supercedeQueued", "propagateStatus", "propagateVersion"})
@SymbolicName("zutubi.buildCompletedConfig")
public class BuildCompletedTriggerConfiguration extends TriggerConfiguration
{
    /**
     * The project to listen for builds of.
     */
    @Reference
    @Required
    private ProjectConfiguration project;
    /**
     * If non-empty, the trigger will only fire after builds completed in one
     * of the given states.
     */
    @ItemPicker(optionProvider = "com.zutubi.pulse.master.tove.config.CompletedResultStateOptionProvider")
    private List<ResultState> states;
    /**
     * If true, the revision of the completed build will also be used for the
     * triggered build.
     */
    @ControllingCheckbox(checkedFields = {"supercedeQueued"})
    private boolean propagateRevision;
    /**
     * If true, build requests raised by this trigger will supercede earlier
     * ones that are already queued (but not commenced).  This prevents
     * several builds with propagated revisions queueing up - instead any
     * existing request is updated to the latest propagated revision.
     */
    private boolean supercedeQueued;

    /**
     * If true, build requests raised by this trigger will inherit the status
     * of the completed build.
     */
    private boolean propagateStatus;

    /**
     * If true, build requests raised by this trigger will inherit the version
     * of the completed build.
     */
    private boolean propagateVersion;
    
    public BuildCompletedTriggerConfiguration()
    {
    }

    public ProjectConfiguration getProject()
    {
        return project;
    }

    public void setProject(ProjectConfiguration project)
    {
        this.project = project;
    }

    public List<ResultState> getStates()
    {
        return states;
    }

    public void setStates(List<ResultState> states)
    {
        this.states = states;
    }

    public boolean isPropagateRevision()
    {
        return propagateRevision;
    }

    public void setPropagateRevision(boolean propagateRevision)
    {
        this.propagateRevision = propagateRevision;
    }

    public boolean isSupercedeQueued()
    {
        return supercedeQueued;
    }

    public void setSupercedeQueued(boolean supercedeQueued)
    {
        this.supercedeQueued = supercedeQueued;
    }

    public boolean isPropagateStatus()
    {
        return propagateStatus;
    }

    public void setPropagateStatus(boolean propagateStatus)
    {
        this.propagateStatus = propagateStatus;
    }

    public boolean isPropagateVersion()
    {
        return propagateVersion;
    }

    public void setPropagateVersion(boolean propagateVersion)
    {
        this.propagateVersion = propagateVersion;
    }

    public Trigger newTrigger()
    {
        return new EventTrigger(BuildCompletedEvent.class, getName(), BuildCompletedEventFilter.class);
    }
}
