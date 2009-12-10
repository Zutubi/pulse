package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.util.Map;

/**
 * A local version of the factory methods used by ivy to create module revision ids.
 *
 * @see ModuleRevisionId
 */
public class IvyModuleRevisionId
{
    private IvyModuleRevisionId()
    {
        // ensure that this class can not be instantiated.
    }

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
