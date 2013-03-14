package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * As part of the refactor of the cleanup configuration, the cleanup what
 * WHOLE_BUILD option has been replaced by a checkbox.  The cleanup what
 * has also changed meaning and is now a multi select field.
 */
public class RefactorCleanupWhatFieldUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_CLEANUP_ALL = "cleanupAll";
    private static final String PROPERTY_WHAT = "what";
    private static final String PROPERTY_UNIT = "unit";
    private static final String CLEANUPUNIT_BUILDS = "BUILDS";
    private static final String CLEANUPWHAT_WORKING_DIRECTORIES_ONLY = "WORKING_DIRECTORIES_ONLY";
    private static final String CLEANUPWHAT_WORKING_COPY_SNAPSHOT = "WORKING_COPY_SNAPSHOT";
    private static final String CLEANUPWHAT_BUILD_ARTIFACTS = "BUILD_ARTIFACTS";
    private static final String CLEANUPWHAT_WHOLE_BUILDS = "WHOLE_BUILDS";


    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "cleanup", PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList((RecordUpgrader)new CleanupRecordUpgrader());
    }

    // A: locate the cleanup configurations.
    // B: if whole build selected, set cleanup all to true, else set cleanup all to false.
    // C: change what to list, BUILD_ARTIFACTS -> BUILD_ARTIFACTS + WORKING_COPY_SNAPSHOT etc.

    class CleanupRecordUpgrader implements RecordUpgrader
    {
        public void upgrade(String path, MutableRecord record)
        {
            if (record.containsKey(PROPERTY_CLEANUP_ALL))
            {
                // Defend against multiple runs as best we can.
                return;
            }

            String what = (String) record.get(PROPERTY_WHAT);

            // if no field value for what is defined, then we move on, this is not where
            // we want ot be making changes.

            if (what == null || what.equals(""))
            {
                return;
            }

            if (what.equals(CLEANUPWHAT_WHOLE_BUILDS))
            {
                record.put(PROPERTY_CLEANUP_ALL, "true");
            }
            else
            {
                record.put(PROPERTY_CLEANUP_ALL, "false");
            }

            // convert the what field to a list.
            if (what.equals(CLEANUPWHAT_WORKING_DIRECTORIES_ONLY))
            {
                record.put(PROPERTY_WHAT, new String[]{CLEANUPWHAT_WORKING_COPY_SNAPSHOT});
            }
            else if (what.equals(CLEANUPWHAT_BUILD_ARTIFACTS))
            {
                record.put(PROPERTY_WHAT, new String[]{CLEANUPWHAT_WORKING_COPY_SNAPSHOT, CLEANUPWHAT_BUILD_ARTIFACTS});
            }
            else if (what.equals(CLEANUPWHAT_WHOLE_BUILDS))
            {
                record.put(PROPERTY_WHAT, new String[]{});
            }

            // if no unit is selected, add a default.
            if (!record.containsKey(PROPERTY_UNIT))
            {
                record.put(PROPERTY_UNIT, CLEANUPUNIT_BUILDS);
            }
        }
    }
}
