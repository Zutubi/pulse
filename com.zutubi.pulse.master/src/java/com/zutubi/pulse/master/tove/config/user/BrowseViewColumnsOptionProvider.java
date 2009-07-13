package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.BuildColumns;
import com.zutubi.pulse.master.tove.handler.MapOption;
import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
import com.zutubi.tove.type.TypeProperty;
import static com.zutubi.util.CollectionUtils.asOrderedMap;
import static com.zutubi.util.CollectionUtils.asPair;

import java.util.Map;

/**
 * Provides available build information columns for the browse view.
 */
public class BrowseViewColumnsOptionProvider extends MapOptionProvider
{
    private static final Map<String, String> COLUMNS = asOrderedMap(asPair(BuildColumns.KEY_VERSION, "build version"),
                                                                    asPair(BuildColumns.KEY_COMPLETED, "completed time"),
                                                                    asPair(BuildColumns.KEY_ERRORS, "error count"),
                                                                    asPair(BuildColumns.KEY_REASON, "reason"),
                                                                    asPair(BuildColumns.KEY_REVISION, "revision"),
                                                                    asPair(BuildColumns.KEY_ELAPSED, "running/remaining time"),
                                                                    asPair(BuildColumns.KEY_WHEN, "start time"),
                                                                    asPair(BuildColumns.KEY_TESTS, "test summary"),
                                                                    asPair(BuildColumns.KEY_WARNINGS, "warning count"));

    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    protected Map<String, String> getMap(Object instance, String parentPath, TypeProperty property)
    {
        return COLUMNS;
    }
}
