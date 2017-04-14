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
 * Types of metric values that may be charted.
 */
public enum MetricType
{
    /**
     * The metric values are whole numbers.
     */
    INTEGRAL
    {
        public Number parse(String input)
        {
            return Long.parseLong(input);
        }
    },
    /**
     * The metric values are real numbers represented as double-precision
     * floating point.
     */
    FLOATING_POINT
    {
        public Number parse(String input)
        {
            return Double.parseDouble(input);
        }
    };

    /**
     * Parses a string into a number based on this value type.
     *
     * @param input the string to parse.
     * @return the metric value, in numerical form
     * @throws NumberFormatException if the string cannot be parsed
     */
    public abstract Number parse(String input);
}
