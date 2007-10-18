package com.zutubi.prototype.validation;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.config.ConfigurationValidationContext;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;

import java.util.Collections;
import java.util.List;

/**
 * A validator that ensures unique names are used for instances added to
 * maps.  This ensures that user-selected names within a configuration scope
 * do not conflict.
 */
public class UniqueNameValidator extends FieldValidatorSupport
{
    public UniqueNameValidator()
    {
        setDefaultMessageKey(".inuse");
    }

    public void validate(Object obj) throws ValidationException
    {
        ValidationContext context = getValidationContext();
        if(context instanceof ConfigurationValidationContext)
        {
            ConfigurationValidationContext configContext = (ConfigurationValidationContext) context;
            Object value = getFieldValue(getFieldName(), obj);
            String parentPath = configContext.getParentPath();

            // We can only validate string keys of map instances.
            if(value != null && value instanceof String && parentPath != null)
            {
                String name = (String) value;
                String baseName = configContext.getBaseName();

                // If this is a new object, or the object's name has changed,
                // check that the name is unique in the parent.
                if(baseName == null || !baseName.equals(name))
                {
                    ConfigurationTemplateManager configurationTemplateManager = configContext.getConfigurationTemplateManager();
                    String path = PathUtils.getPath(parentPath, name);
                    if(configurationTemplateManager.pathExists(path))
                    {
                        addFieldError(getFieldName());
                    }
                    else if(PathUtils.getPathElements(path).length > 2)
                    {
                        // We only need to do these checks when potentially
                        // within a templated instance (hence the > 2 above).
                        String ancestorPath = configurationTemplateManager.findAncestorPath(path);
                        if(ancestorPath != null)
                        {
                            String message = validationContext.getText(".inancestor", new Object[]{getFieldName(), PathUtils.getPathElements(ancestorPath)[1]});
                            validationContext.addFieldError(getFieldName(), message);
                        }
                        else
                        {
                            List<String> descendentPaths = configurationTemplateManager.getDescendentPaths(path, true, false, false);
                            if(descendentPaths.size() > 0)
                            {
                                List<String> descendentNames = CollectionUtils.map(descendentPaths, new Mapping<String, String>()
                                {
                                    public String map(String descendentPath)
                                    {
                                        return PathUtils.getPathElements(descendentPath)[1];
                                    }
                                });

                                String message;
                                if(descendentNames.size() == 1)
                                {
                                    message = validationContext.getText(".indescendent", new Object[]{getFieldName(), descendentNames.get(0)});
                                }
                                else
                                {
                                    Collections.sort(descendentNames, new Sort.StringComparator());
                                    message = validationContext.getText(".indescendents", new Object[]{getFieldName(), descendentNames.toString()});
                                }
                                
                                validationContext.addFieldError(getFieldName(), message);
                            }
                        }
                    }
                }
            }
        }
    }
}
