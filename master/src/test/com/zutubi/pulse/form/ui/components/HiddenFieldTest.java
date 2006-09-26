package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class HiddenFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        HiddenField field = new HiddenField();
        renderer.render(field);

        String content = writer.toString();

        System.out.println(content);
    }
}
