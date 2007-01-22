package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class HiddenFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        HiddenField field = new HiddenField();
        field.render(context, templateRenderer);

        String content = writer.toString();

        System.out.println(content);
    }
}
