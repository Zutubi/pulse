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

import com.google.common.collect.Sets;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.TemplateRecord;

public class InvalidHiddenKeyProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "top";
    private static final String KEY = "key";
    
    private InvalidHiddenKeyProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new InvalidHiddenKeyProblem(PATH, "message", KEY);
    }

    public void testRecordDoesNotExist()
    {
        problem.solve(recordManager);
    }
    
    public void testNoHiddenKeys()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
        
        assertNull(recordManager.select(PATH).getMeta(TemplateRecord.HIDDEN_KEY));
    }

    public void testKeyNotHidden()
    {
        MutableRecord record = new MutableRecordImpl();
        TemplateRecord.hideItem(record, "other");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);

        assertEquals(Sets.newHashSet("other"), TemplateRecord.getHiddenKeys(recordManager.select(PATH)));
    }

    public void testKeyRemoved()
    {
        MutableRecord record = new MutableRecordImpl();
        TemplateRecord.hideItem(record, KEY);
        TemplateRecord.hideItem(record, "other");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);

        assertEquals(Sets.newHashSet("other"), TemplateRecord.getHiddenKeys(recordManager.select(PATH)));
    }

    public void testMultipleKeysRemoved()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta(TemplateRecord.HIDDEN_KEY, "other," + KEY + ",another," + KEY);
        recordManager.insert(PATH, record);
        problem.solve(recordManager);

        assertEquals(Sets.newHashSet("other", "another"), TemplateRecord.getHiddenKeys(recordManager.select(PATH)));
    }
}