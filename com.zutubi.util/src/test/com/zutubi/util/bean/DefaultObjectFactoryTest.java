package com.zutubi.util.bean;

import com.zutubi.util.junit.ZutubiTestCase;

public class DefaultObjectFactoryTest extends ZutubiTestCase
{
    private static final String TEST_STRING = "hello";
    private static final int    TEST_INT    = 1110098734;

    private ObjectFactory objectFactory = new DefaultObjectFactory();

    public void testGetClassInstanceSimple()
    {
        assertSame(Constructors.class, objectFactory.getClassInstance(Constructors.class.getName(), Constructors.class));
    }

    public void testGetClassInstanceUnrelatedTokenType()
    {
        getClassInstanceFailureHelper(Constructors.class.getName(), String.class);
    }

    public void testGetClassInstanceSubTokenType()
    {
        getClassInstanceFailureHelper(ParentType.class.getName(), ChildType.class);
    }

    private <T> void getClassInstanceFailureHelper(String className, Class<? super T> token)
    {
        try
        {
            objectFactory.getClassInstance(className, token);
            fail();
        }
        catch (ClassCastException e)
        {
            assertTrue(e.getMessage().contains("not a subtype"));
        }
    }

    public void testGetClassInstanceSuperTokenType()
    {
        Class<? extends ParentType> clazz = objectFactory.getClassInstance(ChildType.class.getName(), ParentType.class);
        assertSame(ChildType.class, clazz);
    }

    public void testGetClassInstanceIndirectSuperTokenType()
    {
        Class<? extends GrandParentType> clazz = objectFactory.getClassInstance(ChildType.class.getName(), GrandParentType.class);
        assertSame(ChildType.class, clazz);
    }

    public void testBuildByClass()
    {
        assertEquals(new Constructors(), objectFactory.buildBean(Constructors.class));    
    }

    public void testBuildByClassNoDefaultConstructor()
    {
        try
        {
            objectFactory.buildBean(NoDefaultConstructor.class);
            fail();
        }
        catch(Exception e)
        {
        }
    }

    public void testBuildByClassName()
    {
        assertEquals(new Constructors(), objectFactory.<Constructors>buildBean(Constructors.class.getName(), Constructors.class));
    }

    public void testBuildByClassNameNoDefaultConstructor()
    {
        try
        {
            objectFactory.<NoDefaultConstructor>buildBean(NoDefaultConstructor.class.getName(), NoDefaultConstructor.class);
            fail();
        }
        catch(Exception e)
        {
        }
    }

    public void testBuildByClassNameSuperTokenType()
    {
        ParentType bean = objectFactory.buildBean(ChildType.class.getName(), ParentType.class);
        assertTrue(bean instanceof ChildType);    
    }

    public void testBuildByClassNameBadTokenType()
    {
        try
        {
            objectFactory.<String>buildBean(ChildType.class.getName(), String.class);
            fail();
        }
        catch (ClassCastException e)
        {
            assertTrue(e.getMessage().contains("not a subtype"));
        }
    }

    public void testBuildWithArgumentByClass()
    {
        assertEquals(new Constructors(TEST_STRING), objectFactory.buildBean(Constructors.class, new Class[]{String.class}, new Object[]{TEST_STRING}));
    }

    public void testBuildWithArgumentByClassNoSuchConstructor()
    {
        try
        {
            objectFactory.buildBean(Constructors.class, new Class[]{Float.class}, new Object[]{1.0f});
            fail();
        }
        catch(Exception e)
        {
        }
    }

    public void testBuildWithArgumentByClassName()
    {
        assertEquals(new Constructors(TEST_STRING), objectFactory.<Constructors>buildBean(Constructors.class.getName(), Constructors.class, new Class[]{String.class}, new Object[]{TEST_STRING}));
    }

    public void testBuildWithArgumentByClassNameSuchConstructor()
    {
        try
        {
            objectFactory.<Constructors>buildBean(Constructors.class.getName(), Constructors.class, new Class[]{Float.class}, new Object[]{1.0f});
            fail();
        }
        catch(Exception e)
        {
        }
    }

    public void testBuildWithMultipleArgumentsByClass()
    {
        assertEquals(new Constructors(TEST_STRING, TEST_INT), objectFactory.buildBean(Constructors.class, new Class[]{String.class, Integer.TYPE}, new Object[]{TEST_STRING, TEST_INT}));
    }

    public void testBuildWithMultipleArgumentsByClassName()
    {
        assertEquals(new Constructors(TEST_STRING, TEST_INT), objectFactory.<Constructors>buildBean(Constructors.class.getName(), Constructors.class, new Class[]{String.class, Integer.TYPE}, new Object[]{TEST_STRING, TEST_INT}));
    }

    public static class GrandParentType
    {
    }

    public static class ParentType extends GrandParentType
    {
    }

    public static class ChildType extends ParentType
    {
    }

    public static class Constructors
    {
        private String s;
        private int  i;

        public Constructors()
        {
        }

        public Constructors(String s)
        {
            this.s = s;
        }

        public Constructors(String s, int i)
        {
            this.s = s;
            this.i = i;
        }

        public void setS(String s)
        {
            this.s = s;
        }

        public void setI(int i)
        {
            this.i = i;
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Constructors that = (Constructors) o;

            if (i != that.i)
            {
                return false;
            }
            return !(s != null ? !s.equals(that.s) : that.s != null);
        }

        public int hashCode()
        {
            int result;
            result = (s != null ? s.hashCode() : 0);
            result = 31 * result + i;
            return result;
        }
    }

    public static class NoDefaultConstructor
    {
        private String s;

        public NoDefaultConstructor(String s)
        {
            this.s = s;
        }

        public String getS()
        {
            return s;
        }
    }
}
