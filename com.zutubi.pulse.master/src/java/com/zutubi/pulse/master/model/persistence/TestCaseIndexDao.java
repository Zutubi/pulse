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
