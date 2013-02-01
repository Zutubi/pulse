package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Updates the project persistentWorkDir property to allow for the new
 * ${agent.data.dir} property (in place of ${data.dir}, which is still
 * available but no longer the default place for agent data).
 */
public class UpdatePersistentWorkDirUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE = "projects";
    private static final String PROPERTY_OPTIONS = "options";
    private static final String PROPERTY_PERSISTENT_WORK_DIR = "persistentWorkDir";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_OPTIONS));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty(PROPERTY_PERSISTENT_WORK_DIR, new Function<Object, Object>()
        {
            public Object apply(Object o)
            {
                if (o == null)
                {
                    return null;
                }

                String current = (String) o;
                return current.replace("${data.dir}", "${agent.data.dir}");
            }
        }));
    }
}
