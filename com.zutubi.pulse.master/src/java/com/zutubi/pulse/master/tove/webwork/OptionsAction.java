package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.webwork.ServletActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.handler.OptionProvider;
import com.zutubi.pulse.master.tove.handler.OptionProviderFactory;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;

import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Action for listing options for a combobox lazily.
 */
public class OptionsAction extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(OptionsAction.class);
    
    private String parentPath;
    private String baseName;
    private String field;
    private String symbolicName;
    private String errorMessage;
    private String[][] options;

    private ConfigurationTemplateManager configurationTemplateManager;
    private TypeRegistry typeRegistry;
    private ObjectFactory objectFactory;

    public void setParentPath(String parentPath)
    {
        this.parentPath = parentPath;
    }

    public void setBaseName(String baseName)
    {
        this.baseName = baseName;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public String[][] getOptions() throws InterruptedException
    {
        return options;
    }

    public List<String> getListing() throws Exception
    {
        CompositeType type = getType();
        TypeProperty property = getFieldProperty(type);
        Configuration instance = getInstance();

        OptionProvider optionProvider = OptionProviderFactory.build(type, property.getType(), getOptionAnnotation(property), objectFactory);

         @SuppressWarnings({"unchecked"})
         List<String> list = (List<String>) optionProvider.getOptions(instance, parentPath, property);
        if (configurationTemplateManager.isTemplatedPath(parentPath))
        {
            Object emptyOption = optionProvider.getEmptyOption(instance, parentPath, property);
            if (emptyOption != null)
            {
                list.add(0, (String) emptyOption);
            }
        }
        
        return list;
    }

    private CompositeType getType()
    {
        CompositeType type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            throw new RuntimeException("Unknown type '" + symbolicName + "'");
        }
        return type;
    }

    private TypeProperty getFieldProperty(CompositeType type)
    {
        TypeProperty property = type.getProperty(field);
        if (property == null)
        {
            throw new RuntimeException("Invalid field '" + field + "'");
        }
        return property;
    }

    private Configuration getInstance()
    {
        Configuration instance = null;
        if (StringUtils.stringSet(baseName))
        {
            String path = PathUtils.getPath(parentPath, baseName);
            instance = configurationTemplateManager.getInstance(path, Configuration.class);
            if (instance == null)
            {
                throw new RuntimeException("Invalid path '" + path + "'");
            }
        }
        return instance;
    }

    private Annotation getOptionAnnotation(TypeProperty property)
    {
        Select annotation = property.getAnnotation(Select.class);
        if (annotation == null)
        {
            throw new RuntimeException("Invalid property: no select annotation");
        }
        
        return annotation;
    }

    private String[][] convertListing(List<String> listing)
    {
        String[][] result = new String[listing.size()][];
        int i = 0;
        for (String option: listing)
        {
            result[i++] = new String[]{option};
        }

        return result;
    }

    public String execute() throws Exception
    {
        try
        {
            options = convertListing(getListing());
            return SUCCESS;
        }
        catch (Exception e)
        {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errorMessage = I18N.format("unable.to.load", new Object[]{e.getMessage()});
            return ERROR;
        }
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}