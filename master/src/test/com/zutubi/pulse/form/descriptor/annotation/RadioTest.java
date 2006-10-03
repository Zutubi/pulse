package com.zutubi.pulse.form.descriptor.annotation;

import junit.framework.TestCase;
import com.zutubi.pulse.form.descriptor.DefaultFieldDescriptor;
import com.zutubi.pulse.form.descriptor.DefaultFormDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.mock.MockRadio;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class RadioTest extends TestCase
{
    private AnnotationDecorator decorator;

    protected void setUp() throws Exception
    {
        super.setUp();

        decorator = new AnnotationDecorator();
    }

    protected void tearDown() throws Exception
    {
        decorator = null;

        super.tearDown();
    }

    // Test that the radio annotation is correctly applied.
    public void testRadioAnnotation()
    {
        DefaultFieldDescriptor field = new DefaultFieldDescriptor();
        field.setName("option");
        field.setType(String.class);

        DefaultFormDescriptor form = new DefaultFormDescriptor();
        form.setType(MockRadio.class);
        form.setFieldDescriptors(Arrays.asList((FieldDescriptor)field));

        decorator.decorate(form);

        assertEquals("radio", field.getFieldType());
        assertTrue(field.getParameters().containsKey("list"));
        String[] list = (String[]) field.getParameters().get("list");
        assertEquals("a", list[0]);
        assertEquals("b", list[1]);
        assertEquals("c", list[2]);
    }
}
