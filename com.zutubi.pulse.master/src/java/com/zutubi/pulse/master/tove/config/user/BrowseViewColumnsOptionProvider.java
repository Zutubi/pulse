package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.tove.handler.MapOption;
import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
import com.zutubi.tove.type.TypeProperty;

import java.util.Map;

import static com.zutubi.pulse.master.tove.config.user.ProjectsSummaryConfiguration.*;
import static com.zutubi.util.CollectionUtils.asOrderedMap;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Provides available build information columns for the browse view.
 */
public class BrowseViewColumnsOptionProvider extends MapOptionProvider
{
    private static final Map<String, String> COLUMNS = asOrderedMap(asPair(KEY_VERSION, "build version"),
                                                                    asPair(KEY_COMPLETED, "completed time"),
                                                                    asPair(KEY_ERRORS, "error count"),
                                                                    asPair(KEY_MATURITY, "maturity"),
                                                                    asPair(KEY_REASON, "reason"),
                                                                    asPair(KEY_REVISION, "revision"),
                                                                    asPair(KEY_ELAPSED, "running/remaining time"),
                                                                    asPair(KEY_WHEN, "start time"),
                                                                    asPair(KEY_TESTS, "test summary"),
                                                                    asPair(KEY_WARNINGS, "warning count"));

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    protected Map<String, String> getMap(Object instance, String parentPath, TypeProperty property)
    {
        return COLUMNS;
    }
}
