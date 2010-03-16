package com.zutubi.pulse.servercore.agent;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.Properties;

public class SynchronisationTaskSupportTest extends PulseTestCase
{
    public void testSimpleTask()
    {
        final String TEST_VALUE = "param-value";

        SimpleTask task = new SimpleTask(TEST_VALUE);

        SynchronisationMessage message = task.toMessage();
        assertEquals(SynchronisationTask.Type.CLEANUP_DIRECTORY, message.getType());
        Properties arguments = message.getArguments();
        assertEquals(1, arguments.size());
        assertEquals(TEST_VALUE, arguments.get("param"));
        
        SimpleTask fromProperties = new SimpleTask(arguments);
        assertEquals(TEST_VALUE, fromProperties.param);
    }

    public void testNullValue()
    {
        SimpleTask task = new SimpleTask((String) null);
        SynchronisationMessage message = task.toMessage();
        Properties arguments = message.getArguments();
        assertEquals(0, arguments.size());

        SimpleTask fromProperties = new SimpleTask(arguments);
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

        SynchronisationMessage message = task.toMessage();
        Properties arguments = message.getArguments();
        assertEquals(5, arguments.size());

        PropertyTypesTask fromProperties = new PropertyTypesTask(arguments);
        assertEquals(INT_VALUE, fromProperties.intField);
        assertEquals(LONG_VALUE, fromProperties.longField);
        assertEquals(FLOAT_VALUE, fromProperties.floatField);
        assertEquals(DOUBLE_VALUE, fromProperties.doubleField);
        assertEquals(BOOLEAN_VALUE, fromProperties.booleanField);
    }
    
    public void testMissingPrimitives()
    {
        PropertyTypesTask task = new PropertyTypesTask(new Properties());
        assertEquals(0, task.intField);
    }

    public void testUnknownArgument()
    {
        Properties arguments = new Properties();
        arguments.put("unknown", "something");
        arguments.put("intField", "12");
        PropertyTypesTask task = new PropertyTypesTask(arguments);
        assertEquals(12, task.intField);
    }
    
    public void testUnusableFields()
    {
        Properties arguments = new UnusableFieldsTask().toMessage().getArguments();
        assertEquals(0, arguments.size());
    }
    
    public void testTransientField()
    {
        final String NORMAL_VALUE = "norm";

        TransientFieldTask task = new TransientFieldTask(NORMAL_VALUE, "trans");

        SynchronisationMessage message = task.toMessage();
        Properties arguments = message.getArguments();
        assertEquals(1, arguments.size());
        assertTrue(arguments.containsKey("normalField"));
        assertFalse(arguments.containsKey("transientField"));

        TransientFieldTask fromProperties = new TransientFieldTask(arguments);
        assertNull(fromProperties.transientField);
        assertEquals("norm", fromProperties.normalField);
    }

    public static class SimpleTask extends SynchronisationTaskSupport
    {
        private String param;

        public SimpleTask(String param)
        {
            this.param = param;
        }

        public SimpleTask(Properties arguments)
        {
            super(arguments);
        }

        public Type getType()
        {
            return Type.CLEANUP_DIRECTORY;
        }

        public void execute()
        {
        }
    }

    public static class UnusableFieldsTask extends SynchronisationTaskSupport
    {
        final int finalField = 2;
        static int staticField;
        static final int staticFinalField = 42;

        public UnusableFieldsTask()
        {
        }

        public UnusableFieldsTask(Properties arguments)
        {
            super(arguments);
        }

        public Type getType()
        {
            return Type.CLEANUP_DIRECTORY;
        }

        public void execute()
        {
        }
    }

    public static class PropertyTypesTask extends SynchronisationTaskSupport
    {
        private int intField;
        private long longField;
        private float floatField;
        private double doubleField;
        private boolean booleanField;

        public PropertyTypesTask(int intField, long longField, float floatField, double doubleField, boolean booleanField)
        {
            this.intField = intField;
            this.longField = longField;
            this.floatField = floatField;
            this.doubleField = doubleField;
            this.booleanField = booleanField;
        }

        public PropertyTypesTask(Properties arguments)
        {
            super(arguments);
        }

        public Type getType()
        {
            return Type.CLEANUP_DIRECTORY;
        }

        public void execute()
        {
        }
    }

    public static class TransientFieldTask extends SynchronisationTaskSupport
    {
        private String normalField;
        private transient String transientField;

        public TransientFieldTask(String normalField, String transientField)
        {
            this.normalField = normalField;
            this.transientField = transientField;
        }

        public TransientFieldTask(Properties arguments)
        {
            super(arguments);
        }

        public Type getType()
        {
            return Type.CLEANUP_DIRECTORY;
        }

        public void execute()
        {
        }
    }
}
