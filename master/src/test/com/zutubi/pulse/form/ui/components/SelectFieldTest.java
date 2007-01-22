package com.zutubi.pulse.form.ui.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class SelectFieldTest extends ComponentTestCase
{
    public void testComponentRendering() throws Exception
    {
        SelectField field = new SelectField();
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

        SelectField field = new SelectField();
        field.setContext(context);
        field.setLabel("Label");
        field.setName("name");
        field.setList(list);
        field.setValue("a");
        field.render(context, templateRenderer);

        String content = writer.toString();
        System.out.println(content);

    }

}
