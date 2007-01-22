package com.zutubi.pulse.form.ui.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class RadioFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        RadioField field = new RadioField();
        field.setList(Arrays.asList("a", "b"));
        field.render(context, templateRenderer);

        String content = writer.toString();

        System.out.println(content);
    }

    public void testComponentRenderingWithMap() throws Exception
    {
        Map list = new HashMap();
        list.put("a", "A");
        list.put("b", "B");

        RadioField field = new RadioField();
        field.setLabel("Label");
        field.setName("name");
        field.setList(list);
        field.setContext(context);
        field.render(context, templateRenderer);

        String content = writer.toString();
        System.out.println(content);

        assertTrue(content.contains("value=\"a\""));
        assertTrue(content.contains("<label for=\"namea\">A</label>"));
        assertTrue(content.contains("value=\"b\""));
        assertTrue(content.contains("<label for=\"nameb\">B</label>"));
    }
}
