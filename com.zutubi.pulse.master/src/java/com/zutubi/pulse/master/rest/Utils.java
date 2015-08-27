package com.zutubi.pulse.master.rest;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Generic utilities for RESTish APi implementation.
 */
public class Utils
{
    static String getConfigPath(HttpServletRequest request)
    {
        String requestPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        return PathUtils.normalisePath(apm.extractPathWithinPattern(bestMatchPattern, requestPath));
    }

    static PostContext getPostContext(String configPath, ConfigurationTemplateManager configurationTemplateManager)
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
            ComplexType type = configurationTemplateManager.getType(configPath);
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
            } else
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

    static MutableRecord convertProperties(CompositeType type, String templateOwnerPath, Map<String, Object> properties) throws TypeException
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
}
