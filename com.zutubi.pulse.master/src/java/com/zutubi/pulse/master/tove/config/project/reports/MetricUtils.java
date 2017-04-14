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

package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;

/**
 * Utilities for build and stage metrics.
 */
public final class MetricUtils
{
    private static final Logger LOG = Logger.getLogger(MetricUtils.class);

    private MetricUtils()
    {
        // do not instantiate
    }

    /**
     * Extracts metrics for a custom field from a result and adds them to a context.
     *
     * @param buildResult the build the result is from
     * @param result the actual result to get the metrics from
     * @param config configuration of the series defining the custom field metric
     * @param context context to add metric values to
     */
    public static void extractMetrics(BuildResult buildResult, Result result, ReportSeriesConfiguration config, ReportContext context)
    {
        String fieldName = config.getField();
        List<Pair<String, String>> values;
        try
        {
            Pattern pattern = Pattern.compile(fieldName);
            values = context.getAllFieldValues(result, pattern);
        }
        catch (PatternSyntaxException e)
        {
            LOG.warning("Unable to parse field name '" + fieldName + "' as a regular expression: " + e.getMessage(), e);

            // Just treat it as a straight value rather than a pattern.
            String fieldValue = context.getFieldValue(result, fieldName);
            if (fieldValue == null)
            {
                values = Collections.emptyList();
            }
            else
            {
                values = asList(asPair(fieldName, fieldValue));
            }
        }

        for (Pair<String, String> value : values)
        {
            try
            {
                context.addMetricValue(value.first, buildResult, config.getFieldType().parse(value.second));
            }
            catch (NumberFormatException e)
            {
                LOG.warning("Unable to parse value of field '" + value.first + "' (" + value.second + ") as a number for reporting");
            }
        }
    }
}
