package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.*;
import com.zutubi.util.UnaryFunction;
import com.zutubi.util.WebUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Some utility methods to assist in dealing with records in upgrade tasks.
 */
public class RecordUpgradeUtils
{
    private static final String HIDDEN_KEY = "hidden";

    /**
     * Mark the item identified by the key as hidden.
     *
     * @param record    the record to be updated
     * @param key       the key identifying the item to be hidden
     */
    public static void hideItem(MutableRecord record, String key)
    {
        Set<String> hiddenKeys = getHiddenKeys(record);
        hiddenKeys.add(key);
        record.putMeta(HIDDEN_KEY, WebUtils.encodeAndJoin(',', hiddenKeys));
    }

    /**
     * Get the list of hidden keys defined in the record.
     *
     * @param record    the record from which the hidden keys are retrieved.
     * @return a set of hidden keys.
     */
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

    /**
     * Inserts the given record at the given path, also inserting any necessary
     * inherited skeletons.
     * 
     * @param path           path to insert the record at
     * @param record         the record to insert
     * @param scope          details of the templated scope being inserted into
     * @param recordManager  used to manipulate the records
     */
    public static void insertWithSkeletons(String path, Record record, TemplatedScopeDetails scope, final RecordManager recordManager)
    {
        recordManager.insert(path, record);
        
        final String[] elements = PathUtils.getPathElements(path);
        final String remainderPath = PathUtils.getPath(2, elements);
        ScopeHierarchy.Node owner = scope.getHierarchy().findNodeById(elements[1]);

        final Record skeleton = RecordUtils.createSkeletonOf(record);
        owner.forEach(new UnaryFunction<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean process(ScopeHierarchy.Node node)
            {
                String path = PathUtils.getPath(elements[0], node.getId(), remainderPath);
                if (!recordManager.containsRecord(path))
                {
                    recordManager.insert(path, skeleton);
                }

                return true;
            }
        });
    }
}
