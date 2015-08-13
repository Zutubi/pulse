package com.zutubi.pulse.master.tove.config;

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
import java.util.Collections;
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

    public Option getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        // An empty option is always available from getMap.
        return null;
    }

    /**
     * Gets the map of options, which for references maps from paths to names.  The parameters are tweaked vs our
     * super implementation, hence the documentation here.  See also the {@link com.zutubi.pulse.master.tove.handler.ReferenceAnnotationHandler}
     * which does the tweaking.
     *
     * @param referencedScopeInstance this instance defines where reference-able instances can be found.  In the normal
     *                                case this is the same instance the reference is coming from.  However, when the
     *                                reference is dependentOn another field, that field determines the reference-able
     *                                scope.  (At the time of writing the example is configuration of selected stages in
     *                                dependencies, the stage options are from the upstream project, not the project
     *                                defining the dependency.)
     * @param referencingFieldParentPath this always refers to the parent path for the field we are providing options
     *                                   for.  This will correspond with the referencedScopeInstance in the normal case,
     *                                   but will differ in the dependentOn case.
     * @param property the property defining the field we are providing options for
     * @return available options for the given property under the given referencingFieldParentPath.  These are instances
     *         of the right type in the closest owning scope (of that type) of the referencedScopeInstance.
     */
    public Map<String, String> getMap(Object referencedScopeInstance, String referencingFieldParentPath, TypeProperty property)
    {
        if (referencedScopeInstance == null)
        {
            // We don't yet know where we are looking for the reference-able instances.
            return Collections.emptyMap();
        }

        ReferenceType referenceType = (ReferenceType) property.getType().getTargetType();
        Configuration referencedScopeConfiguration = (Configuration) referencedScopeInstance;
        Collection<Configuration> referencable = configurationReferenceManager.getReferencableInstances(referenceType.getReferencedType(), referencedScopeConfiguration.getConfigurationPath());
        Map<String, String> options = new LinkedHashMap<String, String>();

        // Empty option (empty path == null reference).  This is the initial
        // selection when a new instance is being created - required validation
        // can be used to ensure a selection is made.
        options.put("", "");

        String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(referencingFieldParentPath);
        for (Configuration r : referencable)
        {
            if (configurationSecurityManager.hasPermission(r.getConfigurationPath(), AccessManager.ACTION_VIEW))
            {
                try
                {
                    options.put(r.getConfigurationPath(), (String) BeanUtils.getProperty(referenceType.getIdProperty(), r));
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
