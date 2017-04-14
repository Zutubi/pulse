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

package com.zutubi.tove.ui.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.zutubi.tove.ui.links.ConfigurationLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model representing composites.
 */
@JsonTypeName("composite")
public class CompositeModel extends ConfigModel
{
    private CompositeTypeModel type;
    private boolean keyed;
    private Map<String, Object> properties;
    private Map<String, Object> formattedProperties;
    private Map<String, List<String>> validationErrors;
    private List<ActionModel> actions;
    private List<ActionModel> descendantActions;
    private List<ActionModel> refactoringActions;
    private List<ConfigurationLink> links;
    private StateModel state;

    public CompositeModel()
    {
    }

    public CompositeModel(String handle, String key, String label, boolean keyed, boolean deeplyValid)
    {
        super(handle, key, label, deeplyValid);
        this.keyed = keyed;
    }

    public CompositeTypeModel getType()
    {
        return type;
    }

    public void setType(CompositeTypeModel type)
    {
        this.type = type;
    }

    public boolean isKeyed()
    {
        return keyed;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }

    public Map<String, Object> getFormattedProperties()
    {
        return formattedProperties;
    }

    public void setFormattedProperties(Map<String, Object> formattedProperties)
    {
        this.formattedProperties = formattedProperties;
    }

    public Map<String, List<String>> getValidationErrors()
    {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, List<String>> validationErrors)
    {
        this.validationErrors = validationErrors;
    }

    public List<ActionModel> getActions()
    {
        return actions;
    }

    public void addAction(ActionModel action)
    {
        if (actions == null)
        {
            actions = new ArrayList<>();
        }
        actions.add(action);
    }

    public List<ActionModel> getDescendantActions()
    {
        return descendantActions;
    }

    public void addDescendantAction(ActionModel action)
    {
        if (descendantActions == null)
        {
            descendantActions = new ArrayList<>();
        }
        descendantActions.add(action);
    }

    public List<ActionModel> getRefactoringActions()
    {
        return refactoringActions;
    }

    public void addRefactoringAction(ActionModel action)
    {
        if (refactoringActions == null)
        {
            refactoringActions = new ArrayList<>();
        }
        refactoringActions.add(action);
    }

    public List<ConfigurationLink> getLinks()
    {
        return links;
    }

    public void setLinks(List<ConfigurationLink> links)
    {
        if (links.size() > 0)
        {
            this.links = new ArrayList<>(links);
        }
        else
        {
            this.links = null;
        }
    }

    public StateModel getState()
    {
        return state;
    }

    public void setState(StateModel state)
    {
        this.state = state;
    }
}
