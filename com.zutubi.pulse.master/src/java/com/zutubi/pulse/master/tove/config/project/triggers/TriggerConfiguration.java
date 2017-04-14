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

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.tove.annotations.ExternalState;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 */
@Table(columns = {"name", "type", "state"})
@SymbolicName("zutubi.triggerConfig")
public abstract class TriggerConfiguration extends AbstractNamedConfiguration
{
    @ExternalState
    private long triggerId;
    private Map<String, ResourcePropertyConfiguration> properties = new LinkedHashMap<String, ResourcePropertyConfiguration>();
    private List<TriggerConditionConfiguration> conditions = new LinkedList<TriggerConditionConfiguration>();

    public long getTriggerId()
    {
        return triggerId;
    }

    public void setTriggerId(long triggerId)
    {
        this.triggerId = triggerId;
    }

    public Map<String, ResourcePropertyConfiguration> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, ResourcePropertyConfiguration> properties)
    {
        this.properties = properties;
    }

    public List<TriggerConditionConfiguration> getConditions()
    {
        return conditions;
    }

    public void setConditions(List<TriggerConditionConfiguration> conditions)
    {
        this.conditions = conditions;
    }

    @Transient
    public String getType()
    {
        return Messages.getInstance(this).format("type.label");
    }

    public abstract Trigger newTrigger();

    public void update(Trigger trigger)
    {
        trigger.setName(getName());
    }

    /**
     * Called each time a trigger fires (but before the task is run).  This default implementation
     * does nothing, subclasses may override as desired.
     *
     * @param trigger the entity for this trigger
     */
    public void postFire(Trigger trigger)
    {
        // Do nothing by default.
    }
}
