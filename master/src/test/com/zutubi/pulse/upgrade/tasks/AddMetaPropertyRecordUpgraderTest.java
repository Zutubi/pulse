package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.TemplateRecord;
import org.mockito.Mockito;
import org.mockito.Matchers;

public class AddMetaPropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String PROPERTY_NAME = "test name";
    private static final String PROPERTY_VALUE = "test value";
    private static final String NO_INHERIT_NAME = TemplateRecord.NO_INHERIT_META_KEYS[0];
    private static final String SCOPE = "scope";

    private static final String PATH = SCOPE + "/path";

    private AddMetaPropertyRecordUpgrader upgrader;

    protected void setUp() throws Exception
    {
        super.setUp();
        upgrader = new AddMetaPropertyRecordUpgrader(PROPERTY_NAME, PROPERTY_VALUE);
        upgrader.setScopeDetails(new ScopeDetails(SCOPE));
    }

    public void testSimpleScope()
    {
        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.getMeta(PROPERTY_NAME));
    }

    public void testTemplatedScopeNoAncestor()
    {
        TemplatedScopeDetails scopeDetails = Mockito.mock(TemplatedScopeDetails.class);
        Mockito.doReturn(false).when(scopeDetails).hasAncestor(Matchers.anyString());
        upgrader.setScopeDetails(scopeDetails);

        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.getMeta(PROPERTY_NAME));
    }

    public void testTemplatedScopeNoInheritNoAncestor()
    {
        upgrader = new AddMetaPropertyRecordUpgrader(NO_INHERIT_NAME, PROPERTY_VALUE);
        TemplatedScopeDetails scopeDetails = Mockito.mock(TemplatedScopeDetails.class);
        Mockito.doReturn(false).when(scopeDetails).hasAncestor(Matchers.anyString());
        upgrader.setScopeDetails(scopeDetails);

        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.getMeta(NO_INHERIT_NAME));
    }

    public void testTemplatedScopeHasAncestor()
    {
        TemplatedScopeDetails scopeDetails = Mockito.mock(TemplatedScopeDetails.class);
        Mockito.doReturn(true).when(scopeDetails).hasAncestor(Matchers.anyString());
        upgrader.setScopeDetails(scopeDetails);

        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertNull(mutable.getMeta(PROPERTY_NAME));
    }

    public void testTemplatedScopeNoInheritHasAncestor()
    {
        upgrader = new AddMetaPropertyRecordUpgrader(NO_INHERIT_NAME, PROPERTY_VALUE);
        TemplatedScopeDetails scopeDetails = Mockito.mock(TemplatedScopeDetails.class);
        Mockito.doReturn(true).when(scopeDetails).hasAncestor(Matchers.anyString());
        upgrader.setScopeDetails(scopeDetails);

        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.getMeta(NO_INHERIT_NAME));
    }

    public void testPropertyAlreadyExists()
    {
        final String EXISTING_VALUE = "another value";

        MutableRecordImpl mutable = new MutableRecordImpl();
        mutable.putMeta(PROPERTY_NAME, EXISTING_VALUE);
        upgrader.upgrade(PATH, mutable);
        assertEquals(EXISTING_VALUE, mutable.getMeta(PROPERTY_NAME));
    }
}
