package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.pulse.master.tove.handler.OptionProvider;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.bean.ObjectFactory;
import flexjson.JSON;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The options action is used to dynamically load list options.
 *
 * The primary scenario where this is used if when the options presented
 * in one field depend on the configuration of another field that itself
 * has changed since the form was originally rendered.
 */
//NOTE: This implementaiton is currently very specific to the situation of the @Reference(dependentOn)
//      configuraiton.  Generalising the implementation may be an option in the future. 
public class OptionsAction extends ActionSupport
{
    // HMMzzz, the existing ToveActionSupport does not seem to be very relevant to the
    // the processing we require here.

    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ObjectFactory objectFactory;

    /**
     * The path to the object that contains the field whose options
     * are being re-evaluated.
     */
    private String path;

    /**
     * The name of the fields whose options are being re-evaluated.
     */
    private String field;

    /**
     * The current value of the dependent field.
     */
    private long dependency = 0;

    private List options;

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public void setDependency(long dependency)
    {
        this.dependency = dependency;
    }

    @JSON
    public List getOptions()
    {
        return options;
    }

    @SuppressWarnings("unchecked")
    public String execute() throws Exception
    {
        CompositeType type = (CompositeType) configurationTemplateManager.getType(path).getTargetType();
        if (type == null)
        {
            return ERROR;
        }

        TypeProperty property = type.getProperty(field);
        Reference annotation = property.getAnnotation(Reference.class);
        TypeProperty dependencyProperty = type.getProperty(annotation.dependentOn());

        OptionProvider optionProvider = (OptionProvider) objectFactory.buildBean(ClassLoaderUtils.loadAssociatedClass(type.getClazz(), annotation.optionProvider()));

        Object instance = configurationProvider.get(this.dependency, dependencyProperty.getClazz());

        List mapOptions = optionProvider.getOptions(instance, path, property);

        // convert the map options to a list of lists for easy consumption by the javascript.
        options = CollectionUtils.map(mapOptions, new Mapping<Object, Object>()
        {
            public Object map(Object o)
            {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
                return Arrays.asList(entry.getKey(), entry.getValue());
            }
        });

        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

}
