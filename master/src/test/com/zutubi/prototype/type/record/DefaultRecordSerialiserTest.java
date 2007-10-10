package com.zutubi.prototype.type.record;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 */
public class DefaultRecordSerialiserTest extends PulseTestCase
{
    private File tmpDir;
    private DefaultRecordSerialiser serialiser;

    protected void setUp() throws Exception
    {
        tmpDir = FileSystemUtils.createTempDir(getName(), "");
        serialiser = new DefaultRecordSerialiser(tmpDir);
    }

    protected void tearDown() throws Exception
    {
        if (!FileSystemUtils.rmdir(tmpDir))
        {
            throw new RuntimeException("Failed to clean up " + tmpDir.getAbsolutePath());
        }
    }

    public void testSimple() throws IOException
    {
        MutableRecord simple = createSimple();
        assertRoundTrip("test", simple, true);
    }

    public void testNested() throws IOException
    {
        MutableRecord nested = createNested();
        assertRoundTrip("test", nested, true);
    }

    public void testSimpleCollection() throws IOException
    {
        MutableRecord record = new MutableRecordImpl();
        record.put("item1", createSimple());
        record.put("item2", createSimple());
        record.put("item3", createSimple());
        assertRoundTrip("test", record, true);
    }

    public void testNonExistentParent() throws IOException
    {
        try
        {
            serialiser.serialise(PathUtils.getPath("nothing", "here"), new MutableRecordImpl(), true);
            fail();
        }
        catch(RecordSerialiseException e)
        {
            assertTrue(e.getMessage().startsWith("Could not create destination directory"));
        }
    }

    public void testUnsupportedName() throws IOException
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName("ouchy");
        record.put("<>~!@#$%^&*()`-=_+{}[]\\'\"|:;,.?/", "<>~!@#$%^&*()`-=_+{}[]\\'\"|:;,.?/");
        assertRoundTrip("test", record, true);
    }

    public void testUnsupportedNameNested() throws IOException
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName("ouchy");
        record.put("<>~!@#%^&*()`-=_+{}[]'\"|:;,.?", createSimple());
        assertRoundTrip("test", record, true);
    }

    public void testUnsupportedNameMultiNested() throws IOException
    {
        MutableRecord grandchild = createSimple();
        MutableRecord child = new MutableRecordImpl();
        child.setSymbolicName("ouchy");
        child.put("<>~!@#%^&*()`-=_+{}[]'\"|:;,.?", grandchild);
        MutableRecord root = new MutableRecordImpl();
        root.setSymbolicName("ouchy");
        root.put("<>~!@#%^&*()`-=_+{}[]'\"|:;,.?", child);
        assertRoundTrip("test", root, true);
    }

    public void testShallowSave() throws IOException
    {
        MutableRecord record = createNested();
        record = assertRoundTrip("test", record, false);
        assertFalse(record.containsKey("child"));
    }

    public void testReSaveNewProperty() throws IOException
    {
        MutableRecord record = createSimple();
        record = assertRoundTrip("test", record, true);
        record.put("another property", "another value");
        assertRoundTrip("test", record, true);
    }

    public void testReSaveChangeProperty() throws IOException
    {
        MutableRecord record = createSimple();
        record = assertRoundTrip("test", record, true);
        record.put("property", "new value");
        assertRoundTrip("test", record, true);
    }

    public void testReSaveRemoveProperty() throws IOException
    {
        MutableRecord record = createSimple();
        record = assertRoundTrip("test", record, true);
        record.remove("property");
        assertRoundTrip("test", record, true);
    }

    public void testReSaveNewNestedProperty() throws IOException
    {
        MutableRecord record = createNested();
        record = assertRoundTrip("test", record, true);
        record.put("another child", createSimple());
        assertRoundTrip("test", record, true);
    }

    public void testReSaveChangeNestedProperty() throws IOException
    {
        MutableRecord record = createNested();
        record = assertRoundTrip("test", record, true);
        ((MutableRecord) record.get("child")).put("property", "new value");
        assertRoundTrip("test", record, true);
    }

    public void testReSaveRemoveNestedProperty() throws IOException
    {
        MutableRecord record = createNested();
        record = assertRoundTrip("test", record, true);
        record.remove("child");
        assertRoundTrip("test", record, true);
    }

    public void testArray() throws IOException
    {
        MutableRecord record = new MutableRecordImpl();
        String[] array = new String[]{"test", "array", "here"};
        record.put("array", array);
        MutableRecord other = roundTrip("argh", record, false);
        String[] otherArray = (String[]) other.get("array");
        assertEquals(array.length, otherArray.length);
        for(int i = 0; i < array.length; i++)
        {
            assertEquals(array[i], otherArray[i]);
        }
    }

    private MutableRecord createSimple()
    {
        MutableRecord simple = new MutableRecordImpl();
        simple.setSymbolicName("simple");
        simple.put("property", "value");
        return simple;
    }

    private MutableRecord createNested()
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName("nested");
        record.put("child", createSimple());
        return record;
    }

    private MutableRecord assertRoundTrip(String path, MutableRecord record, boolean deep) throws IOException
    {
        MutableRecord other = roundTrip(path, record, deep);

        if(deep)
        {
            assertEquals(record, other);
        }
        else
        {
             // Shallow equals.
            Set<String> keys = record.simpleKeySet();
            assertEquals(keys, other.simpleKeySet());
            for(String key: keys)
            {
                assertEquals(record.get(key), other.get(key));
            }

            keys = record.metaKeySet();
            assertEquals(keys, other.metaKeySet());
            for(String key: keys)
            {
                assertEquals(record.getMeta(key), other.getMeta(key));
            }
        }

        return other;
    }

    private MutableRecord roundTrip(String path, MutableRecord record, boolean deep)
    {
        serialiser.serialise(path, record, deep);
        return serialiser.deserialise(path, new RecordHandler()
        {
            public void handle(String path, Record record)
            {
                // noop
            }
        });
    }
}