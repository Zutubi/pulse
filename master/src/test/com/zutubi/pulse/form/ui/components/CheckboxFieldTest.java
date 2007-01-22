package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class CheckboxFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        CheckboxField field = new CheckboxField();
        field.render(context, templateRenderer);

        String content = writer.toString();

        assertTrue(content.contains("type=\"checkbox\""));
    }
}
