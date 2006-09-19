package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class CheckboxComponentTest extends ComponentTestCase
{
    private CheckboxComponent component;

    protected void setUp() throws Exception
    {
        super.setUp();

        component = new CheckboxComponent();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testComponentRendering()
    {
        component.setName("name");
        component.setLabel("name.label");
        component.setValue("true"); // checked..
        component.render(renderer);
        assertFalse(renderer.hasError());
        System.out.println(renderer.getRenderedContent());
    }
}
