package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.core.model.TestCaseIndex;

import java.util.List;

/**
 * Provides access to indices for test case results.
 */
public interface TestCaseIndexDao extends EntityDao<TestCaseIndex>
{
    List<TestCaseIndex> findBySuite(long stageNameId, String suite);
    List<TestCaseIndex> findByStage(long stageNameId);
    int deleteByProject(long projectId);
}
