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

package com.zutubi.pulse.core.engine.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import static com.zutubi.pulse.core.engine.api.ResultState.*;

public class ResultStateTest extends PulseTestCase
{
    public void testGetWorseState()
    {
        assertEquals(SKIPPED, getWorseState(null, SKIPPED));
        assertEquals(SUCCESS, getWorseState(SUCCESS, SKIPPED));
        assertEquals(WARNINGS, getWorseState(WARNINGS, SUCCESS));
        assertEquals(FAILURE, getWorseState(FAILURE, SUCCESS));
        assertEquals(ERROR, getWorseState(FAILURE, ERROR));
        assertEquals(TERMINATED, getWorseState(TERMINATED, ERROR));
    }

    public void testGetAggregateOrders()
    {
        assertEquals(FAILURE, getAggregate(FAILURE, SUCCESS, new ResultState[0]));
        assertEquals(SUCCESS, getAggregate(FAILURE, SUCCESS, new ResultState[]{SUCCESS}));
        assertEquals(FAILURE, getAggregate(FAILURE, SUCCESS, new ResultState[]{SUCCESS, FAILURE}));
    }

    public void testGetAggregateParams()
    {
        ResultState[] order = new ResultState[]{FAILURE, SUCCESS};

        assertEquals(SUCCESS, getAggregate(FAILURE, SUCCESS, order));
        assertEquals(SUCCESS, getAggregate(SUCCESS, FAILURE, order));

        assertEquals(FAILURE, getAggregate(SKIPPED, FAILURE, order));
        assertEquals(FAILURE, getAggregate(FAILURE, FAILURE, order));
        assertEquals(FAILURE, getAggregate(null, FAILURE, order));
        assertEquals(null, getAggregate(null, null, order));
    }

    public void testWorseStateOrderingContainsAllCompletedStates()
    {
        for (ResultState completed : ResultState.getCompletedStates())
        {
            assertNotNull(getWorseState(null, completed));
        }
    }
}
