package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class HiddenComponentTest extends ComponentTestCase
{
    private HiddenComponent component;

    protected void setUp() throws Exception
    {
        super.setUp();

        component = new HiddenComponent();
    }

    protected void tearDown() throws Exception
    {
        component = null;

        super.tearDown();
    }

    public void testComponentRendering()
    {
        component.render(renderer);
        assertFalse(renderer.hasError());
    }
}
