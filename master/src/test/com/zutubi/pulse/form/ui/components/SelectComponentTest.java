package com.zutubi.pulse.form.ui.components;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class SelectComponentTest extends ComponentTestCase
{
    private SelectComponent component;

    protected void setUp() throws Exception
    {
        super.setUp();

        component = new SelectComponent();
    }

    protected void tearDown() throws Exception
    {
        component = null;

        super.tearDown();
    }

    public void testComponentRendering()
    {
        component.setList(Arrays.asList("plain", "html"));
        component.setName("name");
        component.render(renderer);
        assertFalse(renderer.hasError());
        System.out.println(renderer.getRenderedContent());
    }
}
