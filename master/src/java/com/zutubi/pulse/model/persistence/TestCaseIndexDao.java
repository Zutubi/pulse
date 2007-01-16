package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.core.model.TestCaseIndex;

import java.util.List;

/**
 */
public interface TestCaseIndexDao extends EntityDao<TestCaseIndex>
{
    TestCaseIndex findByCase(long stageNameId, String name);
    List<TestCaseIndex> findBySuite(long stageNameId, String suite);
    List<TestCaseIndex> findByStage(long stageNameId);
    List<TestCaseIndex> findByProject(long projectId, int max);
}
