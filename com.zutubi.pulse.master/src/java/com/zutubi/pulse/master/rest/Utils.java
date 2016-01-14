package com.zutubi.pulse.master.rest;

import com.zutubi.pulse.master.rest.errors.NotFoundException;
import com.zutubi.pulse.master.rest.model.CheckResultModel;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.ConfigurationCheckHandler;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic utilities for RESTish API implementation.
 */
public class Utils
{
    private static final Logger LOG = Logger.getLogger(Utils.class);

    public static String getRequestedPath(HttpServletRequest request)
    {
        return getRequestedPath(request, true, false);
    }

    public static String getRequestedPath(HttpServletRequest request, boolean normalise, boolean allowEmpty)
    {
        String requestPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String path = apm.extractPathWithinPattern(bestMatchPattern, requestPath);
        if (normalise)
        {
            path = PathUtils.normalisePath(path);
        }

        if (!allowEmpty && path.length() == 0)
        {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        return path;
    }

    /**
     * Indicates if the given path has a known, complex type (thus we can return a model for the
     * path). Such paths reference existing records or a defined but non-configured composite
     * property of a parent composite.
     *
     * @param configPath the path to test
     * @param configurationTemplateManager required resource
     * @return true iff the given path has a type we can model
     */
    public static boolean hasModellableType(String configPath, ConfigurationTemplateManager configurationTemplateManager)
    {
        try
        {
            configurationTemplateManager.getType(configPath);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    public static ComplexType getType(String configPath, ConfigurationTemplateManager configurationTemplateManager)
    {
        try
        {
            return configurationTemplateManager.getType(configPath);
        }
        catch (IllegalArgumentException e)
        {
            throw new NotFoundException("Path '" + configPath + "' does not exist or is not addressable via the API");
        }
    }

    static Record getRecord(String path, ConfigurationTemplateManager configurationTemplateManager)
    {
        Record record = configurationTemplateManager.getRecord(path);
        if (record == null)
        {
            throw new NotFoundException("Path '" + path + "' does not exist");
        }
        return record;
    }

    public static Record getComposite(String path, ConfigurationTemplateManager configurationTemplateManager)
    {
        Record record = getRecord(path, configurationTemplateManager);
        if (!StringUtils.stringSet(record.getSymbolicName()))
        {
            throw new IllegalArgumentException("Path '" + path + "' does not address a composite");
        }

        return record;
    }

    public static PostContext getPostContext(String configPath, ConfigurationTemplateManager configurationTemplateManager)
    {
        CompositeType postableType;
        String parentPath;
        String baseName = null;
        if (configurationTemplateManager.getTemplateScopes().contains(configPath))
        {
            // Inserting into a templated collection is a special case, where we need to know the
            // template parent and whether we are inserting a template or concrete instance.
            parentPath = configPath;
            throw new IllegalArgumentException("Not yet supported");
        }
        else
        {
            ComplexType type = getType(configPath, configurationTemplateManager);
            if (type instanceof CollectionType)
            {
                if (type.getTargetType() instanceof CompositeType)
                {
                    parentPath = configPath;
                    postableType = (CompositeType) type.getTargetType();
                } else
                {
                    throw new IllegalArgumentException("Cannot insert into collection at path '" + configPath + "': this collection contains simple elements, and should be updated via a PUT to the parent path.");
                }
            }
            else
            {
                Configuration instance = configurationTemplateManager.getInstance(configPath);
                if (instance != null)
                {
                    throw new IllegalArgumentException("Cannot create at path '" + configPath + "': path already exists.");
                }

                parentPath = PathUtils.getParentPath(configPath);
                baseName = PathUtils.getBaseName(configPath);
                postableType = (CompositeType) type;
            }
        }

        return new PostContext(parentPath, baseName, postableType);
    }

    public static MutableRecord convertProperties(CompositeType type, String templateOwnerPath, Map<String, Object> properties) throws TypeException
    {
        MutableRecord result = type.createNewRecord(true);

        // Internal properties may not be set this way, so strip them from the default config.
        for (TypeProperty property: type.getInternalProperties())
        {
            result.remove(property.getName());
        }

        for (TypeProperty property: type.getProperties(SimpleType.class))
        {
            Object value = properties.get(property.getName());
            if (value != null)
            {
                result.put(property.getName(), property.getType().fromXmlRpc(templateOwnerPath, value, true));
            }
        }

        for (TypeProperty property: type.getProperties(CollectionType.class))
        {
            if (property.getType().getTargetType() instanceof SimpleType)
            {
                Object value = properties.get(property.getName());
                if (value != null)
                {
                    result.put(property.getName(), property.getType().fromXmlRpc(templateOwnerPath, value, true));
                }
            }
        }

        return result;
    }

    public static Map<String, Object> getSimplePropertyValues(CompositeType type, Configuration instance) throws Exception
    {
        Map<String, Object> values = new HashMap<>();
        for (TypeProperty property: type.getProperties(SimpleType.class))
        {
            values.put(property.getName(), property.getValue(instance));
        }

        return values;
    }

    public static CheckResultModel runCheck(ConfigurationCheckHandler<Configuration> handler, Configuration instance)
    {
        CheckResultModel result;
        try
        {
            handler.test(instance);
            result = new CheckResultModel();
        }
        catch (Exception e)
        {
            LOG.debug(e);
            result = new CheckResultModel(e);
        }

        return result;
    }
}
