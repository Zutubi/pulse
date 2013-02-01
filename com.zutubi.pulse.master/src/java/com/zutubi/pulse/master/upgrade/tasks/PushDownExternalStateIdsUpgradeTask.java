package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * An upgrade task to remove external state ids from project and agent templates.  These may exist
 * due to earlier bugs.
 */
public class PushDownExternalStateIdsUpgradeTask extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(PushDownExternalStateIdsUpgradeTask.class);

    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_PROJECT_ID = "projectId";
    private static final String SCOPE_AGENTS = "agents";
    private static final String PROPERTY_AGENT_ID = "agentStateId";
    private static final String NO_STATE = "0";

    private RecordManager recordManager;
    
    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute() throws TaskException
    {
        PersistentScopes scopes = new PersistentScopes(recordManager);
        fixScope(scopes, SCOPE_PROJECTS, PROPERTY_PROJECT_ID);
        fixScope(scopes, SCOPE_AGENTS, PROPERTY_AGENT_ID);
    }

    private void fixScope(PersistentScopes scopes, final String scope, final String property)
    {
        final Map<String, Object> removedIds = new HashMap<String, Object>();
        
        TemplatedScopeDetails scopeDetails = (TemplatedScopeDetails) scopes.getScopeDetails(scope);
        scopeDetails.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
            {
                String path = PathUtils.getPath(scope, node.getId());
                Record record = recordManager.select(path);
                if (record != null)
                {
                    Object idValue = record.get(property);
                    if (node.isTemplate())
                    {
                        if (idValue != null && !idValue.equals(NO_STATE))
                        {
                            LOG.warning("Removing id '" + idValue + "' from '" + path + "'");
                            removedIds.put(node.getId(), idValue);
                            MutableRecord copy = record.copy(false, true);
                            copy.put(property, NO_STATE);
                            recordManager.update(path, copy);
                        }
                    }
                    else
                    {
                        if (idValue == null || idValue.equals(NO_STATE))
                        {
                            // Search up the hierarchy to find which id we inherit.
                            for (ScopeHierarchy.Node parent = node.getParent(); parent != null; parent = parent.getParent())
                            {
                                // We actually remove the id as we can only push it down to one
                                // child.  Other children will have to make do with new, blank state.
                                Object inheritedId = removedIds.remove(parent.getId());
                                if (inheritedId != null)
                                {
                                    LOG.warning("Claiming id '" + inheritedId + "' for '" + path + "'");
                                    MutableRecord copy = record.copy(false, true);
                                    copy.put(property, inheritedId);
                                    recordManager.update(path, copy);
                                    return true;
                                }
                            }
                            
                            // If we got here, we found no id to inherit.  Just report this.
                            LOG.warning("No id for '" + path + "': new blank state will be created.");
                        }
                    }
                }
                
                return true;
            }
        });
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
