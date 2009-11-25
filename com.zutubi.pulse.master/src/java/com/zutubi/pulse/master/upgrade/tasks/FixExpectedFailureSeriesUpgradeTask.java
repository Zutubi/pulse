package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.UnaryFunction;

public class FixExpectedFailureSeriesUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE = "projects";
    private static final String REPORT_GROUP_PATH = "reportGroups/test trends/reports";
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
        PersistentScopes scopes = new PersistentScopes(recordManager);
        TemplatedScopeDetails scope = (TemplatedScopeDetails) scopes.getScopeDetails(SCOPE);
        fixSeries(scope, REPORT_BUILD, "zutubi.buildReportSeriesConfig", false);
        fixSeries(scope, REPORT_STAGE, "zutubi.stageReportSeriesConfig", true);
    }

    private void fixSeries(TemplatedScopeDetails scopeDetails, final String reportName, String symbolicName, boolean aggregate)
    {
        ScopeHierarchy.Node root = scopeDetails.getHierarchy().getRoot();
        String seriesPath = PathUtils.getPath(SCOPE, root.getId(), REPORT_GROUP_PATH, reportName, PROPERTY_SERIES, SERIES_NAME);
        Record seriesRecord = recordManager.select(seriesPath);
        if (seriesRecord != null)
        {
            if (aggregate)
            {
                MutableRecord mutableRecord = seriesRecord.copy(false, true);
                mutableRecord.put("combineStages", "true");
                mutableRecord.put("aggregationFunction", "MEAN");
                recordManager.update(seriesPath, mutableRecord);
            }

            final MutableRecord skeleton = new MutableRecordImpl();
            skeleton.setSymbolicName(symbolicName);
            
            scopeDetails.getHierarchy().forEach(new UnaryFunction<ScopeHierarchy.Node, Boolean>()
            {
                public Boolean process(ScopeHierarchy.Node node)
                {
                    if (node.getParent() == null)
                    {
                        return true;
                    }

                    String seriesMapPath = PathUtils.getPath(SCOPE, node.getId(), REPORT_GROUP_PATH, reportName, PROPERTY_SERIES);
                    if (recordManager.containsRecord(seriesMapPath))
                    {
                        String seriesPath = PathUtils.getPath(seriesMapPath, SERIES_NAME);
                        recordManager.insert(seriesPath, skeleton);
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            });
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
