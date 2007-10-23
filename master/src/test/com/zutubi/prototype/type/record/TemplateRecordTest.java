package com.zutubi.prototype.type.record;

import com.zutubi.pulse.test.PulseTestCase;

import java.util.Set;

/**
 */
public class TemplateRecordTest extends PulseTestCase
{
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
        MutableRecordImpl childRecord = new MutableRecordImpl();
        TemplateRecord child = new TemplateRecord("child", parent, null, childRecord);
        assertTrue(child.containsKey("foo"));
        TemplateRecord.hideItem(childRecord, "foo");
        assertFalse(child.containsKey("foo"));
    }
}
