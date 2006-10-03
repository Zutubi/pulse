package com.zutubi.pulse.form.ui.components;

import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.DelegatingValidationContext;

/**
 * <class-comment/>
 */
public class FormTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        Form field = new Form();
        field.addNestedComponent(new TextField());
        field.addNestedComponent(new HiddenField());
        field.addNestedComponent(new Submit());
        renderer.render(field);

        String content = writer.toString();

        System.out.println(content);
    }

    public void testRenderingFormWithValidationErrors() throws Exception
    {
        ValidationContext context = new DelegatingValidationContext(null);
        context.addFieldError("field", "field error");
        templateRenderer.setValidationContext(context);

        TextField field = new TextField();
        field.setName("field");
        Form form = new Form();
        form.setId("form");
        form.addNestedComponent(field);

        renderer.render(form);

        String content = writer.toString();

        System.out.println(content);
    }
}
