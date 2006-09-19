package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class PasswordComponentTest extends ComponentTestCase
{
    private PasswordComponent component;

    protected void setUp() throws Exception
    {
        super.setUp();

        component = new PasswordComponent();
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
