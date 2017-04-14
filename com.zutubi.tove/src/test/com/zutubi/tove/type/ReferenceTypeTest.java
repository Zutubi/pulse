/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        assertNull(referenceType.toXmlRpc(null, "0"));
    }

    public void testToXmlRpcInvalidHandle() throws TypeException
    {
        assertNull(referenceType.toXmlRpc(null, "9"));
    }

    public void testToXmlRpc() throws TypeException
    {
        Referee ee = new Referee("ee");
        Referer er = new Referer("er", ee);
        String erPath = configurationTemplateManager.insertInstance("refs", er);
        String eePath = PathUtils.getPath(erPath, "r");

        Record eeRecord = configurationTemplateManager.getRecord(eePath);
        Object o = referenceType.toXmlRpc(null, Long.toString(eeRecord.getHandle()));
        assertTrue(o instanceof String);
        assertEquals(eePath, o);
    }

    public void testFromXmlRpcNull() throws TypeException
    {
        assertEquals("0", referenceType.fromXmlRpc(null, null, true));
    }

    public void testToXmlRpcInvalidPath() throws TypeException
    {
        try
        {
            referenceType.fromXmlRpc(null, "nosuchpath", true);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Invalid path 'nosuchpath': references non-existant root scope 'nosuchpath'", e.getMessage());
        }
    }

    public void testFromXmlRpc() throws TypeException
    {
        Referee ee = new Referee("ee");
        Referer er = new Referer("er", ee);
        String erPath = configurationTemplateManager.insertInstance("refs", er);
        String eePath = PathUtils.getPath(erPath, "r");

        Record eeRecord = configurationTemplateManager.getRecord(eePath);
        Object o = referenceType.fromXmlRpc(null, eePath, true);
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
