package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.config.annotations.ID;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class ListTypeTest extends TypeTestCase
{
    private ListType listType;
    private CompositeType mockAType;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockAType = typeRegistry.register("mockA", MockA.class);

        listType = new ListType(configurationTemplateManager);
        listType.setTypeRegistry(typeRegistry);
        listType.setCollectionType(mockAType);
        
    }

    protected void tearDown() throws Exception
    {
        listType = null;
        mockAType = null;
        
        super.tearDown();
    }

    public void testCompositeObjectList() throws TypeException
    {
        List<Object> list = new LinkedList<Object>();
        list.add(new MockA("valueA"));
        list.add(new MockA("valueB"));

        Record record = (Record) listType.unstantiate(list);

        List<Object> newList = listType.instantiate("", record);
        assertEquals(2, newList.size());
        assertTrue(newList.get(0) instanceof MockA);
    }

    public void testInsertionPath() throws TypeException
    {
        Record collection = new MutableRecordImpl();
        Record record = mockAType.unstantiate(new MockA("valueA"));

        assertEquals("1", listType.getInsertionPath(collection, record));
        assertEquals("2", listType.getInsertionPath(collection, record));
        assertEquals("3", listType.getInsertionPath(collection, record));
    }

    public void testSavePath() throws TypeException
    {
        Record collection = new MutableRecordImpl();
        Record record = mockAType.unstantiate(new MockA("valueA"));

        assertEquals("1", listType.getInsertionPath(collection, record));
        assertEquals("1", listType.getSavePath(collection, record));
        assertEquals("2", listType.getInsertionPath(collection, record));
        assertEquals("2", listType.getSavePath(collection, record));
    }

    public static class MockA extends AbstractConfiguration
    {
        @ID
        private String a;

        public MockA()
        {
        }

        public MockA(String a)
        {
            this.a = a;
        }

        public String getA()
        {
            return a;
        }

        public void setA(String a)
        {
            this.a = a;
        }
    }
}
