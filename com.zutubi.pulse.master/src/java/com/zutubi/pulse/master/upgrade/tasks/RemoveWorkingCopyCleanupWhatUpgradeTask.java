package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.upgrade.UpgradeException;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.tove.type.record.*;
import static com.zutubi.tove.type.record.TemplateRecord.HIDDEN_KEY;
import com.zutubi.tove.type.record.store.FileSystemRecordStore;
import com.zutubi.util.UnaryFunction;
import com.zutubi.util.WebUtils;
import com.zutubi.util.logging.Logger;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.*;

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
        return false;
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
        details.getHierarchy().forEach(new UnaryFunction<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean process(ScopeHierarchy.Node node)
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
                            child.forEach(new UnaryFunction<ScopeHierarchy.Node, Boolean>()
                            {
                                public Boolean process(ScopeHierarchy.Node node)
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
        details.getHierarchy().forEach(new UnaryFunction<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean process(ScopeHierarchy.Node node)
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

    public static void hideItem(MutableRecord record, String key)
    {
        Set<String> hiddenKeys = getHiddenKeys(record);
        hiddenKeys.add(key);
        record.putMeta(HIDDEN_KEY, WebUtils.encodeAndJoin(',', hiddenKeys));
    }

    public static Set<String> getHiddenKeys(Record record)
    {
        String hidden = record.getMeta(HIDDEN_KEY);
        if(hidden == null)
        {
            return new HashSet<String>();
        }
        else
        {
            return new HashSet<String>(WebUtils.splitAndDecode(',', hidden));
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

    public static void main(String[] args) throws Exception
    {
        TransactionManager transactionManager = new TransactionManager();
        
        RecordManager recordManager = new RecordManager();
        recordManager.setEventManager(mock(EventManager.class));
        recordManager.setTransactionManager(transactionManager);

        FileSystemRecordStore recordStore = new FileSystemRecordStore();
        recordStore.setTransactionManager(transactionManager);
        recordStore.setPersistenceDirectory(new File("C:\\projects\\pulse\\branches\\2.1.x\\data\\records"));
        recordStore.init();
        
        recordManager.setRecordStore(recordStore);
        recordManager.init();

        RemoveWorkingCopyCleanupWhatUpgradeTask task = new RemoveWorkingCopyCleanupWhatUpgradeTask();
        task.setRecordManager(recordManager);
        task.execute();
    }
}
