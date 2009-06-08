package com.zutubi.pulse.master.tove.config.project.reports;

/**
 * Common chart colour values.
 */
public enum ChartColours
{
    BROKEN(0xeb5a5a),
    SUCCESS(0x96eb96),
    NEUTRAL(0x325aa0);

    private int value;

    ChartColours(int value)
    {
        this.value = value;
    }


    @Override
    public String toString()
    {
        return "0x" + Integer.toHexString(value);
    }
}
