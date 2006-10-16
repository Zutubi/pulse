package com.zutubi.pulse.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 *
 */
public class IntrospectionHelper
{
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

    private final Class bean;

    private Method addText;

    /**
     *
     */
    private final Map<String, NestedCreator> nestedCreators = new HashMap<String, NestedCreator>();

    /**
     *
     */
    private final Map<String, NestedAdder> nestedAdders = new HashMap<String, NestedAdder>();

    /**
     *
     */
    private final Map<String, AttributeSetter> attributeSetters = new HashMap<String, AttributeSetter>();

    /**
     *
     */
    private final Map<Class, NestedAdder> nestedTypeAdders = new HashMap<Class, NestedAdder>();

    /**
     *
     */
    private static final Map<Class, IntrospectionHelper> helpers = new HashMap<Class, IntrospectionHelper>();

    public static IntrospectionHelper getHelper(Class type, Map<String, Class> typeDefinitions)
    {
        if (!helpers.containsKey(type))
        {
            helpers.put(type, new IntrospectionHelper(type, typeDefinitions));
        }
        return helpers.get(type);
    }

    private IntrospectionHelper(final Class bean, Map<String, Class> typeDefinitions)
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
                continue;
            }

            // b) construct attribute setters.
            if (name.startsWith("set") &&
                    Void.TYPE.equals(returnType) &&
                    paramTypes.length == 1)
            {
                String attribName = getPropertyName(name, "set");
                AttributeSetter setter = createAttributeSetter(method, paramTypes[0], typeDefinitions);
                if (setter != null)
                {
                    attributeSetters.put(attribName, setter);
                }
                continue;
            }

            if (name.startsWith("create") &&
                    paramTypes.length == 0 &&
                    !Void.TYPE.equals(returnType))
            {
                String attribName = getPropertyName(name, "create");
                nestedCreators.put(attribName, new NestedCreator() {
                    public Object create(Object parent)
                            throws InvocationTargetException, IllegalAccessException
                    {
                        return method.invoke(parent);
                    }
                });
                continue;
            }

            if (name.startsWith("add") &&
                    paramTypes.length == 1 &&
                    Void.TYPE.equals(returnType))
            {
                if (name.equals("add"))
                {
                    // type adder.
                    NestedAdder adder = new NestedAdder() {
                        public void add(Object parent, Object arg) throws InvocationTargetException, IllegalAccessException
                        {
                            method.invoke(parent, arg);
                        }
                    };
                    nestedTypeAdders.put(paramTypes[0], adder);
                }
                else
                {
                    String attributeName = getPropertyName(name, "add");
                    nestedAdders.put(attributeName, new NestedAdder(){
                        public void add(Object parent, Object arg) throws InvocationTargetException, IllegalAccessException
                        {
                            method.invoke(parent, arg);
                        }

                    });
                }
            }
        }
    }

    public boolean hasAddText()
    {
        return addText != null;
    }

    /**
     *
     */
    private interface NestedCreator
    {
        public Object create(Object parent) throws InvocationTargetException, IllegalAccessException;
    }

    /**
     *
     */
    private interface AttributeSetter
    {
        void set(Object parent, String value, boolean resolveReferences, Scope scope)
                throws InvocationTargetException, IllegalAccessException, FileLoadException;
    }

    /**
     *
     */
    private interface NestedAdder
    {
        public void add(Object parent, Object arg) throws InvocationTargetException, IllegalAccessException;
    }

    /**
     * Convert the specified method name into its associated property name.
     * NOTE: This method does not follow the standard bean conventions.
     *
     * @param methodName
     */
    private String getPropertyName(String methodName, String prefix)
    {
        // cut of the set, lowercase the first letter.
        return methodName.substring(prefix.length(), prefix.length() + 1).toLowerCase() + methodName.substring(prefix.length() + 1);
    }

    /**
     *
     * @param method
     * @param arg
     */
    private AttributeSetter createAttributeSetter(final Method method, Class arg, final Map<String, Class> typeDefinitions)
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
                public void set(Object parent, String value, boolean resolveReferences, Scope scope)
                        throws InvocationTargetException, IllegalAccessException, FileLoadException
                {
                    method.invoke(parent, resolveReferences ? VariableHelper.replaceVariables(value, scope) : value);
                }
            };

        }
        // Boolean argument uses custom conversion.
        else if (Boolean.class.equals(reflectedArg))
        {
            return new AttributeSetter()
            {
                public void set(Object parent, String value, boolean resolveReferences, Scope scope)
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
                public void set(Object parent, String value, boolean resolveReferences, Scope scope)
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
                public void set(Object parent, String value, boolean resolveReferences, Scope scope)
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
        else if (Reference.class.isAssignableFrom(reflectedArg))
        {
            return new AttributeSetter()
            {
                public void set(Object parent, String value, boolean resolveReferences, Scope scope)
                        throws InvocationTargetException, IllegalAccessException, FileLoadException
                {
                    // lookup the type object within the projects references.
                    Object obj = VariableHelper.replaceVariable(value, scope);

                    if (!reflectedArg.isAssignableFrom(obj.getClass()))
                    {
                        List<String> expectedTypes = getAssignablesForType(typeDefinitions, reflectedArg);
                        String gotType = getNameForType(typeDefinitions, obj.getClass());

                        throw new FileLoadException("Referenced property '" + value + "' has unexpected type (expected one of " + expectedTypes + ", got " + gotType + ").");
                    }

                    method.invoke(parent, obj);
                }

                private List<String> getAssignablesForType(Map<String, Class> typeDefinitions, Class clazz)
                {
                    List<String> result = new LinkedList<String>();

                    for(Map.Entry<String, Class> e: typeDefinitions.entrySet())
                    {
                        if(clazz.isAssignableFrom(e.getValue()))
                        {
                            result.add(e.getKey());
                        }
                    }

                    return result;
                }

                private String getNameForType(Map<String, Class> typeDefinitions, Class clazz)
                {
                    for(Map.Entry<String, Class> e: typeDefinitions.entrySet())
                    {
                        if(e.getValue() == clazz)
                        {
                            return e.getKey();
                        }
                    }

                    return "<unknown>";
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
                    public void set(Object parent, String value, boolean resolveReferences, Scope scope) throws InvocationTargetException, IllegalAccessException, FileLoadException
                    {
                        try
                        {
                            Object attribute = c.newInstance(resolveReferences ? VariableHelper.replaceVariables(value, scope) : value);
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

    public boolean hasCreate(String name)
    {
        return nestedCreators.containsKey(name);
    }

    public boolean hasAdd(String name)
    {
        return nestedAdders.containsKey(name);
    }

    public boolean canAdd(Class type)
    {
        return getTypeAdders(type).size() > 0;
    }

    private List<NestedAdder> getTypeAdders(Class type)
    {
        List<NestedAdder> availableAdders = new LinkedList<NestedAdder>();
        for (Class c : nestedTypeAdders.keySet())
        {
            if (c.isAssignableFrom(type))
            {
                availableAdders.add(nestedTypeAdders.get(c));
            }
        }
        return availableAdders;
    }

    public boolean hasSetter(String name)
    {
        return attributeSetters.containsKey(name);
    }

    public Object create(String name, Object parent) throws IllegalAccessException, InvocationTargetException
    {
        return nestedCreators.get(name).create(parent);
    }

    public void set(String name, Object parent, String value, boolean resolveReferences, Scope scope)
            throws IllegalAccessException, InvocationTargetException, ParseException, FileLoadException
    {
        AttributeSetter setter = attributeSetters.get(name);
        if (setter == null)
        {
            throw new UnknownAttributeException("Unrecognised attribute '" + name + "'.");
        }
        attributeSetters.get(name).set(parent, value, resolveReferences, scope);
    }
    
    public void add(String name, Object parent, Object arg) throws IllegalAccessException, InvocationTargetException
    {
        nestedAdders.get(name).add(parent, arg);
    }
    
    public void add(Object parent, Object arg) throws IllegalAccessException, InvocationTargetException
    {
        List<NestedAdder> adders = getTypeAdders(arg.getClass());
        for (NestedAdder adder: adders)
        {
            adder.add(parent, arg);            
        }
    }
    
    public void addText(Object parent, String txt) throws IllegalAccessException, InvocationTargetException
    {
        addText.invoke(parent, txt);
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
    
}
