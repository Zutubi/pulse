package com.zutubi.pulse.master.tove.format;

import com.google.common.base.Function;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Sort;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StateDisplayFieldsTest extends ZutubiTestCase
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

    public void testDefaultCollectionFields() throws Exception
    {
        StateDisplayFields sd = new StateDisplayFields(T.class, DefaultCollectionFields.class, objectFactory);
        assertFields(sd.getCollectionFields(asList(new T()), new ConfigType()), "a", "b", "noParam", "parent");
    }

    public void testDefinedCollectionFields() throws Exception
    {
        StateDisplayFields sd = new StateDisplayFields(T.class, DefinedCollectionFields.class, objectFactory);
        assertFields(sd.getCollectionFields(asList(new T()), new ConfigType()), "a");
    }

    public void testFormatCollection() throws Exception
    {
        StateDisplayFields sd = new StateDisplayFields(T.class, DefaultCollectionFields.class, objectFactory);
        assertEquals(asList("x", "y"), sd.formatCollection("a", asList(new T("x"), new T("y")), new ConfigType()));        
    }

    public void testFormatCollectionParent() throws Exception
    {
        StateDisplayFields sd = new StateDisplayFields(T.class, DefaultCollectionFields.class, objectFactory);
        ConfigType parentInstance = new ConfigType();
        parentInstance.setConfigurationPath("some/path");
        assertEquals("some/path", sd.formatCollection("parent", asList(new T("x"), new T("y")), parentInstance));
    }

    public void testFormatCollectionNoParam() throws Exception
    {
        StateDisplayFields sd = new StateDisplayFields(T.class, DefaultCollectionFields.class, objectFactory);
        assertEquals("noparam", sd.formatCollection("noParam", asList(new T("x"), new T("y")), new ConfigType()));        
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
            return asList("a");
        }
    }

    public static class DefaultCollectionFields
    {
        public List<String> formatCollectionA(Collection<T> ts)
        {
            return CollectionUtils.map(ts, new Function<T, String>()
            {
                public String apply(T t)
                {
                    return t.getName();
                }
            });
        }

        public String formatCollectionB(Collection<T> ts)
        {
            return "fixed";
        }

        public String formatCollectionParent(Collection<T> ts, Configuration parentInstance)
        {
            return parentInstance.getConfigurationPath();
        }

        public String formatCollectionNoParam()
        {
            return "noparam";
        }

        public void formatCollectionVoidReturn()
        {
        }

        public String formatCollectionNonConfigParent(NonConfigType nc, Collection<T> ts)
        {
            return "nonconfigtype";
        }

        public String format()
        {
            return "format";
        }

        public String formatCollection()
        {
            return "formatCollection";
        }

        public String anything()
        {
            return "anything";
        }
    }

    public static class DefinedCollectionFields
    {
        public String formatCollectionA(Collection<T> ts)
        {
            return "a";
        }

        public String formatCollectionB(Collection<T> ts)
        {
            return "b";
        }

        public List<String> getCollectionFields(Collection<T> ts, Configuration parentInstance)
        {
            return asList("a");
        }
    }
}
