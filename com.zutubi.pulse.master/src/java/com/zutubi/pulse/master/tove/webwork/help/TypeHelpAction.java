package com.zutubi.pulse.master.tove.webwork.help;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;

/**
 * Looks up the documentation for a type found by configuration path.
 */
public class TypeHelpAction extends TypeHelpActionSupport
{
    private ConfigurationTemplateManager configurationTemplateManager;

    protected CompositeType getType()
    {
        ComplexType type = configurationTemplateManager.getType(getPath(), ComplexType.class);
        return (CompositeType) type.getTargetType();
    }

    protected Predicate<TypeProperty> getPropertyPredicate()
    {
        return new Predicate<TypeProperty>()
        {
            public boolean apply(TypeProperty typeProperty)
            {
                return ToveUtils.isFormField(typeProperty);
            }
        };
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
