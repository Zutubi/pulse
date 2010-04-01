package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
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


}
