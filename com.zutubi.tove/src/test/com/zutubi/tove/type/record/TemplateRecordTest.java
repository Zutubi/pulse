package com.zutubi.tove.type.record;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Set;

public class TemplateRecordTest extends ZutubiTestCase
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
        MutableRecord record = new MutableRecordImpl();
        String key = "foo";
        record.put(key, "bar");

        TemplateRecord.hideItem(record, key);
        Set<String> hidden = TemplateRecord.getHiddenKeys(record);
        assertEquals(1, hidden.size());
        assertTrue(hidden.contains(key));
    }

    public void testRestoreItem()
    {
        MutableRecord record = new MutableRecordImpl();
        String key = "foo";
        record.put(key, "bar");

        TemplateRecord.hideItem(record, key);
        assertTrue(TemplateRecord.restoreItem(record, key));
        Set<String> hidden = TemplateRecord.getHiddenKeys(record);
        assertEquals(0, hidden.size());
    }

    public void testHideMultipleItems()
    {
        MutableRecord record = new MutableRecordImpl();
        String key1 = "foo1";
        String key2 = "foo2";
        record.put(key1, "bar");
        record.put(key2, "bar");

        TemplateRecord.hideItem(record, key1);
        TemplateRecord.hideItem(record, key2);
        Set<String> hidden = TemplateRecord.getHiddenKeys(record);
        assertEquals(2, hidden.size());
        assertTrue(hidden.contains(key1));
        assertTrue(hidden.contains(key2));
    }

    public void testRestoreMultipleItems()
    {
        MutableRecord record = new MutableRecordImpl();
        String key1 = "foo1";
        String key2 = "foo2";
        record.put(key1, "bar");
        record.put(key2, "bar");

        TemplateRecord.hideItem(record, key1);
        TemplateRecord.hideItem(record, key2);

        assertTrue(TemplateRecord.restoreItem(record, key1));
        Set<String> hidden = TemplateRecord.getHiddenKeys(record);
        assertEquals(1, hidden.size());
        assertTrue(hidden.contains(key2));

        assertTrue(TemplateRecord.restoreItem(record, key2));
        hidden = TemplateRecord.getHiddenKeys(record);
        assertEquals(0, hidden.size());
    }

    public void testRestoreItemNotHidden()
    {
        MutableRecord record = new MutableRecordImpl();
        String key = "foo";
        record.put(key, "bar");

        assertFalse(TemplateRecord.restoreItem(record, key));
        Set<String> hidden = TemplateRecord.getHiddenKeys(record);
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
        MutableRecord parentRecord = new MutableRecordImpl();
        parentRecord.put("foo", "bar");
        TemplateRecord parent = new TemplateRecord("parent", null, null, parentRecord);
        MutableRecord childRecord = new MutableRecordImpl();
        TemplateRecord child = new TemplateRecord("child", parent, null, childRecord);
        assertTrue(child.containsKey("foo"));

        childRecord = childRecord.copy(true, true);
        TemplateRecord.hideItem(childRecord, "foo");
        child = new TemplateRecord("child", parent, null, childRecord);
        assertFalse(child.containsKey("foo"));
    }
}
