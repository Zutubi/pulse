package com.zutubi.prototype.type;

import junit.framework.TestCase;
import com.zutubi.prototype.type.record.RecordManager;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class PersistenceManagerTest extends TestCase
{
    private PersistenceManager persistenceManager;
    private RecordManager recordManager;
    private TypeRegistry typeRegistry;

    protected void setUp() throws Exception
    {
        super.setUp();

        recordManager = new RecordManager();
        typeRegistry = new TypeRegistry();
        persistenceManager = new PersistenceManager();
        persistenceManager.setTypeRegistry(typeRegistry);
        persistenceManager.setRecordManager(recordManager);

        typeRegistry.register("mock", Mock.class);
    }

    protected void tearDown() throws Exception
    {
        persistenceManager = null;
        typeRegistry = null;
        recordManager = null;

        super.tearDown();
    }

    public void testPersistType() throws TypeException
    {
        Mock mock = new Mock("str");
        persistenceManager.store("some/path", mock);
        Object obj = persistenceManager.load("some/path");
        assertEquals(mock, obj);
    }

    public void testPersistToList() throws TypeException
    {
        List<Mock> list = new LinkedList<Mock>();
        list.add(new Mock("a"));
        list.add(new Mock("b"));
        persistenceManager.store("some/list", list);

        Mock mockC = new Mock("c");
        persistenceManager.store("some/list", mockC);

        List<Object> persistedList = (List<Object>) persistenceManager.load("some/list");
        assertEquals(3, persistedList.size());

        assertEquals(list.get(0), persistedList.get(0));
        assertEquals(list.get(1), persistedList.get(1));
        assertEquals(mockC, persistedList.get(2));
    }

    public static class Mock
    {
        private String string;

        public Mock()
        {

        }
        public Mock(String str)
        {
            this.string = str;
        }

        public String getString()
        {
            return string;
        }

        public void setString(String string)
        {
            this.string = string;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Mock mock = (Mock) o;

            if (string != null ? !string.equals(mock.string) : mock.string != null) return false;

            return true;
        }

        public int hashCode()
        {
            return (string != null ? string.hashCode() : 0);
        }
    }
}
