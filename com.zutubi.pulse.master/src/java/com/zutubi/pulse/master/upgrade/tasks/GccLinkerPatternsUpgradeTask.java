package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.util.adt.Pair;

import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;

/**
 * Task to add new expressions to GCC processor to catch linker errors.
 */
public class GccLinkerPatternsUpgradeTask extends AbstractRegexProcessorPatternsUpgradeTask
{
    private static final String TYPE_GCC_PROCESSOR = "zutubi.gccPostProcessorConfig";

    private static final String PATTERN_UNDEFINED  = ": undefined reference to";
    private static final String PATTERN_ERROR_EXIT = "^collect2: ld returned [1-9][0-9]* exit status";
    
    @Override
    protected String getProcessorType()
    {
        return TYPE_GCC_PROCESSOR;
    }

    @Override
    protected Iterable<? extends Pair<String, String>> getPatterns()
    {
        return asList(
                asPair(CATEGORY_ERROR, PATTERN_UNDEFINED),
                asPair(CATEGORY_ERROR, PATTERN_ERROR_EXIT)
        );
    }
}
