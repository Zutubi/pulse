package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.prototype.RecordTypeRegistry;
import com.zutubi.pulse.test.PulseTestCase;

/**
 */
public class DefaultRecordMapperTest extends PulseTestCase
{
    private DefaultRecordMapper mapper;

    protected void setUp() throws Exception
    {
        RecordTypeRegistry typeRegistry = new RecordTypeRegistry();
        typeRegistry.register(SingleString.class);
        mapper = new DefaultRecordMapper();
        mapper.setRecordTypeRegistry(typeRegistry);
    }

    public void testSingleString() throws Exception
    {
        Record r = mapper.toRecord(new SingleString("test"));
        assertEquals("ss", r.getSymbolicName());
        assertEquals(1, r.size());
        assertEquals("test", r.get("value"));
    }

    @SymbolicName("ss")
    private class SingleString
    {
        private String value;

        public SingleString(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }
    }
}
