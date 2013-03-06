package com.zutubi.pulse.master.tove.webwork.help;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;
import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

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
        Collection<String> fields = newArrayList(transform(
                filter(composite.getProperties(), getPropertyPredicate()),
                new Function<TypeProperty, String>()
                {
                    public String apply(TypeProperty typeProperty)
                    {
                        return typeProperty.getName();
                    }
                }));
        // them nicely alongside the form.

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
