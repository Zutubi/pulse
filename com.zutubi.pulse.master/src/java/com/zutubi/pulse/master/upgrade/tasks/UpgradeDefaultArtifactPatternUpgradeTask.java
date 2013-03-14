package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;

/**
 * Update any artifact patterns that match the previous default value to
 * the new default value.
 */
public class UpgradeDefaultArtifactPatternUpgradeTask  extends AbstractRecordPropertiesUpgradeTask
{
    private static final String DEFAULT_ORIGINAL = "(.+)\\\\.(.+)";
    private static final String DEFAULT_NEW = "(.*?)(?:\\.([^.]*))?";
    private static final String SCOPE = "projects";
    private static final String PROPERTY_ARTIFACT_PATTERN = "artifactPattern";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE, WILDCARD_ANY_ELEMENT, "type", "recipes", WILDCARD_ANY_ELEMENT, "commands", WILDCARD_ANY_ELEMENT, "artifacts", WILDCARD_ANY_ELEMENT));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newEditProperty(PROPERTY_ARTIFACT_PATTERN, new UpdateDefaultValues())
        );
    }

    public boolean haltOnFailure()
    {
        // Failure here is not fatal.  User can always manually update the values.
        return false;
    }

    private class UpdateDefaultValues implements Function<Object, Object>
    {
        public Object apply(Object o)
        {
            if (o == null)
            {
                return null;
            }

            String current = o.toString();
            if (current.equals(DEFAULT_ORIGINAL))
            {
                return DEFAULT_NEW;
            }
            return current;
        }
    }
}
