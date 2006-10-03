package com.zutubi.pulse.form.ui.components;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class RadioFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        RadioField field = new RadioField();
        field.setList(Arrays.asList("a", "b"));
        renderer.render(field);

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
        renderer.render(field);

        String content = writer.toString();
        System.out.println(content);

        assertTrue(content.contains("value=\"a\""));
        assertTrue(content.contains("<label for=\"namea\">A</label>"));
        assertTrue(content.contains("value=\"b\""));
        assertTrue(content.contains("<label for=\"nameb\">B</label>"));
    }
}
