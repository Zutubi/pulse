package com.zutubi.tove.type;

import com.zutubi.tove.annotations.ReadOnly;
import junit.framework.TestCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DefinedTypePropertyTest extends TestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSetValueViaSetter() throws NoSuchMethodException, TypeException
    {
        DefinedTypeProperty property = new DefinedTypeProperty();
        property.setType(new PrimitiveType(String.class));
        property.setSetter(Sample.class.getDeclaredMethod("setA", String.class));

        Sample instance = new Sample();
        property.setValue(instance, "A");
        assertEquals("A", instance.getA());
    }

    public void testSetPrimitiveNull() throws NoSuchMethodException, TypeException
    {
        DefinedTypeProperty property = new DefinedTypeProperty();
        property.setType(new PrimitiveType(Long.TYPE));
        property.setSetter(Sample.class.getDeclaredMethod("setL", Long.TYPE));

        Sample instance = new Sample();
        instance.setL(1);
        property.setValue(instance, null);
        assertEquals(Long.MIN_VALUE, instance.getL());
    }

    public void testSetObjectNull() throws NoSuchMethodException, TypeException
    {
        DefinedTypeProperty property = new DefinedTypeProperty();
        property.setType(new PrimitiveType(String.class));
        property.setSetter(Sample.class.getDeclaredMethod("setA", String.class));

        Sample instance = new Sample();
        instance.setA("notNull");
        property.setValue(instance, null);
        assertNull(instance.getA());
    }

    public void testGetValueViaGetter() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        DefinedTypeProperty property = new DefinedTypeProperty();
        property.setType(new PrimitiveType(String.class));
        property.setGetter(Sample.class.getDeclaredMethod("getA"));

        Sample instance = new Sample();
        instance.setA("A");
        assertEquals(instance.getA(), property.getValue(instance));
    }

    public void testGetPrimitiveNull() throws TypeException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        DefinedTypeProperty property = new DefinedTypeProperty();
        property.setType(new PrimitiveType(Long.TYPE));
        property.setGetter(Sample.class.getDeclaredMethod("getL"));

        Sample instance = new Sample();
        instance.setL(Long.MIN_VALUE);
        assertNull(property.getValue(instance));
    }

    public void testGetObjectNull() throws NoSuchMethodException
    {
        DefinedTypeProperty property = new DefinedTypeProperty();
        property.setType(new PrimitiveType(String.class));
        property.setGetter(Sample.class.getDeclaredMethod("getA"));

        Sample instance = new Sample();
        assertNull(instance.getA());
    }

    public void testIsReadable() throws NoSuchFieldException, NoSuchMethodException
    {
        DefinedTypeProperty property = new DefinedTypeProperty();
        assertFalse(property.isReadable());
        property.setGetter(Sample.class.getDeclaredMethod("getA"));
        assertTrue(property.isReadable());
    }

    public void testIsReadOnlyByAnnotation() throws NoSuchMethodException
    {
        DefinedTypeProperty property = new DefinedTypeProperty();
        Method setter = Sample.class.getDeclaredMethod("setO", Object.class);
        property.setSetter(setter);
        assertTrue(property.isWritable());
        
        property.addAnnotation(setter.getAnnotation(ReadOnly.class));
        assertFalse(property.isWritable());
    }

    public static class Sample
    {
        /**
         * Non-primitive field.
         */
        private String a;

        public String getA()
        {
            return a;
        }

        public void setA(String a)
        {
            this.a = a;
        }

        /**
         * Primitive field
         */
        private long l;

        public long getL()
        {
            return l;
        }

        public void setL(long l)
        {
            this.l = l;
        }

        /**
         * Non-primitive read only field.
         */
        private Object o;

        public Object getO()
        {
            return o;
        }

        @ReadOnly
        public void setO(Object o)
        {
            this.o = o;
        }
    }
}
