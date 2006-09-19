package com.zutubi.pulse.form.descriptor.annotation;

import junit.framework.TestCase;
import com.zutubi.pulse.form.descriptor.*;
import com.zutubi.pulse.form.descriptor.reflection.ReflectionDescriptorFactory;
import com.zutubi.pulse.form.mock.MockRadio;

import java.util.Arrays;
import java.util.Map;

/**
 * <class-comment/>
 */
public class AnnotationDecoratorTest extends TestCase
{
    private AnnotationDecorator decorator;
    private FormDescriptor form;
    private FieldDescriptor field;

    private DescriptorFactory descriptorFactory;

    protected void setUp() throws Exception
    {
        super.setUp();

        decorator = new AnnotationDecorator();
        descriptorFactory = new ReflectionDescriptorFactory();

        // use the descriptor factory, its much easier that way. Just need to remember to assert the before
        // and after values to make sure things are as expected.
        form = descriptorFactory.createFormDescriptor(AnnotatedObject.class);
        field = form.getFieldDescriptor("field");
    }

    protected void tearDown() throws Exception
    {
        decorator = null;
        descriptorFactory = null;

        form = null;
        field = null;

        super.tearDown();
    }

    public void testFieldTypeUpdated()
    {
        assertEquals("text", field.getFieldType());
        decorator.decorate(form);
        assertEquals("mock", field.getFieldType());
    }

    public void testParameters()
    {
        // check that the parameters are added as expected - no defaults...
        assertEquals(0, field.getParameters().size());
        decorator.decorate(form);

        Map<String, Object> parameters = field.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("ParameterA", parameters.get("a"));
    }

    private class AnnotatedObject
    {
        private String field;

        @MockField(a = "ParameterA") public String getField()
        {
            return field;
        }

        public void setField(String field)
        {
            this.field = field;
        }
    }
}
