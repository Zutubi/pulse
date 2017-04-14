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
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

import java.util.Arrays;

public class NonCanonicalReferenceProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PATH = "top";
    private static final String KEY = "key";
    private static final String HANDLE = "234";
    private static final String CANONICAL_HANDLE = "567";
    
    private NonCanonicalReferenceProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new NonCanonicalReferenceProblem(PATH, "message", KEY, HANDLE, CANONICAL_HANDLE);
    }

    public void testRecordDoesNotExist()
    {
        problem.solve(recordManager);
    }
    
    public void testKeyDoesNotExist()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
    }

    public void testKeyIsNotSimple()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        String nestedPath = PathUtils.getPath(PATH, KEY);
        recordManager.insert(nestedPath, new MutableRecordImpl());
        problem.solve(recordManager);
        
        assertTrue(recordManager.containsRecord(nestedPath));
    }

    public void testSingleReferenceNotEquals()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, "1");
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertEquals("1", after.get(KEY));
    }

    public void testFixesSingleReference()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, HANDLE);
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertEquals(CANONICAL_HANDLE, after.get(KEY));
    }

    public void testMultipleReferencesNoneMatch()
    {
        final String[] REFERENCES = {"1", "2"};
        
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, REFERENCES);
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertTrue(Arrays.equals(REFERENCES, (String[]) after.get(KEY)));
    }

    public void testFixesReferenceFromMultiple()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, new String[]{"1", HANDLE});
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertTrue(Arrays.equals(new String[]{"1", CANONICAL_HANDLE}, (String[]) after.get(KEY)));
    }

    public void testFixesMultipleReferencesFromMultiple()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put(KEY, new String[]{"1", HANDLE, "2", HANDLE});
        recordManager.insert(PATH, record);
        problem.solve(recordManager);
        
        Record after = recordManager.select(PATH);
        assertTrue(Arrays.equals(new String[]{"1", CANONICAL_HANDLE, "2", CANONICAL_HANDLE}, (String[]) after.get(KEY)));
    }
}