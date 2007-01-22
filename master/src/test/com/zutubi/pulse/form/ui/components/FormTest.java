package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class FormTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        Form field = new Form();
        field.addNestedComponent(new TextField());
        field.addNestedComponent(new HiddenField());
        field.addNestedComponent(new Submit());
        field.render(context, templateRenderer);

        String content = writer.toString();

        assertTrue(content.contains("<form>"));
        assertTrue(content.contains("</form>"));
        assertTrue(content.contains("type=\"text\""));
        assertTrue(content.contains("type=\"hidden\""));
        assertTrue(content.contains("type=\"submit\""));
    }
}
