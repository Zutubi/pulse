package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class PasswordFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        PasswordField field = new PasswordField();
        field.render(context, templateRenderer);

        String content = writer.toString();

        System.out.println(content);
    }
}
