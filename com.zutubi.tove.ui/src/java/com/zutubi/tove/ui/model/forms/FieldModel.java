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

package com.zutubi.tove.ui.model.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single field in a form.
 */
public class FieldModel
{
    private String type;
    private String name;
    private String label;
    private boolean required;
    private boolean readOnly;

    private List<String> actions;
    private List<String> scripts;

    private Map<String, Object> parameters = new HashMap<>();

    public FieldModel()
    {
    }

    public FieldModel(String type, String name, String label)
    {
        this.type = type;
        this.name = name;
        this.label = label;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public List<String> getActions()
    {
        return actions;
    }

    public void addAction(String action)
    {
        if (actions == null)
        {
            actions = new ArrayList<>();
        }

        actions.add(action);
    }

    public List<String> getScripts()
    {
        return scripts;
    }

    public void addScript(String script)
    {
        if (scripts == null)
        {
            scripts = new ArrayList<>();
        }

        scripts.add(script);
    }

    public boolean hasParameter(String name)
    {
        return parameters.containsKey(name);
    }

    public Object getParameter(String name)
    {
        return parameters.get(name);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void addParameter(String name, Object value)
    {
        parameters.put(name, value);
    }
}
