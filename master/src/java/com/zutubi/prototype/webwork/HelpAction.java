package com.zutubi.prototype.webwork;

import com.zutubi.config.annotations.Form;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.config.docs.ConfigurationDocsManager;
import com.zutubi.prototype.config.docs.PropertyDocs;
import com.zutubi.prototype.config.docs.TypeDocs;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.TextUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Looks up the documentation for a type found by configuration path.
 */
public class HelpAction extends ActionSupport
{
    private static final int TRIM_LIMIT = 40;
    
    private String path;
    private ConfigurationDocsManager configurationDocsManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private TypeDocs typeDocs;
    private List<String> formProperties;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public TypeDocs getTypeDocs()
    {
        return typeDocs;
    }

    public List<String> getFormProperties()
    {
        return formProperties;
    }

    public String safeDetails(String s)
    {
        return TextUtils.stringSet(s) ? s : "No details.";
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
        CompositeType type = configurationTemplateManager.getType(path, CompositeType.class);
        typeDocs = configurationDocsManager.getDocs(type);

        // Calculate the form field properties in order so the UI can display
        // them nicely alongside the form.
        List<String> fields = CollectionUtils.map(CollectionUtils.filter(type.getProperties(),
                                                                         new Predicate<TypeProperty>()
                                                                         {
                                                                             public boolean satisfied(TypeProperty typeProperty)
                                                                             {
                                                                                 return PrototypeUtils.isFormField(typeProperty);
                                                                             }
                                                                         }),
                                                  new Mapping<TypeProperty, String>()
                                                  {
                                                      public String map(TypeProperty typeProperty)
                                                      {
                                                          return typeProperty.getName();
                                                      }
                                                  });

        Form annotation = type.getAnnotation(Form.class, true);
        List<String> declaredOrder = null;
        if(annotation != null)
        {
            declaredOrder = Arrays.asList(annotation.fieldOrder());
        }
        formProperties = PrototypeUtils.evaluateFieldOrder(declaredOrder, fields);
        
        return SUCCESS;
    }

    public void setConfigurationDocsManager(ConfigurationDocsManager configurationDocsManager)
    {
        this.configurationDocsManager = configurationDocsManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
