package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains information about all persistent scopes within a record manager.
 */
public class PersistentScopes
{
    private static final String[] TEMPLATED_SCOPES = {"projects", "agents"};

    /**
     * Mapping from scope name to scope details.
     */
    private Map<String, ScopeDetails> scopes = new HashMap<String, ScopeDetails>();

    /**
     * Initialises information about all scopes within the given record
     * manager.
     *
     * @param recordManager record manager which contains the scopes
     */
    public PersistentScopes(RecordManager recordManager)
    {
        List<String> scopeNames = recordManager.getAllPaths(PathUtils.WILDCARD_ANY_ELEMENT);
        for (String scopeName: scopeNames)
        {
            ScopeDetails scope;
            if (CollectionUtils.contains(TEMPLATED_SCOPES, scopeName))
            {
                scope = new TemplatedScopeDetails(scopeName, recordManager);
            }
            else
            {
                scope = new ScopeDetails(scopeName);
            }

            scopes.put(scopeName, scope);
        }
    }

    /**
     * Returns details of a given scope, if the scope exists.
     *
     * @param scopeName name of the scope to look up the details for
     * @return details for the scope, or null if no such scope exists
     */
    public ScopeDetails getScopeDetails(String scopeName)
    {
        return scopes.get(scopeName);
    }

    /**
     * Returns details of the scope in which the given path lives.
     *
     * @param path persistent path to look up the scope details for
     * @return details for the scope in which the path is located, or null if
     *         the path refers to a non-existant scope
     */
    public ScopeDetails findByPath(String path)
    {
        return getScopeDetails(PathUtils.getPathElements(path)[0]);
    }
}
