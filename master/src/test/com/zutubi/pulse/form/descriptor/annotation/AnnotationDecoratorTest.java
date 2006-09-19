package com.zutubi.pulse.form.descriptor.annotation;

import junit.framework.TestCase;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.DefaultFormDescriptor;
import com.zutubi.pulse.form.descriptor.DefaultFieldDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.mock.MockRadio;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class AnnotationDecoratorTest extends TestCase
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

    public void testFieldTypeUpdated()
    {
        //TODO: test that the Field annotation is applied and that it correctly updates the fieldType value.
    }

    public void testParameters()
    {
        //TODO: test that arbitrary parameters are captured and are correctly added into the parameters list.
    }
}
