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

import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;

import static java.util.Arrays.asList;

public class InvalidOrderKeyProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "top";
    private static final String KEY = "key";
    
    private InvalidOrderKeyProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new InvalidOrderKeyProblem(PATH, "message", KEY);
    }

    public void testRecordDoesNotExist()
    {
        problem.solve(recordManager);
    }
    
    public void testNoOrder()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
        
        assertNull(recordManager.select(PATH).getMeta(CollectionType.ORDER_KEY));
    }

    public void testKeyNotInOrder()
    {
        MutableRecord record = new MutableRecordImpl();
        CollectionType.setOrder(record, asList("other"));
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        assertEquals(asList("other"), CollectionType.getDeclaredOrder(recordManager.select(PATH)));
    }

    public void testKeyRefersToSimple()
    {
        MutableRecord record = new MutableRecordImpl();
        CollectionType.setOrder(record, asList(KEY, "other"));
        record.put(KEY, "value");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        assertEquals(asList("other"), CollectionType.getDeclaredOrder(recordManager.select(PATH)));
    }

    public void testKeyRefersToNested()
    {
        MutableRecord record = new MutableRecordImpl();
        CollectionType.setOrder(record, asList(KEY, "other"));
        record.put(KEY, new MutableRecordImpl());
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        assertEquals(asList(KEY, "other"), CollectionType.getDeclaredOrder(recordManager.select(PATH)));
    }
    
    public void testKeyRemoved()
    {
        MutableRecord record = new MutableRecordImpl();
        CollectionType.setOrder(record, asList("other", KEY, "another"));
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        assertEquals(asList("other", "another"), CollectionType.getDeclaredOrder(recordManager.select(PATH)));
    }

    public void testMultipleKeysRemoved()
    {
        MutableRecord record = new MutableRecordImpl();
        CollectionType.setOrder(record, asList(KEY, "other", KEY, "another", KEY));
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        assertEquals(asList("other", "another"), CollectionType.getDeclaredOrder(recordManager.select(PATH)));
    }
}