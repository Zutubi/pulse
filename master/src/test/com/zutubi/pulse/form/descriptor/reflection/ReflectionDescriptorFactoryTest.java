package com.zutubi.pulse.form.descriptor.reflection;

import junit.framework.TestCase;
import com.zutubi.pulse.form.mock.MockBook;
import com.zutubi.pulse.form.mock.MockBookPro;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.annotation.AnnotationDecorator;

import java.util.List;

/**
 * <class-comment/>
 */
public class ReflectionDescriptorFactoryTest extends TestCase
{
    ReflectionDescriptorFactory factory;

    protected void setUp() throws Exception
    {
        super.setUp();

        factory = new ReflectionDescriptorFactory();
        factory.addDecorator(new AnnotationDecorator());
    }

    protected void tearDown() throws Exception
    {
        factory = null;

        super.tearDown();
    }

    public void testSimplePojoWithStrings()
    {
        FormDescriptor form = factory.createFormDescriptor(MockBook.class);
        assertEquals(MockBook.class, form.getType());

        List<FieldDescriptor> fields = form.getFieldDescriptors();
        assertEquals(2, fields.size());

        FieldDescriptor titleField = form.getFieldDescriptor("title");
        assertNotNull(titleField);
        assertEquals(String.class, titleField.getType());
        assertEquals("title", titleField.getName());
        assertEquals("text", titleField.getFieldType());
        assertFalse(titleField.isRequired());

        FieldDescriptor contentField = form.getFieldDescriptor("content");
        assertNotNull(contentField);
        assertEquals(String.class, contentField.getType());
        assertEquals("content", contentField.getName());
        assertEquals("text", contentField.getFieldType());
        assertFalse(contentField.isRequired());
    }

    public void testPojoWithAnnotationDefinedType()
    {
        FormDescriptor form = factory.createFormDescriptor(MockBookPro.class);
        assertEquals(MockBookPro.class, form.getType());

        List<FieldDescriptor> fields = form.getFieldDescriptors();
        assertEquals(2, fields.size());

        FieldDescriptor authorField = form.getFieldDescriptor("author");
        assertNotNull(authorField);
        assertEquals(String.class, authorField.getType());
        assertEquals("author", authorField.getName());
        assertEquals("author", authorField.getFieldType());
        assertTrue(authorField.isRequired());
    }
}
