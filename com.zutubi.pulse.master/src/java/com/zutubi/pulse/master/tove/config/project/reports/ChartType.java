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

/**
 * Types of charts that Pulse can render.
 */
public enum ChartType
{
    /**
     * A histogram (vertical bar chart).  Multiple series are shown as bars
     * placed alongside each other.
     */
    BAR_CHART,
    /**
     * A simple line chart.
     */
    LINE_CHART,
    /**
     * A histogram where multiple series are shown as bars stacked on top of
     * each other.
     */
    STACKED_BAR_CHART,
    /**
     * A chart where values are joined by a line and the area underneath is
     * filled.  Multiple series are shown as areas stacked on top of each
     * other.
     */
    STACKED_AREA_CHART
}
