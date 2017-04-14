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

import com.zutubi.pulse.master.model.BuildResult;

/**
 * Context used to accumulate the values for a build or stage metric.
 */
public interface ReportContext extends CustomFieldSource
{
    /**
     * Adds a new metric to the context.
     *
     * @param name name of the metric
     * @param build build the value was extracted from
     * @param value the value of the metric in the build
     */
    void addMetricValue(String name, BuildResult build, Number value);
}
