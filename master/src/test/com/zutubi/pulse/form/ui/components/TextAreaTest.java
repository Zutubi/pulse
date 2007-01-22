package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class TextAreaTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        TextArea field = new TextArea();
        field.render(context, templateRenderer);

        String content = writer.toString();
        assertTrue(content.contains("<textarea name=\"\"></textarea>"));
    }
}
