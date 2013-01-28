package com.zutubi.pulse.master.tove.webwork.help;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;
import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 */
public abstract class TypeHelpActionSupport extends HelpActionSupport
{
    private ConfigurationDocsManager configurationDocsManager;
    private TypeDocs typeDocs;
    private List<String> formProperties;

    public TypeDocs getTypeDocs()
    {
        return typeDocs;
    }

    public List<String> getFormProperties()
    {
        return formProperties;
    }

    public String execute() throws Exception
    {
        CompositeType composite = getType();
        typeDocs = configurationDocsManager.getDocs(composite);

        // Calculate the form field properties in order so the UI can display
        // them nicely alongside the form.
        Collection<String> fields = CollectionUtils.map(Collections2.filter(composite.getProperties(),
                getPropertyPredicate()),
                new Mapping<TypeProperty, String>()
                {
                    public String map(TypeProperty typeProperty)
                    {
                        return typeProperty.getName();
                    }
                });

        Form annotation = composite.getAnnotation(Form.class, true);
        List<String> declaredOrder = null;
        if(annotation != null)
        {
            declaredOrder = Arrays.asList(annotation.fieldOrder());
        }
        formProperties = ToveUtils.evaluateFieldOrder(declaredOrder, fields);

        return SUCCESS;
    }

    protected abstract CompositeType getType();

    protected abstract Predicate<TypeProperty> getPropertyPredicate();

    public void setConfigurationDocsManager(ConfigurationDocsManager configurationDocsManager)
    {
        this.configurationDocsManager = configurationDocsManager;
    }
}
