package com.cinnamonbob.core2;

import com.cinnamonbob.core2.type.Type;

import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;



/**
 * 
 *
 */
public class IntrospectionHelper
{
    /**
     *
     */
    private static final Hashtable<Class, IntrospectionHelper> HELPERS = new Hashtable<Class, IntrospectionHelper>();

    /**
     *
     */
    private static final Hashtable<Class, Class> PRIMITIVE_TYPE_MAP = new Hashtable<Class, Class>(8);

    // Set up PRIMITIVE_TYPE_MAP
    static
    {
        Class[] primitives = {Boolean.TYPE, Byte.TYPE, Character.TYPE,
                        Short.TYPE, Integer.TYPE, Long.TYPE,
                        Float.TYPE, Double.TYPE};
        Class[] wrappers = {Boolean.class, Byte.class, Character.class,
                        Short.class, Integer.class, Long.class,
                        Float.class, Double.class};
        for (int i = 0; i < primitives.length; i++)
        {
            PRIMITIVE_TYPE_MAP.put(primitives[i], wrappers[i]);
        }
    }

    /**
     *
     */
    private final Class bean;

    /**
     *
     */
    private Method addText;

    /**
     *
     */
    private Map<String, AttributeSetter> attributeSetters = new HashMap<String, AttributeSetter>();

    private Map<String, NestedCreator> nestedCreators = new HashMap<String, NestedCreator>();
    
    /**
     * Get an instance of the Introspection Helper configured for the specified
     * Class type.
     *
     * @param bean
     * @return
     */
    public synchronized static IntrospectionHelper getHelper(Class bean)
    {
        if (!HELPERS.containsKey(bean))
        {
            HELPERS.put(bean, new IntrospectionHelper(bean));
        }
        return HELPERS.get(bean);
    }

    /**
     * Private constructor. Handle the initialisation of this instance of the
     * IntrospectionHelper
     *
     * @param bean
     */
    private IntrospectionHelper(final Class bean)
    {
        this.bean = bean;

        // initialise helper:
        Method[] methods = this.bean.getMethods();
        for (final Method method : methods)
        {

            String name = method.getName();
            Class[] paramTypes = method.getParameterTypes();
            Class returnType = method.getReturnType();

            if (name.equals("addText") &&
                        Void.TYPE.equals(returnType) &&
                        paramTypes.length == 1 &&
                        String.class.equals(paramTypes[0]))
            {
                addText = method;
            }

            // b) construct attribute setters.
            if (name.startsWith("set") &&
                        Void.TYPE.equals(returnType) &&
                        paramTypes.length == 1)
            {
                String attribName = getPropertyName(name, "set");
                AttributeSetter setter = createAttributeSetter(method, paramTypes[0], attribName);
                if (setter != null)
                {
                    attributeSetters.put(attribName, setter);
                }                
            }
            
            if (name.startsWith("create") && 
                    paramTypes.length == 0 && 
                    !Void.TYPE.equals(returnType))
            {
                String attribName = getPropertyName(name, "create");
                nestedCreators.put(attribName, new NestedCreator()
                {
                    public Object create(Object parent) 
                            throws InvocationTargetException, IllegalAccessException
                    {
                        return method.invoke(parent);
                    }
                });                
            }
        }
    }

