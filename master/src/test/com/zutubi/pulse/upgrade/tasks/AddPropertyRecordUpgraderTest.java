package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecordImpl;
import static org.mockito.Mockito.*;

public class AddPropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String PROPERTY_NAME = "test name";
    private static final String PROPERTY_VALUE = "test value";
    private static final String SCOPE = "scope";
    private static final String PATH = SCOPE + "/path";

    private AddPropertyRecordUpgrader upgrader;

    protected void setUp() throws Exception
    {
        super.setUp();
        upgrader = new AddPropertyRecordUpgrader(PROPERTY_NAME, PROPERTY_VALUE);
        upgrader.setScopeDetails(new ScopeDetails(SCOPE));
    }

    public void testSimpleScope()
    {
        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.get(PROPERTY_NAME));
    }

    public void testTemplatedScopeNoAncestor()
    {
        TemplatedScopeDetails scopeDetails = mock(TemplatedScopeDetails.class);
        doReturn(false).when(scopeDetails).hasAncestor(anyString());
        upgrader.setScopeDetails(scopeDetails);

        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.get(PROPERTY_NAME));
    }

    public void testTemplatedScopeHasAncestor()
    {
        TemplatedScopeDetails scopeDetails = mock(TemplatedScopeDetails.class);
        doReturn(true).when(scopeDetails).hasAncestor(anyString());
        upgrader.setScopeDetails(scopeDetails);

        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertNull(mutable.get(PROPERTY_NAME));
    }

    public void testPropertyAlreadyExists()
    {
        final String EXISTING_VALUE = "another value";

        MutableRecordImpl mutable = new MutableRecordImpl();
        mutable.put(PROPERTY_NAME, EXISTING_VALUE);
        upgrader.upgrade(PATH, mutable);
        assertEquals(EXISTING_VALUE, mutable.get(PROPERTY_NAME));
    }

    public void testInvalidValue()
    {
        try
        {
            new AddPropertyRecordUpgrader("name", new Object());
            fail("Should not be able to create an upgrader with a non-simple value");
        }
        catch(IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("only simple properties"));
        }
    }
}
