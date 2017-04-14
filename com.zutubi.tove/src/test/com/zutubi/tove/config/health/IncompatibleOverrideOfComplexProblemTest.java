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

import static com.zutubi.tove.type.record.PathUtils.getPath;

public class IncompatibleOverrideOfComplexProblemTest extends AbstractHealthProblemTestCase
{
    private static final String PARENT_PATH = "parent";
    private static final String PATH = "child";
    private static final String KEY = "key";

    private static final String TYPE1 = "type1";
    private static final String TYPE2 = "type2";

    private IncompatibleOverrideOfComplexProblem problem;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        problem = new IncompatibleOverrideOfComplexProblem(PATH, "message", KEY, PARENT_PATH);
    }

    public void testParentRecordDoesNotExist()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        problem.solve(recordManager);
    }

    public void testParentRecordDoesNotContainKey()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        recordManager.insert(PARENT_PATH, new MutableRecordImpl());
        problem.solve(recordManager);
    }

    public void testParentRecordHasSimpleKey()
    {
        recordManager.insert(PATH, new MutableRecordImpl());
        MutableRecord parent = new MutableRecordImpl();
        parent.put(KEY, "simple");
        recordManager.insert(PARENT_PATH, parent);
        problem.solve(recordManager);
    }
    
    public void testContainingRecordDoesNotExist()
    {
        MutableRecord parent = new MutableRecordImpl();
        parent.put(KEY, new MutableRecordImpl());
        recordManager.insert(PARENT_PATH, parent);
        problem.solve(recordManager);
    }

    public void testCompatibleExists()
    {
        MutableRecord parent = new MutableRecordImpl();
        MutableRecordImpl inherited = new MutableRecordImpl();
        inherited.setSymbolicName(TYPE1);
        parent.put(KEY, inherited);
        recordManager.insert(PARENT_PATH, parent);

        MutableRecord child = new MutableRecordImpl();
        MutableRecord compatible = new MutableRecordImpl();
        compatible.setSymbolicName(TYPE1);
        compatible.put("foo", "bar");
        child.put(KEY, compatible);
        recordManager.insert(PATH, child);
        
        problem.solve(recordManager);
        
        Record compatibleAfter = recordManager.select(getPath(PATH, KEY));
        assertNotNull(compatibleAfter);
        assertEquals("bar", compatibleAfter.get("foo"));
    }

    public void testChildRecordContainsSimpleKey()
    {
        MutableRecord parent = new MutableRecordImpl();
        parent.put(KEY, new MutableRecordImpl());
        recordManager.insert(PARENT_PATH, parent);

        MutableRecord child = new MutableRecordImpl();
        child.put(KEY, "simple");
        recordManager.insert(PATH, child);
        
        problem.solve(recordManager);
        
        assertFalse(recordManager.containsRecord(getPath(PATH, KEY)));
    }
    
    public void testReplaceIncompatibleWithSkeleton()
    {
        MutableRecord parent = new MutableRecordImpl();
        MutableRecord inherited = new MutableRecordImpl();
        inherited.setSymbolicName(TYPE1);
        inherited.put("simple", "value");
        inherited.put("nested", new MutableRecordImpl());
        parent.put(KEY, inherited);
        recordManager.insert(PARENT_PATH, parent);

        MutableRecordImpl incompatible = new MutableRecordImpl();
        incompatible.setSymbolicName(TYPE2);
        recordManager.insert(PATH, incompatible);
        problem.solve(recordManager);
        
        Record skeleton = recordManager.select(getPath(PATH, KEY));
        assertNotNull(skeleton);
        assertNull(skeleton.get("simple"));
        Object nested = skeleton.get("nested");
        assertNotNull(nested);
        assertTrue(nested instanceof Record);
    }
}