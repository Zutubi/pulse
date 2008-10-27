package com.zutubi.pulse.master.tove.webwork.help;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.config.docs.ConfigurationDocsManager;
import com.zutubi.tove.config.docs.PropertyDocs;
import com.zutubi.tove.config.docs.TypeDocs;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;

import java.util.Arrays;
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

    public boolean isExpandable(PropertyDocs docs)
    {
        return hasExamples(docs) || isAbbreviated(docs);
    }

    public boolean hasExamples(PropertyDocs docs)
    {
        return docs.getExamples().size() > 0;
    }

    private boolean isAbbreviated(PropertyDocs docs)
    {
        return docs.getVerbose() != null && !docs.getVerbose().equals(docs.getBrief());
    }

    public String execute() throws Exception
    {
        CompositeType composite = getType();
        typeDocs = configurationDocsManager.getDocs(composite);

        // Calculate the form field properties in order so the UI can display
        // them nicely alongside the form.
        List<String> fields = CollectionUtils.map(CollectionUtils.filter(composite.getProperties(),
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