    /**
     * Create a new implementation of the attribute setter interface, one that
     * handles the data conversion appropriately.
     *
     * @param method
     * @param arg
     * @return
     */
    private AttributeSetter createAttributeSetter(final Method method,
                                                  Class arg,
                                                  String attributeName)
    {

        // Simplify things by treating primities like there wrapper classes.
        // Javas introspection does not mind.
        final Class reflectedArg = PRIMITIVE_TYPE_MAP.containsKey(arg)
                ? (Class) PRIMITIVE_TYPE_MAP.get(arg) : arg;

        // String argument requires no conversion.
        if (String.class.equals(reflectedArg))
        {
            return new AttributeSetter()
            {
                public void set(Project project, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException
                {
                    method.invoke(parent, value);
                }
            };

        }
        // Boolean argument uses custom conversion.
        else if (Boolean.class.equals(reflectedArg))
        {
            return new AttributeSetter()
            {
                public void set(Project project, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException
                {
                    method.invoke(parent, toBoolean(value));
                }

            };

        }
        // char and Character get special treatment - take the first character
        else if (Character.class.equals(reflectedArg))
        {
            return new AttributeSetter()
            {
                public void set(Project project, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException
                {
                    if (value.length() == 0)
                    {
                        throw new RuntimeException();
                    }
                    method.invoke(parent, value.charAt(0));
                }

            };
        }
        // Class argument uses Class.forName() conversion.
        else if (Class.class.equals(reflectedArg))
        {
            return new AttributeSetter()
            {
                public void set(Project project, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException
                {
                    try
                    {
                        method.invoke(parent, Class.forName(value));
                    } catch (ClassNotFoundException ce)
                    {
                        throw new InvocationTargetException(ce);
                    }
                }
            };
        }
        else if (Type.class.isAssignableFrom(reflectedArg))
        {
            return new AttributeSetter()
            {
                public void set(Project project, Object parent, String value) 
                        throws InvocationTargetException, IllegalAccessException
                {
                    // lookup the type object within the projects references.
                    Object obj = project.getReference(value);                    
                    if (obj != null && reflectedArg.isAssignableFrom(obj.getClass()))
                    {
                        method.invoke(parent, obj);
                    }                    
                }
            };
        }
        // The default conversion is using the single string argument constructor, if it
        // exists. This is how most of the primitive types will be handled.
        else
        {
            try
            {
                final Constructor c = reflectedArg.getConstructor(new Class[]{String.class});

                return new AttributeSetter()
                {
                    public void set(Project project, Object parent, String value) throws InvocationTargetException, IllegalAccessException
                    {
                        try
                        {
                            Object attribute = c.newInstance(value);
                            method.invoke(parent, attribute);
                        } catch (InstantiationException ie)
                        {
                            throw new InvocationTargetException(ie);
                        }
                    }
                };
            } catch (NoSuchMethodException nme)
            {
                // constructor does not exist.
            }
        }

        return null;
    }

    /**
     * Set the value of the specified attribute on the object.
     *
     * @param obj
     * @param attribute
     * @param value
     * @throws IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void set(Project project, Object obj, String attribute, String value)
            throws IllegalAccessException, InvocationTargetException
    {
        assertObjectType(obj);

        if (!attributeSetters.containsKey(attribute))
        {
            throw new IllegalArgumentException("Setter for attribute '" + attribute +
                    "' does not exist on object of type " + bean.getName());
        }

        AttributeSetter setter = attributeSetters.get(attribute);
        setter.set(project, obj, value);
    }

    public Object create(Project project, Object obj, String attribute) 
            throws IllegalAccessException, InvocationTargetException
    {
        assertObjectType(obj);
        if (!nestedCreators.containsKey(attribute))
        {
            throw new IllegalArgumentException("Creator for attribute '" + attribute +
                "' does not exist on object of type " + bean.getName());
        }
        NestedCreator creator = nestedCreators.get(attribute);
        return creator.create(obj);
    }
    
    /**
     * Convert the specified method name into its associated property name.
     * NOTE: This method does not follow the full standard bean conventions.
     *
     * @param methodName
     * @return
     */
    private String getPropertyName(String methodName, String prefix)
    {
        // cut of the set, lowercase the first letter.
        return methodName.substring(prefix.length(), prefix.length() + 1).toLowerCase() + methodName.substring(prefix.length() + 1);
    }

    /**
     * Internal interface used for setting element attributes. It is
     * intended that concrete implementations of this interface understand
     * how to convert the string attribute value into the required attribute
     * object type.
     */
    private interface AttributeSetter
    {
        /**
         * @param project
         * @throws java.lang.reflect.InvocationTargetException
         * @throws IllegalAccessException
         * @param parent
         * @param value
         */
        void set(Project project, Object parent, String value)
                throws InvocationTargetException, IllegalAccessException;
    }

    /**
     * 
     */
    private interface NestedCreator {
        Object create(Object parent)
            throws InvocationTargetException, IllegalAccessException;
    }
    
    
    public void addText(Object o, String text) throws IllegalAccessException, InvocationTargetException
    {
        assertObjectType(o);
        if (addText == null)
        {
            throw new IllegalArgumentException();
        }
        addText.invoke(o, text);
    }

    public boolean canAddText()
    {
        return addText != null;
    }
    
    /**
     * Custom data conversion for string to boolean. Expand on default
     * conversion and include on and yes.
     *
     * @param str
     * @return will return true if the specified string is 'on', 'true' or 'yes'
     *         and false otherwise.
     */
    public static boolean toBoolean(String str)
    {
        return (str.equalsIgnoreCase("on") ||
                       str.equalsIgnoreCase("true") ||
                       str.equalsIgnoreCase("yes"));
    }

    /**
     * Check that the object is of the same type as the Class used to setup
     * this instance of the introspection helper.
     *
     * @param obj
     * @throws IllegalArgumentException if a mismatch is detected.
     */
    private void assertObjectType(Object obj)
    {
        if (obj == null)
        {
            throw new IllegalArgumentException();
        }
        if (!obj.getClass().equals(bean))
        {
            throw new IllegalArgumentException();
        }
    }
}
