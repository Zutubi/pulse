package com.zutubi.pulse.form.ui.components;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class SelectFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        SelectField field = new SelectField();
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

        SelectField field = new SelectField();
        field.setLabel("Label");
        field.setName("name");
        field.setList(list);
        field.setValue("a");
        renderer.render(field);

        String content = writer.toString();
        System.out.println(content);

    }

}
