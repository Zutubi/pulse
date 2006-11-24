package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.TestCaseIndex;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.Project;

import java.util.List;

/**
 */
public interface TestCaseIndexDao extends EntityDao<TestCaseIndex>
{
    TestCaseIndex findByCase(long stageNameId, String name);
}
