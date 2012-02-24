package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Removes the checkoutScheme from SCMs as it has been replace with more general
 * bootstrap options.
 */
public class RemoveCheckoutSchemeUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern("projects/*/scm");
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newDeleteProperty("checkoutScheme"));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
