package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.util.Map;

/**
 * A factory for the ivy ModuleRevisionId instance that provides a set of convenience methods
 * mapping Pulse data types to ModuleRevisionId.
 */
public class IvyModuleRevisionId
{
    public static ModuleRevisionId newInstance(String org, String module, String revision)
    {
        return ModuleRevisionId.newInstance(org, module, revision);
    }

    public static ModuleRevisionId newInstance(String org, String module, String revision, Map extraAttributes)
    {
        return ModuleRevisionId.newInstance(org, module, revision, extraAttributes);
    }

    public static ModuleRevisionId newInstance(String org, String name, String branch, String revision)
    {
        return ModuleRevisionId.newInstance(org, name, branch, revision);
    }
}
