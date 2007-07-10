package com.zutubi.prototype.type;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
@SuppressWarnings({ "unchecked" })
public class ListTypeTest extends TypeTestCase
{
    private ListType listType;
    private CompositeType mockAType;

    protected void setUp() throws Exception
    {
        super.setUp();

        mockAType = typeRegistry.register(MockA.class);

        listType = new ListType(recordManager);
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

        SimpleInstantiator instantiator = new SimpleInstantiator(null);
        List<Object> newList = (List<Object>) instantiator.instantiate(listType, record);
        assertEquals(2, newList.size());
        assertTrue(newList.get(0) instanceof MockA);
    }

    public void testInsertionPath() throws TypeException
    {
        long lastHandle = recordManager.allocateHandle();
        MutableRecord record = mockAType.unstantiate(new MockA("valueA"));
        assertEquals("coll/" + Long.toString(lastHandle + 1), listType.getInsertionPath("coll", record));
    }

    public void testSavePath() throws TypeException
    {
        MutableRecord record = mockAType.unstantiate(new MockA("valueA"));
        assertEquals("any/path", listType.getSavePath("any/path", record));
    }

    @SymbolicName("mockA")
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
