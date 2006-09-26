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
        renderer.render(field);

        String content = writer.toString();

        System.out.println(content);
    }
}
