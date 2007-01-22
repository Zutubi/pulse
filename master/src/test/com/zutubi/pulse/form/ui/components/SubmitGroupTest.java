package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class SubmitGroupTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        SubmitGroup field = new SubmitGroup();
        field.addNestedComponent(new Submit());
        field.addNestedComponent(new Submit());
        field.addNestedComponent(new Submit());
        field.render(context, templateRenderer);

        String content = writer.toString();

        assertTrue(content.contains("<input type=\"submit\" class=\"submit\"/><input type=\"submit\" class=\"submit\"/><input type=\"submit\" class=\"submit\"/>"));
    }
}
