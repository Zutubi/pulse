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

package com.zutubi.tove.config;

import com.google.common.collect.Sets;
import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.Internal;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.TemplatedMapType;
import com.zutubi.tove.type.record.MutableRecord;

import java.util.*;

public class TypeListenerTest extends AbstractConfigurationSystemTestCase
{
    private CompositeType typeA;
    private CompositeType typeB;
    private long globalHandle;
    private long childtHandle;

    protected void setUp() throws Exception
    {
        super.setUp();

        typeA = typeRegistry.register(A.class);
        typeB = typeRegistry.getType(B.class);

        MapType mapA = new MapType(typeA, typeRegistry);
        configurationPersistenceManager.register("sample", mapA);

        MapType templatedMap = new TemplatedMapType(typeA, typeRegistry);
        configurationPersistenceManager.register("template", templatedMap);

        insertA("globalt", -1, true);

        globalHandle = configurationTemplateManager.getRecord("template/globalt").getHandle();
        insertA("childt", globalHandle, true);
        insertA("child", globalHandle, false);

        childtHandle = configurationTemplateManager.getRecord("template/childt").getHandle();
        insertA("grandchildt", childtHandle, true);
        insertA("grandchild", childtHandle, false);
    }

    private void insertA(String name, long parentHandle, boolean template)
    {
        MutableRecord record = createA(name);
        if (template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }
        if (parentHandle >= 0)
        {
            configurationTemplateManager.setParentTemplate(record, parentHandle);
        }
        configurationTemplateManager.insertRecord("template", record);
    }

    private MutableRecord createA(String name)
    {
        MutableRecord record = typeA.createNewRecord(false);
        record.put("name", name);
        return record;
    }

    private MutableRecord createEditedA(String name)
    {
        MutableRecord record = createA(name);
        record.put("thing", "edited");
        return record;
    }

    private void insertB(String name, String path)
    {
        MutableRecord record = createB(name);
        configurationTemplateManager.insertRecord(path, record);
    }

    private MutableRecord createB(String name)
    {
        MutableRecord record = typeB.createNewRecord(false);
        record.put("name", name);
        return record;
    }

    private MutableRecord createEditedB(String name)
    {
        MutableRecord record = typeB.createNewRecord(false);
        record.put("name", name);
        record.put("thing", "edited");
        return record;
    }

    public void testInstanceInserted()
    {
        RecordingTypeListener<A> listener = register(A.class);
        configurationTemplateManager.insertInstance("sample", new A("a"));
        listener.assertInsert("sample/a");
        listener.assertPostInsert("sample/a");
        listener.assertDone();
    }

    public void testInstanceChanged()
    {
        String path = configurationTemplateManager.insertInstance("sample", new A("a"));
        RecordingTypeListener<A> listener = register(A.class);

        A clone = configurationTemplateManager.getCloneOfInstance(path, A.class);
        clone.setThing("edited");
        configurationTemplateManager.save(clone);
        listener.assertSave("sample/a", false);
        listener.assertPostSave("sample/a", false);
        listener.assertDone();
    }

    public void testInstanceDeleted()
    {
        configurationTemplateManager.insertInstance("sample", new A("a"));
        RecordingTypeListener<A> listener = register(A.class);
        configurationTemplateManager.delete("sample/a");
        listener.assertDelete("sample/a");
        listener.assertPostDelete("sample/a");
        listener.assertDone();
    }

    public void testNestedInsertTriggersSave()
    {
        configurationTemplateManager.insertInstance("sample", new A("a"));
        RecordingTypeListener<A> listener = register(A.class);
        configurationTemplateManager.insertInstance("sample/a/b", new B("b"));
        listener.assertSave("sample/a", true);
        listener.assertPostSave("sample/a", true);
        listener.assertDone();
    }

    public void testNestedSaveTriggersSave()
    {
        configurationTemplateManager.insertInstance("sample", new A("a"));
        String bPath = configurationTemplateManager.insertInstance("sample/a/b", new B("b"));
        RecordingTypeListener<A> listener = register(A.class);
        B clone = configurationTemplateManager.getCloneOfInstance(bPath, B.class);
        clone.setName("edited");
        configurationTemplateManager.save(clone);
        listener.assertSave("sample/a", true);
        listener.assertPostSave("sample/a", true);
        listener.assertDone();
    }

