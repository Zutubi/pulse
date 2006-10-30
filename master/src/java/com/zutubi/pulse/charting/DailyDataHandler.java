package com.zutubi.pulse.charting;

import java.util.Date;

/**
 */
public interface DailyDataHandler
{
    void handle(Date day, DailyData data);
}
