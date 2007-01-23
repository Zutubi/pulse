package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.prototype.RecordTypeRegistry;
import com.zutubi.pulse.prototype.record.types.*;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 */
public class DefaultRecordMapperTest extends PulseTestCase
{
    private DefaultRecordMapper mapper;

    protected void setUp() throws Exception
    {
        RecordTypeRegistry typeRegistry = new RecordTypeRegistry();
        typeRegistry.register(BasicTypes.class);
        typeRegistry.register(ChildType1.class);
        typeRegistry.register(ChildType2.class);
        typeRegistry.register(GrandchildType.class);
        typeRegistry.register(ParentType.class);
        typeRegistry.register(PolymorphicList.class);
        typeRegistry.register(PolymorphicMap.class);
        typeRegistry.register(PolymorphicReference.class);
        typeRegistry.register(SingleEnum.class);
        typeRegistry.register(StringList.class);
        typeRegistry.register(StringMap.class);
        typeRegistry.register(SingleReference.class);
        typeRegistry.register(SingleString.class);
        typeRegistry.register(SingleStringList.class);
        typeRegistry.register(SingleStringMap.class);
        mapper = new DefaultRecordMapper();
        mapper.setRecordTypeRegistry(typeRegistry);
    }

    public void testBasicTypes() throws Exception
    {
        BasicTypes basics = new BasicTypes();
        basics.setBooleanO(Boolean.TRUE);
        basics.setBooleanP(false);
        basics.setByteO((byte) 1);
        basics.setByteP((byte) 2);
        basics.setCharacterO('a');
        basics.setCharacterP('b');
        basics.setDoubleO(3.3);
        basics.setDoubleP(4.4);
        basics.setFloatO((float) 0.5);
        basics.setFloatP((float) 0.6);
        basics.setIntegerO(7);
        basics.setIntegerP(8);
        basics.setLongO(9L);
        basics.setLongP(10L);
        basics.setShortO((short) 11);
        basics.setShortP((short) 12);
        basics.setString("howdy");

        testRoundTrip(basics);
    }

    public void testSingleString() throws Exception
    {
        Record r = mapper.toRecord(new SingleString("test"));
        assertSingleStringRecord(r, "test");

        Object o = mapper.fromRecord(r);
        assertTrue(o instanceof SingleString);
        SingleString s = (SingleString) o;
        assertEquals("test", s.getValue());
    }

    public void testStringList() throws Exception
    {
        Record r = mapper.toRecord(new StringList("s1", "s2", "s3"));
        assertEquals("stringlist", r.getSymbolicName());
        assertEquals(1, r.size());
        Object lo = r.get("list");
        assertNotNull(lo);
        assertTrue(lo instanceof List);
        List list = (List) lo;
        assertEquals(Arrays.asList("s1", "s2", "s3"), list);

        Object o = mapper.fromRecord(r);
        assertTrue(o instanceof StringList);
        StringList s = (StringList) o;
        assertEquals(Arrays.asList("s1", "s2", "s3"), s.getList());
    }

    public void testObjectList() throws Exception
    {
        SingleStringList ssl = new SingleStringList("one", "two");
        Record r = mapper.toRecord(ssl);
        assertEquals("singlestringlist", r.getSymbolicName());
        assertEquals(1, r.size());
        Object o = r.get("list");
        assertNotNull(o);
        assertTrue(o instanceof List);
        List list = (List)o;
        assertEquals(2, list.size());
        assertSingleStringRecord((Record) list.get(0), "one");
        assertSingleStringRecord((Record) list.get(1), "two");

        o = mapper.fromRecord(r);
        assertTrue(o instanceof SingleStringList);
        assertEquals(ssl, o);
    }

    public void testStringMap() throws Exception
    {
        StringMap sm = new StringMap();
        sm.put("foo", "bar");
        sm.put("baz", "quux");

        Record r = mapper.toRecord(sm);
        assertEquals("stringmap", r.getSymbolicName());
        assertEquals(1, r.size());
        Object mo = r.get("map");
        assertNotNull(mo);
        assertTrue(mo instanceof Map);
        Map map = (Map) mo;
        assertEquals(2, map.size());
        assertEquals("bar", map.get("foo"));
        assertEquals("quux", map.get("baz"));

        Object o = mapper.fromRecord(r);
        assertTrue(o instanceof StringMap);
        StringMap s = (StringMap) o;
        assertEquals("bar", s.get("foo"));
        assertEquals("quux", s.get("baz"));
    }

    public void testObjectMap() throws Exception
    {
        SingleStringMap sm = new SingleStringMap();
        SingleString ssb = new SingleString("bar");
        SingleString ssq = new SingleString("quux");
        sm.put("foo", ssb);
        sm.put("baz", ssq);

        Record r = mapper.toRecord(sm);
        assertEquals("singlestringmap", r.getSymbolicName());
        assertEquals(1, r.size());
        Object mo = r.get("map");
        assertNotNull(mo);
        assertTrue(mo instanceof Map);
        Map map = (Map) mo;
        assertEquals(2, map.size());
        assertSingleStringRecord((Record) map.get("foo"), "bar");
        assertSingleStringRecord((Record) map.get("baz"), "quux");

        Object o = mapper.fromRecord(r);
        assertTrue(o instanceof SingleStringMap);
        SingleStringMap s = (SingleStringMap) o;
        assertEquals(ssb, s.get("foo"));
        assertEquals(ssq, s.get("baz"));
    }

    public void testEnum() throws Exception
    {
        testRoundTrip(new SingleEnum(SingleEnum.Test.TWO));
    }

    public void testSingleReference() throws Exception
    {
        testRoundTrip(new SingleReference("blurg"));
    }

    public void testPolymorphicReferenceParent() throws Exception
    {
        testRoundTrip(new PolymorphicReference(new ParentType(1)));
    }

    public void testPolymorphicReferenceChild() throws Exception
    {
        testRoundTrip(new PolymorphicReference(new ChildType1(1, "helpmerhonda")));
    }

    public void testPolyMorphicReferenceChild2() throws Exception
    {
        testRoundTrip(new PolymorphicReference(new ChildType2(1, "one", "three", "two")));
    }
    
    public void testPolymorphicReferenceGrandchild() throws Exception
    {
        testRoundTrip(new PolymorphicReference(new GrandchildType(1, "one", 2L)));
    }

    public void testPolymorphicList() throws Exception
    {
        testRoundTrip(new PolymorphicList(new ParentType(1), new ChildType1(2, "3"), new ChildType2(4, "5", "6"), new GrandchildType(7, "8", 9L)));
    }

    public void testPolymorphicMap() throws Exception
    {
        PolymorphicMap map = new PolymorphicMap();
        map.put("parent", new ParentType(1));
        map.put("child1", new ChildType1(2, "3"));
        map.put("child2", new ChildType2(4, "5", "6"));
        map.put("grandchild", new GrandchildType(7, "8", 9L));
        testRoundTrip(map);
    }

    private void testRoundTrip(Object o)
    {
        assertEquals(o, mapper.fromRecord(mapper.toRecord(o)));
    }

    private void assertSingleStringRecord(Record r, String s)
    {
        assertEquals("ss", r.getSymbolicName());
        assertEquals(1, r.size());
        assertEquals(s, r.get("value"));
    }
}
