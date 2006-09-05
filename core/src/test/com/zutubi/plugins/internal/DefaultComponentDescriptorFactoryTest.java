package com.zutubi.plugins.internal;

import junit.framework.TestCase;
import nu.xom.Element;

/**
 * <class-comment/>
 */
public class DefaultComponentDescriptorFactoryTest extends TestCase
{
    private DefaultComponentDescriptorFactory descriptorFactory;

    public DefaultComponentDescriptorFactoryTest()
    {
    }

    public DefaultComponentDescriptorFactoryTest(String string)
    {
        super(string);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        descriptorFactory = new DefaultComponentDescriptorFactory();
        descriptorFactory.setObjectFactory(new DefaultObjectFactory());
        descriptorFactory.addDescriptor("test-type", MockComponentDescriptor.class);
    }

    protected void tearDown() throws Exception
    {
        descriptorFactory = null;

        super.tearDown();
    }

    public void testCreate()
    {
        MockComponentDescriptor descriptor = (MockComponentDescriptor) descriptorFactory.createComponentDescriptor("test-type", null);
        assertNotNull(descriptor);
        assertTrue(descriptor.initialised);
    }

    /**
     *
     *
     */
    protected static class MockComponentDescriptor extends ComponentDescriptorSupport
    {
        protected boolean initialised = false;

        public void init(Element element)
        {
            initialised = true;
        }
    }
}
