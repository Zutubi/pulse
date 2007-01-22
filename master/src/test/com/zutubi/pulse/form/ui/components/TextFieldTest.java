package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class TextFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        TextField field = new TextField();
        field.render(context, templateRenderer);

        String content = writer.toString();

        assertTrue(content.contains("<input type=\"text\" name=\"\"/>"));
        assertTrue(content.contains("<th class=\"label\" ></th>"));
    }
}
