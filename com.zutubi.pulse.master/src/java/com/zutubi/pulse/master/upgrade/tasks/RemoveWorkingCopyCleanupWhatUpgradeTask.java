/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.upgrade.UpgradeException;
import static com.zutubi.pulse.master.upgrade.tasks.RecordUpgradeUtils.hideItem;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This cleanup task is responsible for removing all of the WORKING_COPY_SNAPSHOT
 * options from cleanup configurations.
 *
 * By removing the CleanupWhat option, we potentially invalidate the configuration.
 * An invalid configuration can be identified as one that a false {@link CleanupConfiguration#cleanupAll}
 * property and has no {@link CleanupConfiguration#what} options specified.  If such a
 * configuration has no overrides, it can be removed.  Otherwise, the leaf nodes need to
 * be hidden to ensure the projects remain valid.
 */
public class RemoveWorkingCopyCleanupWhatUpgradeTask extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(RemoveWorkingCopyCleanupWhatUpgradeTask.class);

    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_CLEANUP_WHAT = "what";
    private static final String PROPERTY_CLEANUP_ALL = "cleanupAll";
    private static final String OPTION_WORKING_COPY_SNAPSHOT = "WORKING_COPY_SNAPSHOT";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws UpgradeException
    {
        // Scrub the cleanup working copy option from all of the cleanup configurations.
        RecordLocator recordLocator = RecordLocators.newPathPattern("projects/*/cleanup/*");
        Map<String, Record> recordsToUpgrade = recordLocator.locate(recordManager);
        for (Map.Entry<String, Record> recordEntry: recordsToUpgrade.entrySet())
        {
            String path = recordEntry.getKey();
            MutableRecord mutableRecord = recordEntry.getValue().copy(false, true);

            if (mutableRecord.containsKey(PROPERTY_CLEANUP_WHAT))
            {
                List<String> whats = new LinkedList<String>(Arrays.asList((String[]) mutableRecord.get(PROPERTY_CLEANUP_WHAT)));
                if (whats.contains(OPTION_WORKING_COPY_SNAPSHOT))
                {
                    whats.remove(OPTION_WORKING_COPY_SNAPSHOT);
                    mutableRecord.put(PROPERTY_CLEANUP_WHAT, whats.toArray(new String[whats.size()]));
                    LOG.info("Removing " + OPTION_WORKING_COPY_SNAPSHOT + " from " + path);
                    recordManager.update(path, mutableRecord);
                }
            }
        }

        // Delete any cleanup configuration trees that are now completely invalid.
        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        details.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
            {
                Map<String, Record> records = recordManager.selectAll("projects/" + node.getId() + "/cleanup/*");

                for (final String cleanupConfigurationPath : records.keySet())
                {
                    final String cleanupName = PathUtils.getBaseName(cleanupConfigurationPath);

                    // Only interested in the records where the cleanup configuration is first defined
                    if (isBase(node, cleanupName) && !isValidConfig(records.get(cleanupConfigurationPath)))
                    {
                        final List<String> overridingDescendants = new LinkedList<String>();
                        final List<String> descendants = new LinkedList<String>();

                        // Do any children override this base configuration?
                        for (ScopeHierarchy.Node child : node.getChildren())
                        {
                            child.forEach(new Function<ScopeHierarchy.Node, Boolean>()
                            {
                                public Boolean apply(ScopeHierarchy.Node node)
                                {
                                    // abort any further processing once we have identified an override.
                                    if (overridingDescendants.size() > 0)
                                    {
                                        return false;
                                    }
                                    
                                    String path = "projects/" + node.getId() + "/cleanup/" + cleanupName;
                                    Record r = recordManager.select(path);
                                    if (r != null && !isSkeleton(r))
                                    {
                                        overridingDescendants.add(path);
                                        return false;
                                    }

                                    descendants.add(path);
                                    return true;
                                }
                            });
                        }

                        if (overridingDescendants.size() == 0)
                        {
                            for (String pathToDelete : descendants)
                            {
                                LOG.info("Removing " + pathToDelete);
                                recordManager.delete(pathToDelete);
                            }
                            LOG.info("Removing " + cleanupConfigurationPath);
                            recordManager.delete(cleanupConfigurationPath);
                        }
                    }
                }
                return true;
            }
        });

        // Hide any remaining cleanup configurations that are invalid.
        final List<String> pathsToHide = new LinkedList<String>();
        details.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
            {
                if (!node.hasChildren()) // we are at a leaf
                {
                    Map<String, Record> records = recordManager.selectAll("projects/" + node.getId() + "/cleanup/*");
                    for (final String cleanupConfigurationPath : records.keySet())
                    {
                        final String cleanupName = PathUtils.getBaseName(cleanupConfigurationPath);

                        // validate the leaf node:  get the defined property values.
                        Boolean cleanupAll = getCleanupAll(node, cleanupName);
                        if (cleanupAll != null && cleanupAll)
                        {
                            continue;
                        }
                        String[] what = getCleanupWhat(node, cleanupName);
                        if (what != null && what.length > 0)
                        {
                            continue;
                        }
                        pathsToHide.add(cleanupConfigurationPath);
                    }
                }
                return true;
            }
        });

        for (String pathToHide : pathsToHide)
        {
            String itemToHide = PathUtils.getBaseName(pathToHide);
            String collectionPath = PathUtils.getParentPath(pathToHide);

            Record r = recordManager.select(collectionPath);
            MutableRecord mutableRecord = r.copy(false, true);

            hideItem(mutableRecord, itemToHide);
            LOG.info("Hiding " + pathToHide);
            recordManager.update(collectionPath, mutableRecord);
        }
    }

    private Boolean getCleanupAll(ScopeHierarchy.Node node, String cleanupName)
    {
        Record r = recordManager.select("projects/" + node.getId() + "/cleanup/" + cleanupName);
        if (r.containsKey(PROPERTY_CLEANUP_ALL))
        {
            return Boolean.valueOf((String)r.get(PROPERTY_CLEANUP_ALL));
        }
        ScopeHierarchy.Node parent = node.getParent();
        if (parent != null)
        {
            return getCleanupAll(parent, cleanupName);
        }
        return null;
    }

    private String[] getCleanupWhat(ScopeHierarchy.Node node, String cleanupName)
    {
        Record r = recordManager.select("projects/" + node.getId() + "/cleanup/" + cleanupName);
        if (r.containsKey(PROPERTY_CLEANUP_WHAT))
        {
            return (String[]) r.get(PROPERTY_CLEANUP_WHAT);
        }
        ScopeHierarchy.Node parent = node.getParent();
        if (parent != null)
        {
            return getCleanupWhat(parent, cleanupName);
        }
        return null;
    }

    private boolean isValidConfig(Record r)
    {
        if (r.containsKey(PROPERTY_CLEANUP_WHAT))
        {
            String[] values = (String[]) r.get(PROPERTY_CLEANUP_WHAT);
            if (values != null && values.length > 0)
            {
                return true;
            }
        }
        return r.containsKey(PROPERTY_CLEANUP_ALL) && Boolean.valueOf((String) r.get(PROPERTY_CLEANUP_ALL));
    }

    private boolean isBase(ScopeHierarchy.Node node, String cleanupName)
    {
        // ie: no reference to this configuration in the parent.
        ScopeHierarchy.Node parent = node.getParent();
        return parent == null || recordManager.select("projects/" + parent.getId() + "/cleanup/" + cleanupName) == null;
    }

    private boolean isSkeleton(Record record)
    {
        return !record.containsKey(PROPERTY_CLEANUP_WHAT) && !record.containsKey(PROPERTY_CLEANUP_ALL);
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
