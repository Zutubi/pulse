/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.charting.build;

import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.core.model.ResultCustomFields;
import com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource;
import com.zutubi.util.adt.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.zutubi.util.CollectionUtils.asPair;

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
        return getFieldsForResult(result).get(name);
    }

    public List<Pair<String, String>> getAllFieldValues(Result result, Pattern namePattern)
    {
        List<Pair<String, String>> values = new ArrayList<Pair<String, String>>();
        Map<String, String> fields = getFieldsForResult(result);
        for (Map.Entry<String, String> entry: fields.entrySet())
        {
            if (namePattern.matcher(entry.getKey()).matches())
            {
                values.add(asPair(entry.getKey(), entry.getValue()));
            }
        }

        return values;
    }

    private Map<String, String> getFieldsForResult(Result result)
    {
        ResultCustomFields customFields = resultToFields.get(result);
        if (customFields == null)
        {
            customFields = new ResultCustomFields(result.getAbsoluteOutputDir(dataRoot));
            resultToFields.put(result, customFields);
        }

        return customFields.load();
    }
}
