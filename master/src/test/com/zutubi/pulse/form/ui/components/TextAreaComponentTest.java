package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class TextAreaComponentTest extends ComponentTestCase
{
    private TextAreaComponent component;

    protected void setUp() throws Exception
    {
        super.setUp();

        component = new TextAreaComponent();
    }

    protected void tearDown() throws Exception
    {
        component = null;

        super.tearDown();
    }

    public void testComponentRendering()
    {
        component.setName("name");
        component.setCols(5);
        component.setValue("initial value");
        component.render(renderer);
        assertFalse(renderer.hasError());
        System.out.println(renderer.getRenderedContent());
    }
}
