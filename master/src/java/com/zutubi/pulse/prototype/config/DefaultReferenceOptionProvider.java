package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.MapOptionProvider;
import com.zutubi.prototype.MapOption;
import com.zutubi.prototype.config.ConfigurationReferenceManager;
import com.zutubi.prototype.type.ReferenceType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.pulse.core.config.Configuration;
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

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        // A zero handle means a null reference
        return new MapOption("0", "");
    }

    public Map<String,String> getMap(Object instance, String path, TypeProperty property)
    {
        ReferenceType referenceType = (ReferenceType) property.getType().getTargetType();
        Collection<Configuration> referencable = configurationReferenceManager.getReferencableInstances(referenceType.getReferencedType(), path);
        Map<String, String> options = new LinkedHashMap<String, String>();

        for(Configuration r: referencable)
        {
            try
            {
                options.put(Long.toString(r.getHandle()), (String) BeanUtils.getProperty(referenceType.getIdProperty(), r));
            }
            catch (BeanException e)
            {
                LOG.severe(e);
            }
        }
        
        return options;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }
}
