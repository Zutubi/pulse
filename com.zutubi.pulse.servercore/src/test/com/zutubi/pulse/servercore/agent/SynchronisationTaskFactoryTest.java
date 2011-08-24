package com.zutubi.pulse.servercore.agent;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.zutubi.util.FileSystemUtils.encodeFilenameComponent;

public class SynchronisationTaskFactoryTest extends PulseTestCase
{
    private static final String TYPE_MAP = "MAP";
    private static final String TYPE_PROPERTY_TYPES = "PROPERTY_TYPES";
    private static final String TYPE_SIMPLE = "SIMPLE";
    private static final String TYPE_TRANSIENT = "TRANSIENT";
    private static final String TYPE_UNUSABLE_FIELDS = "UNUSABLE_FIELDS";

    private SynchronisationTaskFactory synchronisationTaskFactory;

    @Override
    protected void setUp() throws Exception
    {
        synchronisationTaskFactory = new SynchronisationTaskFactory();
        synchronisationTaskFactory.setObjectFactory(new DefaultObjectFactory());
        SynchronisationTaskFactory.registerType(TYPE_MAP, MapTask.class);
        SynchronisationTaskFactory.registerType(TYPE_PROPERTY_TYPES, PropertyTypesTask.class);
        SynchronisationTaskFactory.registerType(TYPE_SIMPLE, SimpleTask.class);
        SynchronisationTaskFactory.registerType(TYPE_TRANSIENT, TransientFieldTask.class);
        SynchronisationTaskFactory.registerType(TYPE_UNUSABLE_FIELDS, UnusableFieldsTask.class);
    }

    public void testSimpleTask()
    {
        simpleValueHelper("param-value");
    }

    public void testEncodedString()
    {
        simpleValueHelper(encodeFilenameComponent("path value!"));
    }

    private void simpleValueHelper(String testValue)
    {
        SimpleTask task = new SimpleTask(testValue);

        SynchronisationMessage message = synchronisationTaskFactory.toMessage(task);
        assertEquals(TYPE_SIMPLE, message.getTypeName());
        Properties arguments = message.getArguments();
        assertEquals(1, arguments.size());
        assertEquals(testValue, arguments.get("param"));

        SimpleTask fromProperties = (SimpleTask) synchronisationTaskFactory.fromMessage(message);
        assertEquals(testValue, fromProperties.param);
    }

    public void testNullValue()
    {
        SimpleTask task = new SimpleTask(null);
        SynchronisationMessage message = synchronisationTaskFactory.toMessage(task);
        Properties arguments = message.getArguments();
        assertEquals(0, arguments.size());

        SimpleTask fromProperties = (SimpleTask) synchronisationTaskFactory.fromMessage(message);
        assertNull(fromProperties.param);
    }
    
    public void testPropertyTypesTask()
    {
        final int INT_VALUE = 22;
        final long LONG_VALUE = 1111234567890L;
        final float FLOAT_VALUE = 1.1f;
        final double DOUBLE_VALUE = 6.6e-7f;
        final boolean BOOLEAN_VALUE = true;

        PropertyTypesTask task = new PropertyTypesTask(INT_VALUE, LONG_VALUE, FLOAT_VALUE, DOUBLE_VALUE, BOOLEAN_VALUE);

        SynchronisationMessage message = synchronisationTaskFactory.toMessage(task);
        Properties arguments = message.getArguments();
        assertEquals(5, arguments.size());

        PropertyTypesTask fromProperties = (PropertyTypesTask) synchronisationTaskFactory.fromMessage(message);
        assertEquals(INT_VALUE, fromProperties.intField);
        assertEquals(LONG_VALUE, fromProperties.longField);
        assertEquals(FLOAT_VALUE, fromProperties.floatField);
        assertEquals(DOUBLE_VALUE, fromProperties.doubleField);
        assertEquals(BOOLEAN_VALUE, fromProperties.booleanField);
    }
    
