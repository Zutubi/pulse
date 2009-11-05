package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.*;

/**
 * Adds report series for expected failures to the default build/stage test
 * breakdown reports if they are found.
 */
public class AddExpectedFailureReportSeriesUpgradeTask extends AbstractUpgradeTask
{
    private static final String REPORTS_PATH = "projects/global project template/reportGroups/test trends/reports";
    private static final String REPORT_BUILD = "test breakdown by build";
    private static final String REPORT_STAGE = "test breakdown averaged per stage";
    private static final String PROPERTY_SERIES = "seriesMap";
    private static final String SERIES_NAME = "expected failure";
    
    protected RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute() throws TaskException
    {
        addSeries(REPORT_BUILD, "zutubi.buildReportSeriesConfig");
        addSeries(REPORT_STAGE, "zutubi.stageReportSeriesConfig");
    }

    private void addSeries(String reportName, String symbolicName)
    {
        String seriesMapPath = PathUtils.getPath(REPORTS_PATH, reportName, PROPERTY_SERIES);
        Record seriesMap = recordManager.select(seriesMapPath);
        if (seriesMap != null && !seriesMap.containsKey(SERIES_NAME))
        {
            recordManager.insert(PathUtils.getPath(seriesMapPath, SERIES_NAME), createSeries(symbolicName));
        }
    }

    private Record createSeries(String symbolicName)
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName(symbolicName);
        record.put("name", SERIES_NAME);
        record.put("metric", "TEST_EXPECTED_FAIL_COUNT");
        record.put("useCustomColour", "true");
        record.put("customColour", "0xf9e0a3");
        return record;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
