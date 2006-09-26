package com.zutubi.pulse.form.ui.components;

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
}
