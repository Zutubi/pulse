package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class PasswordFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        PasswordField field = new PasswordField();
        renderer.render(field);

        String content = writer.toString();

        System.out.println(content);
    }
}
