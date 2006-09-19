package com.zutubi.pulse.form.descriptor.reflection;

import junit.framework.TestCase;
import com.zutubi.pulse.form.mock.*;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.InstanceDescriptor;
import com.zutubi.pulse.form.descriptor.annotation.AnnotationDecorator;
import com.zutubi.pulse.form.FieldType;

import java.util.List;
import java.util.Map;

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
        assertEquals(FieldType.TEXT, titleField.getFieldType());
        assertFalse(titleField.isRequired());

        FieldDescriptor contentField = form.getFieldDescriptor("content");
        assertNotNull(contentField);
        assertEquals(String.class, contentField.getType());
        assertEquals("content", contentField.getName());
        assertEquals(FieldType.TEXT, contentField.getFieldType());
        assertFalse(contentField.isRequired());
    }

    // this test is related to annotations, and should probably be moved over there. But, for now, it can stay here.
    public void testMockBookProAuthorFieldTypeAnnotation()
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

    public void testMockAccountFields()
    {
        FormDescriptor form = factory.createFormDescriptor(MockAccount.class);
        assertEquals(MockAccount.class, form.getType());

        List<FieldDescriptor> fields = form.getFieldDescriptors();
        assertEquals(4, fields.size());

        // check that password field is automagically typed.
        FieldDescriptor passwordField = form.getFieldDescriptor("password");
        assertNotNull(passwordField);
        assertEquals(String.class, passwordField.getType());
        assertEquals("password", passwordField.getName());
        assertEquals(FieldType.PASSWORD, passwordField.getFieldType());

        // check that the password annotation hint has worked on the pass field.
        FieldDescriptor passField = form.getFieldDescriptor("pass");
        assertNotNull(passField);
        assertEquals(String.class, passField.getType());
        assertEquals("pass", passField.getName());
        assertEquals(FieldType.PASSWORD, passField.getFieldType());

        // check that the name field has an non standard size
        FieldDescriptor nameField = form.getFieldDescriptor("name");
        assertNotNull(nameField);
        assertEquals(String.class, nameField.getType());
        assertEquals("name", nameField.getName());
        assertEquals(FieldType.TEXT, nameField.getFieldType());
        assertEquals(50, nameField.getParameters().get("size"));

        // check that the user field does not have the undefined size of 0 specified.
        FieldDescriptor userField = form.getFieldDescriptor("user");
        assertNotNull(userField);
        assertEquals(String.class, userField.getType());
        assertEquals("user", userField.getName());
        assertEquals("text", userField.getFieldType());
        assertFalse(userField.getParameters().containsKey("size"));
    }

    public void testMockSelectFields()
    {
        FormDescriptor form = factory.createFormDescriptor(MockSelect.class);
        assertEquals(MockSelect.class, form.getType());

        assertEquals(1, form.getFieldDescriptors().size());

        FieldDescriptor fieldField = form.getFieldDescriptor("field");
        assertNotNull(fieldField);

        // now to configure - requires an instance.
        assertEquals(String.class, fieldField.getType());
        assertEquals("field", fieldField.getName());
        assertEquals(FieldType.SELECT, fieldField.getFieldType());
    }

    public void testMockCheckbox()
    {
        FormDescriptor form = factory.createFormDescriptor(MockCheckbox.class);
        assertEquals(MockCheckbox.class, form.getType());

        assertEquals(1, form.getFieldDescriptors().size());

        FieldDescriptor selectField = form.getFieldDescriptor("select");
        assertNotNull(selectField);

        assertEquals(FieldType.CHECKBOX, selectField.getFieldType());
        assertEquals(Boolean.class, selectField.getType());
        assertEquals("select", selectField.getName());
    }
}
