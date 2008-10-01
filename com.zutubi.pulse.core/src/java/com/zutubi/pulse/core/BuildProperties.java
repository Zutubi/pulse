package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.util.TextUtils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Defines the property names for various build properties.
 */
public class BuildProperties
{
    // Namespaces
    public static final String NAMESPACE_INTERNAL = "internal";
    public static final String NAMESPACE_USER     = "user";

    // Scope labels
    public static final String SCOPE_RECIPE = "recipe";
    
    // Shared properties
    public static final String PROPERTY_DIRECTORY               = "dir";
    public static final String PROPERTY_STATUS                  = "status";

    // Build level properties
    public static final String PROPERTY_BUILD_COUNT             = "build.count";
    public static final String PROPERTY_BUILD_DIRECTORY         = "build.dir";
    public static final String PROPERTY_BUILD_NUMBER            = "build.number";
    public static final String PROPERTY_BUILD_REASON            = "build.reason";
    public static final String PROPERTY_BUILD_REVISION          = "build.revision";
    public static final String PROPERTY_BUILD_TIMESTAMP         = "build.timestamp";
    public static final String PROPERTY_BUILD_TIMESTAMP_MILLIS  = "build.timestamp.millis";
    public static final String PROPERTY_BUILD_TRIGGER           = "build.trigger";
    public static final String PROPERTY_CLEAN_BUILD             = "clean.build";
    public static final String PROPERTY_COMPRESS_ARTIFACTS      = "compress.artifacts";
    public static final String PROPERTY_COMPRESS_WORKING_DIR    = "compress.working.dir";
    public static final String PROPERTY_INCREMENTAL_BUILD       = "incremental.build";
    public static final String PROPERTY_MASTER_URL              = "master.url";
    public static final String PROPERTY_PROJECT                 = "project";
    public static final String PROPERTY_SUCCESS_COUNT           = "success.count";
    public static final String PROPERTY_TEST_SUMMARY            = "test.summary";

    // Stage/Recipe level properties
    public static final String PROPERTY_AGENT                   = "agent";
    public static final String PROPERTY_BASE_DIR                = "base.dir";
    public static final String PROPERTY_FILE_REPOSITORY         = "file.repository";
    public static final String PROPERTY_RECIPE                  = "recipe";
    public static final String PROPERTY_RECIPE_ID               = "recipe.id";
    public static final String PROPERTY_RECIPE_PATHS            = "recipe.paths";
    public static final String PROPERTY_RECIPE_TIMESTAMP        = "recipe.timestamp";
    public static final String PROPERTY_RECIPE_TIMESTAMP_MILLIS = "recipe.timestamp.millis";
    public static final String PROPERTY_RESOURCE_REPOSITORY     = "resource.repository";
    public static final String PROPERTY_STAGE                   = "stage";
    public static final String PROPERTY_TEST_RESULTS            = "test.results";

    // Command level properties
    public static final String PROPERTY_OUTPUT_DIR             = "output.dir";

    // Other constants
    public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    public static void addResourceProperties(ExecutionContext context, List<ResourceRequirement> resourceRequirements, ResourceRepository resourceRepository)
    {
        if (resourceRequirements != null)
        {
            for(ResourceRequirement requirement: resourceRequirements)
            {
                Resource resource = resourceRepository.getResource(requirement.getResource());
                if(resource == null)
                {
                    return;
                }

                for(ResourceProperty property: resource.getProperties().values())
                {
                    context.add(property);
                }

                String importVersion = requirement.getVersion();
                if(requirement.isDefaultVersion())
                {
                    importVersion = resource.getDefaultVersion();
                }

                if(TextUtils.stringSet(importVersion))
                {
                    ResourceVersion version = resource.getVersion(importVersion);
                    if(version == null)
                    {
                        return;
                    }

                    for(ResourceProperty property: version.getProperties().values())
                    {
                        context.add(property);
                    }
                }
            }
        }
    }
}
