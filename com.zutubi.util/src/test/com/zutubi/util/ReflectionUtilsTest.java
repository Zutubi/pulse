package com.zutubi.util;

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 */
public class ReflectionUtilsTest extends TestCase
{
    private Method noParams;
    private Method oneParam;
    private Method twoParams;
    private Method parentParam;
    private Method returnsList;
    private Method returnsStringList;

    protected void setUp() throws Exception
    {
        noParams = Methods.class.getMethod("noParams");
        oneParam = Methods.class.getMethod("oneParam", Foo.class);
        twoParams = Methods.class.getMethod("twoParams", Foo.class, Bar.class);
        parentParam = Methods.class.getMethod("parentParam", Parent.class);
        returnsList = Methods.class.getMethod("returnsList");
        returnsStringList = Methods.class.getMethod("returnsStringList");
    }

    public void testAcceptsParametersNoParams() throws Exception
    {
        assertTrue(ReflectionUtils.acceptsParameters(noParams));
        assertFalse(ReflectionUtils.acceptsParameters(oneParam));
    }

    public void testAcceptsParametersOneParam() throws Exception
    {
        assertFalse(ReflectionUtils.acceptsParameters(noParams, Foo.class));
        assertTrue(ReflectionUtils.acceptsParameters(oneParam, Foo.class));
    }

    public void testAcceptsParametersTwoParams() throws Exception
    {
        assertFalse(ReflectionUtils.acceptsParameters(noParams, Foo.class, Bar.class));
        assertFalse(ReflectionUtils.acceptsParameters(oneParam, Foo.class, Bar.class));
        assertTrue(ReflectionUtils.acceptsParameters(twoParams, Foo.class, Bar.class));
    }

    public void testAcceptsParametersInheritedType() throws Exception
    {
        assertTrue(ReflectionUtils.acceptsParameters(parentParam, Parent.class));
        assertTrue(ReflectionUtils.acceptsParameters(parentParam, Child.class));
    }

    public void testReturnsList() throws Exception
    {
        assertFalse(ReflectionUtils.returnsParameterisedType(returnsList, List.class));
    }

    public void testReturnsStringList() throws Exception
    {
        assertFalse(ReflectionUtils.returnsParameterisedType(returnsList, List.class, String.class));
        assertFalse(ReflectionUtils.returnsParameterisedType(returnsStringList, List.class, Foo.class));
        assertFalse(ReflectionUtils.returnsParameterisedType(returnsStringList, Foo.class, String.class));
        assertTrue(ReflectionUtils.returnsParameterisedType(returnsStringList, List.class, String.class));
    }

    public void testReturnsStringListCollection() throws Exception
    {
        assertTrue(ReflectionUtils.returnsParameterisedType(returnsStringList, Collection.class, String.class));
    }

    public static class Methods
    {
        public void noParams()
        {
        }

        public void oneParam(Foo f)
        {
        }

        public void twoParams(Foo f, Bar b)
        {
        }

        public void parentParam(Parent c)
        {
        }

        public List returnsList()
        {
            return null;
        }

        public List<String> returnsStringList()
        {
            return null;
        }
    }

    public static class Foo
    {
    }

    public static class Bar
    {
    }

    public static class Parent
    {
    }

    public static class Child extends Parent
    {
    }
}
