package com.zutubi.tove.format;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.util.Sort;
import com.zutubi.util.bean.DefaultObjectFactory;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 */
public class StateDisplayFieldsTest extends TestCase
{
    private DefaultObjectFactory objectFactory = new DefaultObjectFactory();

    public void testDefaultFields() throws Exception
    {
        StateDisplayFields sd = new StateDisplayFields(T.class, DefaultFields.class, objectFactory);
        assertFields(sd.getFields(new T()), "a", "b", "c", "noParam");
    }

    public void testDefinedFields() throws Exception
    {
        StateDisplayFields sd = new StateDisplayFields(T.class, DefinedFields.class, objectFactory);
        assertFields(sd.getFields(new T()), "a");
    }

    public void testFormatField() throws Exception
    {
        StateDisplayFields sd = new StateDisplayFields(T.class, DefinedFields.class, objectFactory);
        assertEquals("test name", sd.format("a", new T("test name")));
    }

    public void testFormatFieldNoArg() throws Exception
    {
        StateDisplayFields sd = new StateDisplayFields(T.class, DefaultFields.class, objectFactory);
        assertEquals("noparam", sd.format("noParam", new T("test name")));
    }

    private void assertFields(List<String> got, String... expected)
    {
        assertEquals(expected.length, got.size());
        Collections.sort(got, new Sort.StringComparator());
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], got.get(i));
        }
    }

    public static class T extends AbstractNamedConfiguration
    {
        public T()
        {
        }

        public T(String s)
        {
            super(s);
        }
    }

    public static class U extends T
    {
    }

    public static class ConfigType extends AbstractConfiguration
    {
    }

    public static class NonConfigType
    {
    }

    public static class DefaultFields
    {
        public String formatA(T t)
        {
            return t.getName();
        }

        public String formatB(T t)
        {
            return t.getName();
        }

        public String formatC(T t)
        {
            return t.getName();
        }

        public String formatNoParam()
        {
            return "noparam";
        }

        public void formatVoidReturn()
        {
        }

        public String formatNonMatchingType(ConfigType c)
        {
            return "nonmatchingtype";
        }

        public String formatNonConfigType(NonConfigType nc)
        {
            return "nonconfigtype";
        }
    }

    public static class DefinedFields
    {
        public String formatA(T t)
        {
            return t.getName();
        }

        public String formatB(T t)
        {
            return t.getName();
        }

        public List<String> getFields(T t)
        {
            return Arrays.asList("a");
        }
    }
}
