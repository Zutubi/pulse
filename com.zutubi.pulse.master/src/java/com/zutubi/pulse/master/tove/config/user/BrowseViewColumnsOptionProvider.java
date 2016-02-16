package com.zutubi.pulse.master.tove.config.user;

import com.google.common.collect.ImmutableMap;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.handler.MapOptionProvider;

import java.util.Map;

import static com.zutubi.pulse.master.tove.config.user.ProjectsSummaryConfiguration.*;

/**
 * Provides available build information columns for the browse view.
 */
public class BrowseViewColumnsOptionProvider extends MapOptionProvider
{
    private static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder()
            .put(KEY_VERSION, "build version")
            .put(KEY_COMPLETED, "completed time")
            .put(KEY_ERRORS, "error count")
            .put(KEY_MATURITY, "maturity")
            .put(KEY_REASON, "reason")
            .put(KEY_REVISION, "revision")
            .put(KEY_ELAPSED, "running/remaining time")
            .put(KEY_WHEN, "start time")
            .put(KEY_TESTS, "test summary")
            .put(KEY_WARNINGS, "warning count")
            .build();

    public Option getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    protected Map<String, String> getMap(TypeProperty property, FormContext context)
    {
        return COLUMNS;
    }
}
