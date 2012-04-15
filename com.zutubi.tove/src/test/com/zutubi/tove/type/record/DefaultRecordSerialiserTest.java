package com.zutubi.tove.type.record;

import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class DefaultRecordSerialiserTest extends ZutubiTestCase
{
    private File tmpDir;
    private DefaultRecordSerialiser serialiser;

    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = FileSystemUtils.createTempDir(getName(), "");
        serialiser = new DefaultRecordSerialiser(tmpDir);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);
        super.tearDown();
    }

    public void testSimple() throws IOException
    {
        MutableRecord simple = createSimple();
        assertRoundTrip(simple, true);
    }

    public void testNested() throws IOException
    {
        MutableRecord nested = createNested();
        assertRoundTrip(nested, true);
    }

    public void testSimpleCollection() throws IOException
    {
        MutableRecord record = new MutableRecordImpl();
        record.put("item1", createSimple());
        record.put("item2", createSimple());
        record.put("item3", createSimple());
        assertRoundTrip(record, true);
    }

    public void testNonExistentParent() throws IOException
    {
        try
        {
            serialiser.serialise(PathUtils.getPath("nothing", "here"), new MutableRecordImpl(), true, 2);
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
        assertRoundTrip(record, true);
    }

    public void testUnsupportedNameNested() throws IOException
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName("ouchy");
        record.put("<>~!@#%^&*()`-=_+{}[]'\"|:;,.?", createSimple());
        assertRoundTrip(record, true);
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
        assertRoundTrip(root, true);
    }

    public void testShallowSave() throws IOException
    {
        MutableRecord record = createNested();
        record = assertRoundTrip(record, false);
        assertFalse(record.containsKey("child"));
    }

    public void testReSaveNewProperty() throws IOException
    {
        MutableRecord record = createSimple();
        record = assertRoundTrip(record, true);
        record.put("another property", "another value");
        assertRoundTrip(record, true);
    }

    public void testReSaveChangeProperty() throws IOException
    {
        MutableRecord record = createSimple();
        record = assertRoundTrip(record, true);
        record.put("property", "new value");
        assertRoundTrip(record, true);
    }

    public void testReSaveRemoveProperty() throws IOException
    {
        MutableRecord record = createSimple();
        record = assertRoundTrip(record, true);
        record.remove("property");
        assertRoundTrip(record, true);
    }

    public void testReSaveNewNestedProperty() throws IOException
    {
        MutableRecord record = createNested();
        record = assertRoundTrip(record, true);
        record.put("another child", createSimple());
        assertRoundTrip(record, true);
    }

    public void testReSaveChangeNestedProperty() throws IOException
    {
        MutableRecord record = createNested();
        record = assertRoundTrip(record, true);
        ((MutableRecord) record.get("child")).put("property", "new value");
        assertRoundTrip(record, true);
    }

    public void testReSaveRemoveNestedProperty() throws IOException
    {
        MutableRecord record = createNested();
        record = assertRoundTrip(record, true);
        record.remove("child");
        assertRoundTrip(record, true);
    }

    public void testArray() throws IOException
    {
        MutableRecord record = new MutableRecordImpl();
        String[] array = new String[]{"test", "array", "here"};
        record.put("array", array);
        MutableRecord other = roundTrip(record, false);
        String[] otherArray = (String[]) other.get("array");
        assertEquals(array.length, otherArray.length);
        for(int i = 0; i < array.length; i++)
        {
            assertEquals(array[i], otherArray[i]);
        }
    }

    public void testMaxDepthZero()
    {
        MutableRecord record = createDeepRecord();

        serialiser.setMaxPathDepth(0);
        serialiser.serialise(record, true);

        assertFalse(new File(tmpDir, "1").isDirectory());

        MutableRecord other = serialiser.deserialise();
        assertEquals(record, other);
    }

    public void testMaxDepthOne()
    {
        MutableRecord record = createDeepRecord();

        serialiser.setMaxPathDepth(1);
        serialiser.serialise(record, true);

        assertTrue(new File(tmpDir, "1").isDirectory());
        assertFalse(new File(tmpDir, "1/2").isDirectory());

        MutableRecord other = serialiser.deserialise();
        assertEquals(record, other);
    }

    public void testMaxDepthTwo()
    {
        MutableRecord record = createDeepRecord();

        serialiser.setMaxPathDepth(2);
        serialiser.serialise(record, true);

        assertTrue(new File(tmpDir, "1").isDirectory());
        assertTrue(new File(tmpDir, "1/2").isDirectory());
        assertFalse(new File(tmpDir, "1/2/3").isDirectory());

        MutableRecord other = serialiser.deserialise();
        assertEquals(record, other);
    }

    private MutableRecord createDeepRecord()
    {
        MutableRecord record = createSimple();
        MutableRecord one = createSimple();
        record.put("1", one);
        MutableRecord two = createSimple();
        one.put("2", two);
        MutableRecord three = createSimple();
        two.put("3", three);
        return record;
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

    private MutableRecord assertRoundTrip(MutableRecord record, boolean deep) throws IOException
    {
        MutableRecord other = roundTrip(record, deep);

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

    private MutableRecord roundTrip(MutableRecord record, boolean deep)
    {
        serialiser.serialise(record, deep);
        return serialiser.deserialise();
    }
}