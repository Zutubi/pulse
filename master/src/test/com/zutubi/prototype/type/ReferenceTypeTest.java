package com.zutubi.prototype.type;

import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.AbstractConfigurationSystemTestCase;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;

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

        MapType refererMap = new MapType();
        refererMap.setTypeRegistry(typeRegistry);
        refererMap.setCollectionType(refererType);
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
