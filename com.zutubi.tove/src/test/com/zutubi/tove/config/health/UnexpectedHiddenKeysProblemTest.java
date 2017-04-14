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

package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;

public class UnexpectedHiddenKeysProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "top";
    
    private UnexpectedHiddenKeysProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new UnexpectedHiddenKeysProblem(PATH, "message");
    }

    public void testRecordDoesNotExist()
    {
        problem.solve(recordManager);
    }
    
    public void testNoHiddenKeys()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
    }

    public void testRemovesHiddenKeys()
    {
        MutableRecord record = new MutableRecordImpl();
        TemplateRecord.hideItem(record, "any");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertEquals(0, TemplateRecord.getHiddenKeys(after).size());
    }
}