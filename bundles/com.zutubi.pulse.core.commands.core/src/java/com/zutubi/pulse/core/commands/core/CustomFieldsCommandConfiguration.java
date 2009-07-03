package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for instances of {@link CustomFieldsCommand}.
 */
@SymbolicName("zutubi.customFieldsCommandConfig")
@Form(fieldOrder = {"name", "value", "scope"})
public class CustomFieldsCommandConfiguration extends CommandConfigurationSupport
{
    @Addable("field")
    private Map<String, CustomFieldConfiguration> fields = new LinkedHashMap<String, CustomFieldConfiguration>();

    public CustomFieldsCommandConfiguration()
    {
        super(CustomFieldsCommand.class);
    }

    public CustomFieldsCommandConfiguration(String name)
    {
        this();
        setName(name);
    }

    public Map<String, CustomFieldConfiguration> getFields()
    {
        return fields;
    }

    public void setFields(Map<String, CustomFieldConfiguration> fields)
    {
        this.fields = fields;
    }

    public void addField(CustomFieldConfiguration field)
    {
        fields.put(field.getName(), field);
    }
}
