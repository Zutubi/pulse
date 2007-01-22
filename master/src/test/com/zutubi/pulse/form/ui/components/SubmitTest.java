package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class SubmitTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        Submit field = new Submit();
        field.render(context, templateRenderer);

        String content = writer.toString();
        assertTrue(content.contains("type=\"submit\""));
    }
}
