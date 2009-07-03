package com.zutubi.pulse.master.charting.build;

import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.core.model.ResultCustomFields;
import com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource}
 * that loads the actual custom field values for recipes.  It also caches
 * across multiple uses.
 */
public class DefaultCustomFieldSource implements CustomFieldSource
{
    private File dataRoot;
    private Map<Result, ResultCustomFields> resultToFields = new HashMap<Result, ResultCustomFields>();

    public DefaultCustomFieldSource(File dataRoot)
    {
        this.dataRoot = dataRoot;
    }

    public String getFieldValue(Result result, String name)
    {
        ResultCustomFields customFields = resultToFields.get(result);
        if (customFields == null)
        {
            customFields = new ResultCustomFields(result.getAbsoluteOutputDir(dataRoot));
            resultToFields.put(result, customFields);
        }

        return customFields.load().get(name);
    }
}
