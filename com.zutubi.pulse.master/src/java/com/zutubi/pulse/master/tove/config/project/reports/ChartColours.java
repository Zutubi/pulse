package com.zutubi.pulse.master.tove.config.project.reports;

import java.awt.*;

/**
 * Common chart colour values.
 */
public enum ChartColours
{
    BROKEN_FILL(0xeb5a5a),
    BUSY_FILL(0x5b7dc2),
    DISABLED_FILL(0xeb5a5a),
    EXPECTED_FAIL_FILL(0xf9e0a3),
    FAIL_FILL(0xffdddd),
    SUCCESS_FILL(0x96eb96),
    WARNING_FILL(0xffffce),
    NOTHING_FILL(0xe0e0e0),
    NEUTRAL_LINE(0x325aa0),
    ERROR_LINE(0xc00000),
    WARNING_LINE(0xf0c000);

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

    public Color asColor()
    {
        return new Color(value);
    }
}
