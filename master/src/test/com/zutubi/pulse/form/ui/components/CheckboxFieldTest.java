package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class CheckboxFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        CheckboxField field = new CheckboxField();
        renderer.render(field);

        String content = writer.toString();

        System.out.println(content);
    }
}