    public void testNestedDeleteTriggersSave()
    {
        configurationTemplateManager.insertInstance("sample", new A("a"));
        configurationTemplateManager.insertInstance("sample/a/b", new B("b"));
        RecordingTypeListener<A> listener = register(A.class);
        configurationTemplateManager.delete("sample/a/b");
        listener.assertSave("sample/a", true);
        listener.assertPostSave("sample/a", true);
        listener.assertDone();
    }

    public void testInsertTemplateA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, true);
        la.assertDone();
        lb.assertDone();
    }

    public void testInsertTemplateAInheritingB()
    {
        insertB("nesty", "template/globalt/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, true);
        la.assertDone();
        lb.assertDone();
    }

    public void testInsertConcreteA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, false);
        la.assertInsert("template/test");
        la.assertPostInsert("template/test");
        la.assertDone();
        lb.assertDone();
    }

    public void testInsertConcreteAInheritingB()
    {
        insertB("nesty", "template/globalt/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, false);
        la.assertInsert("template/test");
        la.assertPostInsert("template/test");
        la.assertDone();
        lb.assertInsert("template/test/b");
        lb.assertPostInsert("template/test/b");
        lb.assertDone();
    }

    public void testInsertConcreteAInheritingBFromCollection()
    {
        insertB("nesty", "template/globalt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertA("test", globalHandle, false);
        la.assertInsert("template/test");
        la.assertPostInsert("template/test");
        la.assertDone();
        lb.assertInsert("template/test/bees/nesty");
        lb.assertPostInsert("template/test/bees/nesty");
        lb.assertDone();
    }

    public void testInsertBIntoLeaf()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertB("test", "template/child/b");
        la.assertSave("template/child", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertInsert("template/child/b");
        lb.assertPostInsert("template/child/b");
        lb.assertDone();
    }

    public void testInsertBIntoLeafCollection()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertB("test", "template/child/bees");
        la.assertSave("template/child", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertInsert("template/child/bees/test");
        lb.assertPostInsert("template/child/bees/test");
        lb.assertDone();
    }

    public void testInsertBIntoIntermediate()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertB("test", "template/childt/b");
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertInsert("template/grandchild/b");
        lb.assertPostInsert("template/grandchild/b");
        lb.assertDone();
    }

    public void testInsertBIntoIntermediateCollection()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertB("test", "template/childt/bees");
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertInsert("template/grandchild/bees/test");
        lb.assertPostInsert("template/grandchild/bees/test");
        lb.assertDone();
    }
    
    public void testInsertBIntoRoot()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertB("test", "template/globalt/b");
        la.assertSave("template/grandchild", true);
        la.assertSave("template/child", true);
        la.assertPostSave("template/grandchild", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertInsert("template/grandchild/b");
        lb.assertInsert("template/child/b");
        lb.assertPostInsert("template/grandchild/b");
        lb.assertPostInsert("template/child/b");
        lb.assertDone();
    }

    public void testInsertBIntoRootCollection()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        insertB("test", "template/globalt/bees");
        la.assertSave("template/grandchild", true);
        la.assertSave("template/child", true);
        la.assertPostSave("template/grandchild", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertInsert("template/grandchild/bees/test");
        lb.assertInsert("template/child/bees/test");
        lb.assertPostInsert("template/grandchild/bees/test");
        lb.assertPostInsert("template/child/bees/test");
        lb.assertDone();
    }

    public void testSaveConcreteA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child", createEditedA("child"));
        la.assertSave("template/child", false);
        la.assertPostSave("template/child", false);
        la.assertDone();
        lb.assertDone();
    }

    public void testRenameConcreteA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child", createA("newname"));
        la.assertSave("template/newname", false);
        la.assertPostSave("template/newname", false);
        la.assertDone();
        lb.assertDone();
    }

    public void testSaveLeafTemplateA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/grandchildt", createA("grandchildt"));
        la.assertDone();
        lb.assertDone();
    }

    public void testRenameLeafTemplateA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/grandchildt", createA("newname"));
        la.assertDone();
        lb.assertDone();
    }

    public void testSaveIntermediateTemplateA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt", createEditedA("childt"));
        la.assertSave("template/grandchild", false);
        la.assertPostSave("template/grandchild", false);
        la.assertDone();
        lb.assertDone();
    }

    public void testRenameIntermediateTemplateA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt", createA("newname"));
        la.assertSave("template/grandchild", false);
        la.assertPostSave("template/grandchild", false);
        la.assertDone();
        lb.assertDone();
    }

    public void testSaveRootTemplateA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt", createEditedA("globalt"));
        la.assertSave("template/grandchild", false);
        la.assertPostSave("template/grandchild", false);
        la.assertSave("template/child", false);
        la.assertPostSave("template/child", false);
        la.assertDone();
        lb.assertDone();
    }

    public void testRenameRootTemplateA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt", createA("newname"));
        la.assertSave("template/grandchild", false);
        la.assertPostSave("template/grandchild", false);
        la.assertSave("template/child", false);
        la.assertPostSave("template/child", false);
        la.assertDone();
        lb.assertDone();
    }

    public void testSaveBInLeaf()
    {
        insertB("test", "template/child/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child/b", createEditedB("test"));
        la.assertSave("template/child", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertSave("template/child/b", false);
        lb.assertPostSave("template/child/b", false);
        lb.assertDone();
    }

    public void testSaveBInLeafCollection()
    {
        insertB("test", "template/child/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child/bees/test", createEditedB("test"));
        la.assertSave("template/child", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertSave("template/child/bees/test", false);
        lb.assertPostSave("template/child/bees/test", false);
        lb.assertDone();
    }

    public void testRenameBInLeafCollection()
    {
        insertB("test", "template/child/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/child/bees/test", createEditedB("newname"));
        la.assertSave("template/child", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertSave("template/child/bees/newname", false);
        lb.assertPostSave("template/child/bees/newname", false);
        lb.assertDone();
    }

    public void testSaveBInIntermediate()
    {
        insertB("test", "template/childt/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/b", createEditedB("test"));
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertSave("template/grandchild/b", false);
        lb.assertPostSave("template/grandchild/b", false);
        lb.assertDone();
    }

    public void testSaveBInIntermediateCollection()
    {
        insertB("test", "template/childt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/bees/test", createEditedB("test"));
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertSave("template/grandchild/bees/test", false);
        lb.assertPostSave("template/grandchild/bees/test", false);
        lb.assertDone();
    }

    public void testRenameBInIntermediateCollection()
    {
        insertB("test", "template/childt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/bees/test", createB("newname"));
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertSave("template/grandchild/bees/newname", false);
        lb.assertPostSave("template/grandchild/bees/newname", false);
        lb.assertDone();
    }

    public void testSaveBInRoot()
    {
        insertB("test", "template/globalt/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt/b", createEditedB("test"));
        la.assertSave("template/grandchild",  true);
        la.assertSave("template/child", true);
        la.assertPostSave("template/grandchild",  true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertSave("template/grandchild/b", false);
        lb.assertPostSave("template/grandchild/b", false);
        lb.assertSave("template/child/b", false);
        lb.assertPostSave("template/child/b", false);
        lb.assertDone();
    }

    public void testSaveBInRootCollection()
    {
        insertB("test", "template/globalt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt/bees/test", createEditedB("test"));
        la.assertSave("template/grandchild", true);
        la.assertSave("template/child", true);
        la.assertPostSave("template/grandchild", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertSave("template/grandchild/bees/test", false);
        lb.assertPostSave("template/grandchild/bees/test", false);
        lb.assertSave("template/child/bees/test", false);
        lb.assertPostSave("template/child/bees/test", false);
        lb.assertDone();
    }

    public void testRenameBInRootCollection()
    {
        insertB("test", "template/globalt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/globalt/bees/test", createEditedB("newname"));
        la.assertSave("template/grandchild", true);
        la.assertSave("template/child", true);
        la.assertPostSave("template/grandchild", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertSave("template/grandchild/bees/newname", false);
        lb.assertPostSave("template/grandchild/bees/newname", false);
        lb.assertSave("template/child/bees/newname", false);
        lb.assertPostSave("template/child/bees/newname", false);
        lb.assertDone();
    }

    public void testDeleteConcreteA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/child");
        la.assertDelete("template/child");
        la.assertPostDelete("template/child");
        la.assertDone();
        lb.assertDone();
    }

    public void testDeleteConcreteAIncludingB()
    {
        insertB("test", "template/child/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/child");
        la.assertDelete("template/child");
        la.assertPostDelete("template/child");
        la.assertDone();
        lb.assertDelete("template/child/b");
        lb.assertPostDelete("template/child/b");
        lb.assertDone();
    }

    public void testDeleteConcreteAIncludingBInCollection()
    {
        insertB("test", "template/child/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/child");
        la.assertDelete("template/child");
        la.assertPostDelete("template/child");
        la.assertDone();
        lb.assertDelete("template/child/bees/test");
        lb.assertPostDelete("template/child/bees/test");
        lb.assertDone();
    }

    public void testDeleteTemplateA()
    {
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchildt");
        la.assertDone();
        lb.assertDone();
    }

    public void testDeleteTemplateAIncludingB()
    {
        insertB("test", "template/grandchildt/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchildt");
        la.assertDone();
        lb.assertDone();
    }

    public void testDeleteTemplateAIncludingBInCollection()
    {
        insertB("test", "template/grandchildt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchildt");
        la.assertDone();
        lb.assertDone();
    }

    public void testDeleteBFromLeaf()
    {
        insertB("test", "template/grandchild/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchild/b");
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertDelete("template/grandchild/b");
        lb.assertPostDelete("template/grandchild/b");
        lb.assertDone();
    }

    public void testDeleteBFromLeafCollection()
    {
        insertB("test", "template/grandchild/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/grandchild/bees/test");
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertDelete("template/grandchild/bees/test");
        lb.assertPostDelete("template/grandchild/bees/test");
        lb.assertDone();
    }

    public void testDeleteBFromIntermediate()
    {
        insertB("test", "template/childt/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/childt/b");
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertDelete("template/grandchild/b");
        lb.assertPostDelete("template/grandchild/b");
        lb.assertDone();
    }

    public void testDeleteBFromIntermediateCollection()
    {
        insertB("test", "template/childt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/childt/bees/test");
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertDelete("template/grandchild/bees/test");
        lb.assertPostDelete("template/grandchild/bees/test");
        lb.assertDone();
    }

    public void testDeleteBFromRoot()
    {
        insertB("test", "template/globalt/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/globalt/b");
        la.assertSave("template/grandchild", true);
        la.assertSave("template/child", true);
        la.assertPostSave("template/grandchild", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertDelete("template/grandchild/b");
        lb.assertDelete("template/child/b");
        lb.assertPostDelete("template/grandchild/b");
        lb.assertPostDelete("template/child/b");
        lb.assertDone();
    }

    public void testDeleteBFromRootCollection()
    {
        insertB("test", "template/globalt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.delete("template/globalt/bees/test");
        la.assertSave("template/grandchild", true);
        la.assertSave("template/child", true);
        la.assertPostSave("template/grandchild", true);
        la.assertPostSave("template/child", true);
        la.assertDone();
        lb.assertDelete("template/grandchild/bees/test");
        lb.assertDelete("template/child/bees/test");
        lb.assertPostDelete("template/grandchild/bees/test");
        lb.assertPostDelete("template/child/bees/test");
        lb.assertDone();
    }

    public void testSaveBInLeafOverridingIntermediate()
    {
        insertB("test", "template/childt/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/grandchild/b", createEditedB("test"));
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertSave("template/grandchild/b", false);
        lb.assertPostSave("template/grandchild/b", false);
        lb.assertDone();
    }

    public void testSaveBInLeafCollectionOverridingIntermediate()
    {
        insertB("test", "template/childt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/grandchild/bees/test", createEditedB("test"));
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertSave("template/grandchild/bees/test", false);
        lb.assertPostSave("template/grandchild/bees/test", false);
        lb.assertDone();
    }

    public void testSaveBInIntermediateOverridingRoot()
    {
        insertB("test", "template/globalt/b");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/b", createEditedB("test"));
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertSave("template/grandchild/b", false);
        lb.assertPostSave("template/grandchild/b", false);
        lb.assertDone();
    }

    public void testSaveBInIntermediateCollectionOverridingRoot()
    {
        insertB("test", "template/globalt/bees");
        RecordingTypeListener<A> la = register(A.class);
        RecordingTypeListener<B> lb = register(B.class);
        configurationTemplateManager.saveRecord("template/childt/bees/test", createEditedB("test"));
        la.assertSave("template/grandchild", true);
        la.assertPostSave("template/grandchild", true);
        la.assertDone();
        lb.assertSave("template/grandchild/bees/test", false);
        lb.assertPostSave("template/grandchild/bees/test", false);
        lb.assertDone();
    }

    public void testAssignId()
    {
        IDAssigningListener<A> listener = new IDAssigningListener<A>(A.class);
        listener.register(configurationProvider, true);

        configurationTemplateManager.insertRecord("sample", createA("a"));
        assertId("sample/a", 1);
    }

    public void testAssignedIdSurvivesRefresh()
    {
        IDAssigningListener<A> listener = new IDAssigningListener<A>(A.class);
        listener.register(configurationProvider, true);

        configurationTemplateManager.insertRecord("sample", createA("a"));
        configurationTemplateManager.saveRecord("sample/a", createA("a"));
        assertId("sample/a", 1);
    }

    public void testNewConcreteAssignedId()
    {
        IDAssigningListener<A> listener = new IDAssigningListener<A>(A.class);
        listener.register(configurationProvider, true);

        insertA("new", childtHandle, false);
        assertId("template/new", 1);
    }

    public void testAllConcreteGetAssignedIds()
    {
        IDAssigningListener<B> listener = new IDAssigningListener<B>(B.class);
        listener.register(configurationProvider, true);

        insertB("new", "template/globalt/b");

        Set<Long> ids = new HashSet<Long>();
        ids.add(configurationProvider.get("template/child/b", EyeDee.class).getId());
        ids.add(configurationProvider.get("template/grandchild/b", EyeDee.class).getId());
        
        assertEquals(Sets.newHashSet(1l, 2l), ids);
    }

    public void testChangeInSave()
    {
        A a = new A("a");
        a.setB(new B("b"));
        String path = configurationTemplateManager.insertInstance("sample", a);
        RecordingTypeListener<A> listener = register(A.class);
        TypeListener<A> changingListener = new TypeAdapter<A>(A.class)
        {
            public void save(A instance, boolean nested)
            {
                if (!nested)
                {
                    B b = configurationTemplateManager.deepClone(instance.b);
                    b.setThing("something");
                    configurationProvider.save(b);
                }
            }
        };
        changingListener.register(configurationProvider, true);
        A clone = configurationTemplateManager.getCloneOfInstance(path, A.class);
        clone.setThing("edited");
        configurationTemplateManager.save(clone);
        listener.assertSave("sample/a", false);
        listener.assertSave("sample/a", true);
        listener.assertPostSave("sample/a", false);
        listener.assertPostSave("sample/a", true);
        listener.assertDone();
    }

    private void assertId(String path, int id)
    {
        EyeDee c = (EyeDee) configurationTemplateManager.getInstance(path);
        assertNotNull(c);
        assertEquals(id, c.getId());
    }

    private <T extends Configuration> RecordingTypeListener<T> register(Class<T> clazz)
    {
        RecordingTypeListener<T> listener = new RecordingTypeListener<T>(clazz);
        listener.register(configurationProvider, true);
        return listener;
    }

    private static class RecordingTypeListener<X extends Configuration> extends TypeListener<X>
    {
        private static class Event
        {
            public enum Type
            {
                INSERT,
                DELETE,
                SAVE,
                POST_INSERT,
                POST_DELETE,
                POST_SAVE,
            }

            public Type type;
            public String path;
            public boolean nested;

            public Event(Type type, String path)
            {
                this(type, path, false);
            }

            public Event(Type type, String path, boolean nested)
            {
                this.type = type;
                this.path = path;
                this.nested = nested;
            }

            public boolean equals(Object o)
            {
                if (this == o)
                {
                    return true;
                }
                if (o == null || getClass() != o.getClass())
                {
                    return false;
                }

                Event event = (Event) o;

                if (nested != event.nested)
                {
                    return false;
                }
                if (!path.equals(event.path))
                {
                    return false;
                }
                return type == event.type;
            }

            public int hashCode()
            {
                int result;
                result = type.hashCode();
                result = 31 * result + path.hashCode();
                result = 31 * result + (nested ? 1 : 0);
                return result;
            }
        }

        private List<Event> got = new LinkedList<Event>();

        public RecordingTypeListener(Class<X> configurationClass)
        {
            super(configurationClass);
        }

        public void insert(X instance)
        {
            got.add(new Event(Event.Type.INSERT, instance.getConfigurationPath()));
        }

        public void delete(X instance)
        {
            got.add(new Event(Event.Type.DELETE, instance.getConfigurationPath()));
        }

        public void save(X instance, boolean nested)
        {
            got.add(new Event(Event.Type.SAVE, instance.getConfigurationPath()));
        }

        public void postInsert(X instance)
        {
            got.add(new Event(Event.Type.POST_INSERT, instance.getConfigurationPath()));
        }

        public void postDelete(X instance)
        {
            got.add(new Event(Event.Type.POST_DELETE, instance.getConfigurationPath()));
        }

        public void postSave(X instance, boolean nested)
        {
            got.add(new Event(Event.Type.POST_SAVE, instance.getConfigurationPath()));
        }

        private void assertEvent(String path, Event.Type type)
        {
            assertEvent(path, type, false);
        }

        private void assertEvent(String path, Event.Type type, boolean nested)
        {
            assertTrue(got.remove(new Event(type, path, nested)));
        }

        public void assertInsert(String path)
        {
            assertEvent(path, Event.Type.INSERT);
        }

        public void assertDelete(String path)
        {
            assertEvent(path, Event.Type.DELETE);
        }

        public void assertSave(String path, boolean nested)
        {
            assertEvent(path, Event.Type.SAVE);
        }

        public void assertPostInsert(String path)
        {
            assertEvent(path, Event.Type.POST_INSERT);
        }

        public void assertPostDelete(String path)
        {
            assertEvent(path, Event.Type.POST_DELETE);
        }

        public void assertPostSave(String path, boolean nested)
        {
            assertEvent(path, Event.Type.POST_SAVE);
        }

        public void assertDone()
        {
            assertEquals(0, got.size());
        }
    }

    private static class IDAssigningListener<X extends EyeDee> extends RecordingTypeListener<X>
    {
        private long nextId = 1;

        public IDAssigningListener(Class<X> configurationClass)
        {
            super(configurationClass);
        }

        public void postInsert(EyeDee instance)
        {
            instance.setId(nextId++);
            if(instance instanceof A)
            {
                A a = (A) instance;
                a.setName("edited:" + a.getName());
            }
        }
    }

    public static abstract class EyeDee extends AbstractConfiguration
    {
        @Internal
        private long id;
        public long getId(){return id;}
        public void setId(long id){this.id = id;}
    }

    @SymbolicName("a")
    public static class A extends EyeDee
    {
        @ID
        private String name;
        private String thing;
        private B b;
        private Map<String, B> bees;
        public A(){}
        public A(String a){this.name = a;}
        public String getName(){return name;}
        public void setName(String name){this.name = name;}
        public String getThing(){return thing;}
        public void setThing(String thing){this.thing = thing;}
        public B getB(){return b;}
        public void setB(B b){this.b = b;}
        public Map<String, B> getBees(){return bees;}
        public void setBees(Map<String, B> bees){this.bees = bees;}
    }

    @SymbolicName("b")
    public static class B extends EyeDee
    {
        @ID
        private String name;
        private String thing;
        public B(){}
        public B(String str){this.name = str;}
        public String getName(){return name;}
        public void setName(String name){this.name = name;}
        public String getThing(){return thing;}
        public void setThing(String thing){this.thing = thing;}
    }
}

