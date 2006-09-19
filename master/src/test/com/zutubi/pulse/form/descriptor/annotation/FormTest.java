package com.zutubi.pulse.form.descriptor.annotation;

import junit.framework.TestCase;
import com.zutubi.pulse.form.descriptor.DefaultFormDescriptor;
import com.zutubi.pulse.form.mock.MockRadio;
import com.zutubi.pulse.form.mock.MockForm;

import java.util.Collections;

/**
 * <class-comment/>
 */
public class FormTest extends TestCase
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

    public void testFormFieldOrder()
    {
        DefaultFormDescriptor form = new DefaultFormDescriptor();
        form.setType(MockForm.class);
        form.setFieldDescriptors(Collections.EMPTY_LIST);

        assertNull(form.getFieldOrder());

        decorator.decorate(form);

        assertNotNull(form.getFieldOrder());
        assertEquals("fieldA", form.getFieldOrder()[0]);
        assertEquals("fieldB", form.getFieldOrder()[1]);
    }
}
