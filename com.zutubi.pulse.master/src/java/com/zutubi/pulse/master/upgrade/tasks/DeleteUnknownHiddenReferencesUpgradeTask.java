package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import java.util.Set;
import java.util.HashSet;

/**
 * This upgrade task traverses the record store, removing references to hidden keys that
 * are no longer valid.  
 */
public class DeleteUnknownHiddenReferencesUpgradeTask extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(DeleteUnknownHiddenReferencesUpgradeTask.class);

    private static final String HIDDEN_KEY    = "hidden";
    private static final char SEPARATOR = ',';

    private RecordManager recordManager;

    public void execute() throws TaskException
    {
        traverse("", recordManager.select(), new UnaryProcedure<String>()
        {
            public void process(String path)
            {
                Record record = recordManager.select(path);
                Set<String> hiddenKeys = getHiddenKeys(record);
                if (hiddenKeys.size() == 0)
                {
                    return;
                }

                // we have hidden keys, lets find the parent..
                String[] pathElements = PathUtils.getPathElements(path);
                if (pathElements.length < 2)
                {
                    // for us to find a parent from which elements can be hidden, we need to have
                    // at least a templated 'scope' AND the 'owner'.
                    LOG.warning("Hidden keys found in path " + path + " ignored, too short.");
                    return;
                }

                String scope = pathElements[0];
                String owner = pathElements[1];

                Record owningRecord = recordManager.select(PathUtils.getPath(scope, owner));
                String owningParentPath = getTemplateParentPath(owningRecord);
                if (owningParentPath == null)
                {
                    LOG.warning("Hidden keys found in path " + path + " ignored, no parent path.");
                    LOG.warning(" - owning path: " + PathUtils.getPath(scope, owner));
                    LOG.warning(" - parent handle: " + owningRecord.getMeta(TemplateRecord.PARENT_KEY));
                    LOG.warning(" - parent path: " + recordManager.getPathForHandle(Long.parseLong(owningRecord.getMeta(TemplateRecord.PARENT_KEY))));
                    return;
                }

                String[] parentPath = new String[pathElements.length];
                parentPath[0] = scope;
                parentPath[1] = PathUtils.getPathElements(owningParentPath)[1];
                System.arraycopy(pathElements, 2, parentPath, 2, parentPath.length - 2);

                Record parent = recordManager.select(PathUtils.getPath(parentPath));
                
                for (String hiddenKey : hiddenKeys)
                {
                    if (!parent.containsKey(hiddenKey))
                    {
                        MutableRecord editableCopy = record.copy(false);
                        restoreItem(editableCopy, hiddenKey);
                        LOG.info("Removing unknown hidden reference " + hiddenKey + " from " + path);
                        recordManager.update(path, editableCopy);
                    }
                }
            }
        });
    }

    private String getTemplateParentPath(Record record)
    {
        long handle = getTemplateParentHandle(record);
        if (handle != 0)
        {
            return recordManager.getPathForHandle(handle);
        }
        return null;
    }

    private long getTemplateParentHandle(Record record)
    {
        String parentString = record.getMeta(TemplateRecord.PARENT_KEY);
        if (parentString != null)
        {
            try
            {
                return Long.parseLong(parentString);
            }
            catch (NumberFormatException e)
            {
                // noop.
            }
        }
        return 0;
    }

    private Set<String> getHiddenKeys(Record record)
    {
        String hidden = record.getMeta(HIDDEN_KEY);
        if(hidden == null)
        {
            return new HashSet<String>();
        }
        else
        {
            return new HashSet<String>(StringUtils.splitAndDecode(SEPARATOR, hidden));
        }
    }

    private boolean restoreItem(MutableRecord record, String key)
    {
        Set<String> hiddenKeys = getHiddenKeys(record);
        boolean result = hiddenKeys.remove(key);
        if(hiddenKeys.size() == 0)
        {
            record.removeMeta(HIDDEN_KEY);
        }
        else
        {
            record.putMeta(HIDDEN_KEY, StringUtils.encodeAndJoin(SEPARATOR, hiddenKeys));
        }

        return result;
    }

    private void traverse(String path, Record record, UnaryProcedure<String> procedure)
    {
        if (path.length() != 0)
        {
            procedure.process(path);
        }

        for (String key : record.keySet())
        {
            Object value = record.get(key);
            if (value instanceof MutableRecord)
            {
                String nestedPath = PathUtils.getPath(path, key);
                MutableRecord nestedRecord = (MutableRecord) value;
                traverse(nestedPath, nestedRecord, procedure);
            }
        }
    }

    public boolean haltOnFailure()
    {
        // A running instance can handle the existance of these 'unknown' hidden references,
        // so we can continue on failure.
        return false;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
