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

package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.postprocessors.api.TestStatus;

/**
 * Stores historical information about a test case.
 */
public class TestCaseIndex extends Entity
{
    private long projectId;
    private long stageNameId;
    private String name;
    private int totalCount;
    private int expectedFailureCount;
    private int failureCount;
    private int errorCount;
    private int skippedCount;
    private long brokenSince = 0;
    private long brokenNumber = 0;

    public TestCaseIndex()
    {
    }

    public TestCaseIndex(long projectId, long stageNameId, String name)
    {
        this.projectId = projectId;
        this.stageNameId = stageNameId;
        this.name = name;
    }

    public void recordExecution(TestStatus status, long buildId, long buildNumber)
    {
        totalCount++;
        switch (status)
        {
            case EXPECTED_FAILURE:
                expectedFailureCount++;
                checkBroken(buildId, buildNumber);
                break;
            case ERROR:
                errorCount++;
                checkBroken(buildId, buildNumber);
                break;
            case FAILURE:
                failureCount++;
                checkBroken(buildId, buildNumber);
                break;
            case PASS:
                brokenSince = 0;
                brokenNumber = 0;
                break;
            case SKIPPED:
                skippedCount++;
        }
    }

    private void checkBroken(long buildId, long buildNumber)
    {
        if (isHealthy())
        {
            brokenSince = buildId;
            brokenNumber = buildNumber;
        }
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public long getStageNameId()
    {
        return stageNameId;
    }

    public void setStageNameId(long stageNameId)
    {
        this.stageNameId = stageNameId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(int totalCount)
    {
        this.totalCount = totalCount;
    }

    public int getExpectedFailureCount()
    {
        return expectedFailureCount;
    }

    public void setExpectedFailureCount(int expectedFailureCount)
    {
        this.expectedFailureCount = expectedFailureCount;
    }

    public int getFailureCount()
    {
        return failureCount;
    }

    public void setFailureCount(int failureCount)
    {
        this.failureCount = failureCount;
    }

    public int getErrorCount()
    {
        return errorCount;
    }

    public void setErrorCount(int errorCount)
    {
        this.errorCount = errorCount;
    }

    public int getSkippedCount()
    {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount)
    {
        this.skippedCount = skippedCount;
    }

    public boolean isHealthy()
    {
        return brokenSince == 0;
    }

    public long getBrokenSince()
    {
        return brokenSince;
    }

    public void setBrokenSince(long brokenSince)
    {
        this.brokenSince = brokenSince;
    }

    public long getBrokenNumber()
    {
        return brokenNumber;
    }

    public void setBrokenNumber(long brokenNumber)
    {
        this.brokenNumber = brokenNumber;
    }
}
