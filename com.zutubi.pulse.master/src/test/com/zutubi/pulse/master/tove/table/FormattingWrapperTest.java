package com.zutubi.pulse.master.tove.table;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.RandomUtils;
import com.zutubi.tove.annotations.Format;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.api.Formatter;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.config.api.AbstractConfiguration;

public class FormattingWrapperTest extends ZutubiTestCase
{
    private CompositeType type;
    private ObjectFactory objectFactory;
    private String value;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        TypeRegistry typeRegistry = new TypeRegistry();
        type = typeRegistry.register(Sample.class);
        value = RandomUtils.randomString(5);
        objectFactory = new DefaultObjectFactory();
    }

    public void testDefaultFormatting() throws Exception
    {
        assertFormatting(value, "a");
    }

    public void testReturnFormatting() throws Exception
    {
        assertFormatting("<r>" + value + "</r>", "b");
    }

    public void testVirtualFormatting() throws Exception
    {
        assertFormatting("<f>" + value + "</f>", "c");
    }

    public void testVirtualFormattingOnExtendedInstance() throws Exception
    {
        Sample s = new ExtendedSample(value);
        FormattingWrapper wrapper = newFormattingWrapper(s, type);
        assertEquals("<f><e>" + value + "</e></f>", wrapper.get("c"));
    }

    public void testFormattingWrapperCreation() throws TypeException
    {
        new FormattingWrapper(new ExtendedSample(), type);

        try
        {
            new FormattingWrapper(this, type);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("not of the expected type"));
        }
    }

    private void assertFormatting(String expected, String propertyName) throws Exception
    {
        Sample s = new Sample(value);
        FormattingWrapper wrapper = newFormattingWrapper(s, type);
        assertEquals(expected, wrapper.get(propertyName));
    }

    private FormattingWrapper newFormattingWrapper(Sample s, CompositeType type)
    {
        FormattingWrapper wrapper = new FormattingWrapper(s, type);
        wrapper.setObjectFactory(objectFactory);
        return wrapper;
    }

    @SymbolicName("sample")
    public static class Sample extends AbstractConfiguration
    {
        private String value;

        public Sample()
        {
        }

        private Sample(String value)
        {
            this.value = value;
        }

        public String getA()
        {
            return value;
        }

        @Format("StringFormatter")
        public String getB()
        {
            return value;
        }
    }

    public static class ExtendedSample extends Sample
    {
        public ExtendedSample()
        {
        }

        public ExtendedSample(String value)
        {
            super(value);
        }

        public String getA()
        {
            return "<e>" + super.getA() + "</e>";
        }
    }

    public static class SampleFormatter
    {
        public String getC(Sample a)
        {
            return "<f>" + a.getA() + "</f>";
        }
    }

    public static class StringFormatter implements Formatter<String>
    {
        public String format(String str)
        {
            return "<r>" + str + "</r>";
        }
    }
}
