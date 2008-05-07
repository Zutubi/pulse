package com.zutubi.prototype.type.record;

import com.zutubi.prototype.type.ComplexType;
import com.zutubi.prototype.type.Instantiator;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.util.GraphFunction;

import java.util.Set;

/**
 */
public class TemplateRecordTest extends PulseTestCase
{
    public void testMetaKeySet()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta("foo", "bar");
        record.putMeta("baz", "quux");
        record.put("a", "a");

        TemplateRecord template = new TemplateRecord("jim", null, null, record);
        Set<String> metaKeySet = template.metaKeySet();
        assertTrue(metaKeySet.contains("foo"));
        assertTrue(metaKeySet.contains("baz"));
        assertFalse(metaKeySet.contains("a"));
    }

    public void testMetaKeySetInherited()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta("inparent", "x");
        record.put("a", "a");
        TemplateRecord parent = new TemplateRecord("jim", null, null, record);

        record = new MutableRecordImpl();
        record.putMeta("inchild", "x");
        record.put("b", "b");
        TemplateRecord child = new TemplateRecord("bob", parent, null, record);

        Set<String> metaKeySet = child.metaKeySet();
        assertTrue(metaKeySet.contains("inparent"));
        assertTrue(metaKeySet.contains("inchild"));
        assertFalse(metaKeySet.contains("a"));
        assertFalse(metaKeySet.contains("b"));
    }

    public void testMetaKeySetInheritedNoInherit()
    {
        MutableRecord record = new MutableRecordImpl();
        record.putMeta("inparent", "x");
        record.putMeta(TemplateRecord.PARENT_KEY, "x");
        TemplateRecord parent = new TemplateRecord("jim", null, null, record);

        record = new MutableRecordImpl();
        record.putMeta("inchild", "x");
        TemplateRecord child = new TemplateRecord("bob", parent, null, record);

        Set<String> metaKeySet = child.metaKeySet();
        assertTrue(metaKeySet.contains("inparent"));
        assertTrue(metaKeySet.contains("inchild"));
        assertFalse(metaKeySet.contains(TemplateRecord.PARENT_KEY));
        assertTrue(parent.metaKeySet().contains(TemplateRecord.PARENT_KEY));
    }

    public void testHideItem()
    {
        MutableRecord record = newRecord(1);
        record.put("foo", newRecord(2));

        TemplateRecord.hideItem(record, 2);
        Set<Long> hidden = TemplateRecord.getHiddenHandles(record);
        assertEquals(1, hidden.size());
        assertTrue(hidden.contains(2L));
    }

    public void testRestoreItem()
    {
        MutableRecord record = newRecord(1);
        record.put("foo", newRecord(2));

        TemplateRecord.hideItem(record, 2);
        assertTrue(TemplateRecord.restoreItem(record, 2));
        Set<Long> hidden = TemplateRecord.getHiddenHandles(record);
        assertEquals(0, hidden.size());
    }

    public void testHideMultipleItems()
    {
        MutableRecord record = newRecord(1);
        record.put("foo1", newRecord(2));
        record.put("foo2", newRecord(3));

        TemplateRecord.hideItem(record, 2);
        TemplateRecord.hideItem(record, 3);
        Set<Long> hidden = TemplateRecord.getHiddenHandles(record);
        assertEquals(2, hidden.size());
        assertTrue(hidden.contains(2L));
        assertTrue(hidden.contains(3L));
    }

    public void testRestoreMultipleItems()
    {
        MutableRecord record = newRecord(1);
        String key1 = "foo1";
        String key2 = "foo2";
        record.put(key1, newRecord(2));
        record.put(key1, newRecord(3));

        TemplateRecord.hideItem(record, 2);
        TemplateRecord.hideItem(record, 3);

        assertTrue(TemplateRecord.restoreItem(record, 2));
        Set<Long> hidden = TemplateRecord.getHiddenHandles(record);
        assertEquals(1, hidden.size());
        assertTrue(hidden.contains(3L));

        assertTrue(TemplateRecord.restoreItem(record, 3));
        hidden = TemplateRecord.getHiddenHandles(record);
        assertEquals(0, hidden.size());
    }

    public void testRestoreItemNotHidden()
    {
        MutableRecord record = newRecord(1);
        record.put("foo", newRecord(2));

        assertFalse(TemplateRecord.restoreItem(record, 2));
        Set<Long> hidden = TemplateRecord.getHiddenHandles(record);
        assertEquals(0, hidden.size());
    }

    public void testContainsKey()
    {
        MutableRecord record = new MutableRecordImpl();
        record.put("foo", "bar");
        TemplateRecord template = new TemplateRecord("test", null, null, record);
        assertTrue(template.containsKey("foo"));
        assertFalse(template.containsKey("bar"));
    }

    public void testContainsKeyInherited()
    {
        MutableRecord parentRecord = new MutableRecordImpl();
        parentRecord.put("foo", "bar");
        TemplateRecord parent = new TemplateRecord("parent", null, null, parentRecord);
        MutableRecordImpl childRecord = new MutableRecordImpl();
        childRecord.put("quux", "quuux");
        TemplateRecord child = new TemplateRecord("child", parent, null, childRecord);
        assertTrue(child.containsKey("quux"));
        assertTrue(child.containsKey("foo"));
        assertFalse(child.containsKey("bar"));
        assertFalse(parent.containsKey("quux"));
    }

    public void testContainsKeyHidden()
    {
        MutableRecord parentRecord = newRecord(1);
        parentRecord.put("foo", newRecord(2));
        TemplateRecord parent = new TemplateRecord("parent", null, new MockType(), parentRecord);
        MutableRecordImpl childRecord = new MutableRecordImpl();
        TemplateRecord child = new TemplateRecord("child", parent, null, childRecord);
        assertTrue(child.containsKey("foo"));
        TemplateRecord.hideItem(childRecord, 2);
        assertFalse(child.containsKey("foo"));
    }

    private MutableRecord newRecord(long handle)
    {
        MutableRecord record = new MutableRecordImpl();
        record.setHandle(handle);
        return record;
    }

    private static class MockType implements ComplexType
    {
        public Type getTargetType()
        {
            throw new RuntimeException("Not implemented");
        }

        public Type getActualType(Object value)
        {
            throw new RuntimeException("Not implemented");           
        }

        public Class getClazz()
        {
            throw new RuntimeException("Not implemented");
        }

        public Object instantiate(Object data, Instantiator instantiator) throws TypeException
        {
            throw new RuntimeException("Not implemented");
        }

        public void initialise(Object instance, Object data, Instantiator instantiator)
        {
            throw new RuntimeException("Not implemented");
        }

        public Object unstantiate(Object instance) throws TypeException
        {
            throw new RuntimeException("Not implemented");
        }

        public Object toXmlRpc(Object data) throws TypeException
        {
            throw new RuntimeException("Not implemented");
        }

        public Object fromXmlRpc(Object data) throws TypeException
        {
            throw new RuntimeException("Not implemented");
        }

        public String getSymbolicName()
        {
            throw new RuntimeException("Not implemented");
        }

        public String getSavePath(String path, Record record)
        {
            throw new RuntimeException("Not implemented");
        }

        public String getInsertionPath(String path, Record record)
        {
            throw new RuntimeException("Not implemented");
        }

        public MutableRecord createNewRecord(boolean applyDefaults)
        {
            throw new RuntimeException("Not implemented");
        }

        public boolean isTemplated()
        {
            throw new RuntimeException("Not implemented");
        }

        public Type getDeclaredPropertyType(String propertyName)
        {
            throw new RuntimeException("Not implemented");
        }

        public Type getActualPropertyType(String propertyName, Object propertyValue)
        {
             return new MockType();
        }

        public boolean isValid(Object instance)
        {
            throw new RuntimeException("Not implemented");
        }

        public void forEachComplex(Object instance, GraphFunction<Object> f) throws TypeException
        {
            throw new RuntimeException("Not implemented");
        }
    }
}
