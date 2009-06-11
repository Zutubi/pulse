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
