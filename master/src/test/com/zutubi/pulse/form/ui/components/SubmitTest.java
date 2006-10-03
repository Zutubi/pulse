package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class SubmitTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        Submit field = new Submit();
        renderer.render(field);

        String content = writer.toString();

        System.out.println(content);
    }
}
