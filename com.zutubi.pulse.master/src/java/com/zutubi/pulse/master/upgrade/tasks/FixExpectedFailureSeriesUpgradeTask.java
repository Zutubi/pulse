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

package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.*;

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
            
            scopeDetails.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
            {
                public Boolean apply(ScopeHierarchy.Node node)
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
