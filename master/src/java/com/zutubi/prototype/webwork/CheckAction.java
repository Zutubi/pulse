package com.zutubi.prototype.webwork;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.prototype.config.ConfigurationCrudSupport;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.annotation.ConfigurationCheck;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.opensymphony.xwork.ActionContext;

import java.util.Map;
import java.util.HashMap;

/**
 * Process a configuration check request.
 *
 */
public class CheckAction extends ActionSupport
{
    private String path;

    private ObjectFactory objectFactory;

    private TypeRegistry typeRegistry;

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public String execute() throws Exception
    {
        Map<String, Object> parameters = ActionContext.getContext().getParameters();

        Map<String, Object> checkParameters = new HashMap<String, Object>();
        for (String name : parameters.keySet())
        {
            if (name.endsWith("_check"))
            {
                checkParameters.put(name.substring(0, name.length() - 6), parameters.get(name));
            }
        }

        String symbolicName = ((String[])checkParameters.get("symbolicName"))[0];
        
        Type type = typeRegistry.getType(symbolicName);
        Object instance = objectFactory.buildBean(type.getClazz());


        ConfigurationCrudSupport crud = new ConfigurationCrudSupport();
        crud.apply(checkParameters,  instance);

        ConfigurationCheck annotation = (ConfigurationCheck) type.getAnnotation(ConfigurationCheck.class);
        ConfigurationCheckHandler handler = objectFactory.buildBean(annotation.value());
        crud.apply(parameters, handler);

        handler.test(instance);

        // Propogate errors to the web ui in the same way as with default validation, using the validation context.
        // In this way, the errors will appear on the form, where they are most appropriate. 

        // We need to return the existing form values as well.

        return super.execute();
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