    public void testMapTask()
    {
        final Map<String, String> TEST_MAP = new HashMap<String, String>();
        TEST_MAP.put("key", "value");
        TEST_MAP.put("encoded", encodeFilenameComponent("path value!"));
        TEST_MAP.put(encodeFilenameComponent("encoded key!"), "non-encoded value");

        MapTask task = new MapTask(TEST_MAP);
        SynchronisationMessage message = synchronisationTaskFactory.toMessage(task);
        Properties arguments = message.getArguments();
        assertEquals(1, arguments.size());

        MapTask fromProperties = (MapTask) synchronisationTaskFactory.fromMessage(message);
        assertEquals(TEST_MAP, fromProperties.map);
    }

    public void testMissingPrimitives()
    {
        SynchronisationMessage message = new SynchronisationMessage(SynchronisationTask.Type.TEST.name(), new Properties());
        TestSynchronisationTask task = (TestSynchronisationTask) synchronisationTaskFactory.fromMessage(message);
        assertEquals(false, task.isSucceed());
    }

    public void testUnknownArgument()
    {
        final int INT_VALUE = 12;
        PropertyTypesTask task = new PropertyTypesTask(INT_VALUE, 0L, 0.0f, 0.0d, false);

        SynchronisationMessage message = synchronisationTaskFactory.toMessage(task);
        Properties arguments = message.getArguments();
        arguments.put("unknown", "something");
        arguments.put("intField", "12");
        PropertyTypesTask fromMessage = (PropertyTypesTask) synchronisationTaskFactory.fromMessage(message);
        assertEquals(12, fromMessage.intField);
    }
    
    public void testUnusableFields()
    {
        SynchronisationMessage message = synchronisationTaskFactory.toMessage(new UnusableFieldsTask());
        assertEquals(0, message.getArguments().size());
    }
    
    public void testTransientField()
    {
        final String NORMAL_VALUE = "norm";

        TransientFieldTask task = new TransientFieldTask(NORMAL_VALUE, "trans");

        SynchronisationMessage message = synchronisationTaskFactory.toMessage(task);
        Properties arguments = message.getArguments();
        assertEquals(1, arguments.size());
        assertTrue(arguments.containsKey("normalField"));
        assertFalse(arguments.containsKey("transientField"));

        TransientFieldTask fromProperties = (TransientFieldTask) synchronisationTaskFactory.fromMessage(message);
        assertNull(fromProperties.transientField);
        assertEquals("norm", fromProperties.normalField);
    }

    public static class SimpleTask implements SynchronisationTask
    {
        private String param;

        public SimpleTask()
        {
        }

        public SimpleTask(String param)
        {
            this.param = param;
        }

        public void execute()
        {
        }
    }

    public static class UnusableFieldsTask implements SynchronisationTask
    {
        final int finalField = 2;
        static int staticField;
        static final int staticFinalField = 42;

        public UnusableFieldsTask()
        {
        }

        public void execute()
        {
        }
    }

    public static class PropertyTypesTask implements SynchronisationTask
    {
        private int intField;
        private long longField;
        private float floatField;
        private double doubleField;
        private boolean booleanField;

        public PropertyTypesTask()
        {
        }

        public PropertyTypesTask(int intField, long longField, float floatField, double doubleField, boolean booleanField)
        {
            this.intField = intField;
            this.longField = longField;
            this.floatField = floatField;
            this.doubleField = doubleField;
            this.booleanField = booleanField;
        }

        public void execute()
        {
        }
    }

    public static class MapTask implements SynchronisationTask
    {
        private Map<String, String> map;

        public MapTask()
        {
        }

        public MapTask(Map<String, String> map)
        {
            this.map = map;
        }

        public void execute()
        {
        }
    }

    public static class TransientFieldTask implements SynchronisationTask
    {
        private String normalField;
        private transient String transientField;

        public TransientFieldTask()
        {
        }

        public TransientFieldTask(String normalField, String transientField)
        {
            this.normalField = normalField;
            this.transientField = transientField;
        }

        public void execute()
        {
        }
    }
}
