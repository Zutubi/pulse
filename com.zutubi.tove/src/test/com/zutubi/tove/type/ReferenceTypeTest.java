package com.zutubi.tove.type;

import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

/**
 */
public class ReferenceTypeTest extends AbstractConfigurationSystemTestCase
{
    private ReferenceType referenceType;

    protected void setUp() throws Exception
    {
        super.setUp();
        CompositeType refererType = typeRegistry.register(Referer.class);
        CompositeType refereeType = typeRegistry.getType(Referee.class);
        referenceType = new ReferenceType(refereeType, configurationReferenceManager);

        MapType refererMap = new MapType(refererType, typeRegistry);
        configurationPersistenceManager.register("refs", refererMap);
    }

    public void testToXmlRpcZero() throws TypeException
    {
        assertNull(referenceType.toXmlRpc("0"));
    }

    public void testToXmlRpcInvalidHandle() throws TypeException
    {
        assertNull(referenceType.toXmlRpc("9"));
    }

    public void testToXmlRpc() throws TypeException
    {
        Referee ee = new Referee("ee");
        Referer er = new Referer("er", ee);
        String erPath = configurationTemplateManager.insert("refs", er);
        String eePath = PathUtils.getPath(erPath, "r");

        Record eeRecord = configurationTemplateManager.getRecord(eePath);
        Object o = referenceType.toXmlRpc(Long.toString(eeRecord.getHandle()));
        assertTrue(o instanceof String);
        assertEquals(eePath, o);
    }

    public void testFromXmlRpcNull() throws TypeException
    {
        assertEquals("0", referenceType.fromXmlRpc(null));
    }

    public void testToXmlRpcInvalidPath() throws TypeException
    {
        try
        {
            referenceType.fromXmlRpc("nosuchpath");
            fail();
        }
        catch (TypeException e)
        {
            assertEquals("Reference to unknown path 'nosuchpath'", e.getMessage());
        }
    }

    public void testFromXmlRpc() throws TypeException
    {
        Referee ee = new Referee("ee");
        Referer er = new Referer("er", ee);
        String erPath = configurationTemplateManager.insert("refs", er);
        String eePath = PathUtils.getPath(erPath, "r");

        Record eeRecord = configurationTemplateManager.getRecord(eePath);
        Object o = referenceType.fromXmlRpc(eePath);
        assertTrue(o instanceof String);
        assertEquals(Long.toString(eeRecord.getHandle()), o);
    }

    @SymbolicName("referee")
    public static class Referee extends AbstractConfiguration
    {
        @ID
        private String a;

        public Referee()
        {
        }

        public Referee(String a)
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

    @SymbolicName("referrer")
    public static class Referer extends AbstractNamedConfiguration
    {
        private Referee r;

        public Referer()
        {
        }

        public Referer(String name, Referee r)
        {
            super(name);
            this.r = r;
        }

        public Referee getR()
        {
            return r;
        }

        public void setR(Referee r)
        {
            this.r = r;
        }
    }

}
