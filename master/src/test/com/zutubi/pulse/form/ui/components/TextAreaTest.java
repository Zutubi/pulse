package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class TextAreaTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        TextArea field = new TextArea();
        renderer.render(field);

        String content = writer.toString();
        System.out.println(content);
    }
}
