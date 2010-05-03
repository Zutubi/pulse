package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.UnaryFunction;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Updates regex-test post-processors to allow them to have multiple strings
 * for each test status.
 */
public class RegexTestPostProcessorMultipleStatusesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String TYPE = "zutubi.regexTestPostProcessorConfig";

    private static final String PROPERTY_PASS_STATUS = "passStatus";
    private static final String PROPERTY_FAILURE_STATUS = "failureStatus";
    private static final String PROPERTY_ERROR_STATUS = "errorStatus";
    private static final String PROPERTY_SKIPPED_STATUS = "skippedStatus";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern(
                PathUtils.getPath("projects/*/postProcessors/*")
        ), TYPE);
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        ToArrayFn editFn = new ToArrayFn();
        return asList(
                RecordUpgraders.newEditProperty(PROPERTY_PASS_STATUS, editFn),
                RecordUpgraders.newEditProperty(PROPERTY_FAILURE_STATUS, editFn),
                RecordUpgraders.newEditProperty(PROPERTY_ERROR_STATUS, editFn),
                RecordUpgraders.newEditProperty(PROPERTY_SKIPPED_STATUS, editFn)
                
        );
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    private static class ToArrayFn implements UnaryFunction<Object, Object>
    {
        public Object process(Object o)
        {
            if (o == null)
            {
                return null;
            }
            else if (o instanceof String)
            {
                return new String[]{(String) o};
            }
            else
            {
                return o;
            }
        }
    }
}