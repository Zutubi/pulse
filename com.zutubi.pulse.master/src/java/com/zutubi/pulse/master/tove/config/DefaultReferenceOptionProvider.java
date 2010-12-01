package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.master.tove.handler.MapOption;
import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.ReferenceType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.bean.BeanException;
import com.zutubi.util.bean.BeanUtils;
import com.zutubi.util.logging.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An option provider for lists that allow references to be selected.  This
 * provider lists all references of the appropriate type.
 */
public class DefaultReferenceOptionProvider extends MapOptionProvider
{
    private static final Logger LOG = Logger.getLogger(DefaultReferenceOptionProvider.class);

    private ConfigurationReferenceManager configurationReferenceManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        // An empty option is always available from getMap.
        return null;
    }

    public Map<String, String> getMap(Object instance, String path, TypeProperty property)
    {
        ReferenceType referenceType = (ReferenceType) property.getType().getTargetType();
        Collection<Configuration> referencable = configurationReferenceManager.getReferencableInstances(referenceType.getReferencedType(), path);
        Map<String, String> options = new LinkedHashMap<String, String>();

        // Empty option (0 handle == null reference).  This is the initial
        // selection when a new instance is being created - required validation
        // can be used to ensure a selection is made.
        options.put("0", "");

        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);
        for (Configuration r : referencable)
        {
            if (configurationSecurityManager.hasPermission(r.getConfigurationPath(), AccessManager.ACTION_VIEW))
            {
                try
                {
                    long handle = configurationReferenceManager.getReferenceHandleForPath(templateOwnerPath, r.getConfigurationPath());
                    options.put(Long.toString(handle), (String) BeanUtils.getProperty(referenceType.getIdProperty(), r));
                }
                catch (BeanException e)
                {
                    LOG.severe(e);
                }
            }
        }

        return options;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
