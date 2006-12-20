package com.zutubi.pulse.plugins.descriptors;

import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.plugins.internal.DefaultPlugin;
import com.zutubi.plugins.utils.XOMUtils;
import nu.xom.Element;
import nu.xom.ParsingException;

/**
 * <class-comment/>
 */
public class EventListenerComponentDescriptorTest extends PulseTestCase
{
    private DefaultEventManager eventManager;

    private EventListenerComponentDescriptor descriptor;

    protected void setUp() throws Exception
    {
        super.setUp();

        eventManager = new DefaultEventManager();

        descriptor = new EventListenerComponentDescriptor();
        descriptor.setEventManager(eventManager);
        descriptor.setObjectFactory(new ObjectFactory());
        descriptor.setPlugin(new DefaultPlugin());
    }

    protected void tearDown() throws Exception
    {
        descriptor = null;

        // reset the static count so that it does contaminate other tests.
        MockListener.handleEventCount = 0;

        super.tearDown();
    }

    public void testEnableAndDisableDescriptor()
    {
        assertEquals(0, MockListener.handleEventCount);

        descriptor.setListenerClassName("com.zutubi.pulse.plugins.descriptors.MockListener");
        eventManager.publish(new Event<Object>(this));
        assertEquals(0, MockListener.handleEventCount);

        descriptor.enable();

        eventManager.publish(new Event<Object>(this));
        assertEquals(1, MockListener.handleEventCount);

        descriptor.disable();

        eventManager.publish(new Event<Object>(this));
        assertEquals(1, MockListener.handleEventCount);
    }

    public void testConfiguration() throws ParsingException
    {
        Element config = XOMUtils.parseText(
                "<event-listener key=\"key\" name=\"name\" class=\"com.zutubi.pulse.plugins.descriptors.MockListener\"/>"
        ).getRootElement();

        descriptor.init(config);
        assertEquals("key", descriptor.getKey());
        assertEquals("name", descriptor.getName());
        assertEquals("com.zutubi.pulse.plugins.descriptors.MockListener", descriptor.getListenerClassName());
    }
}
