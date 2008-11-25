package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.engine.api.ReferenceMap;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.squeezers.BooleanSqueezer;

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

    private Method setText;

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
        // initialise helper:
        Method[] methods = bean.getMethods();
        for (final Method method : methods)
        {

            String name = method.getName();
            Class[] paramTypes = method.getParameterTypes();
            Class returnType = method.getReturnType();

            if (name.equals("setText") &&
                    Void.TYPE.equals(returnType) &&
                    paramTypes.length == 1 &&
                    String.class.equals(paramTypes[0]))
            {
                setText = method;
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

            if (name.startsWith("add") && Void.TYPE.equals(returnType))
            {
                if(paramTypes.length == 1)
                {
                    if (name.equals("add"))
                    {
                        // type adder.
                        NestedAdder adder = new NestedAdder() {
                            public void add(Object parent, Object arg, Scope scope) throws InvocationTargetException, IllegalAccessException
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
                            public void add(Object parent, Object arg, Scope scope) throws InvocationTargetException, IllegalAccessException
                            {
                                method.invoke(parent, arg);
                            }
                        });
                    }
                }
                else if(paramTypes.length == 2 && paramTypes[1].isAssignableFrom(Scope.class))
                {
                    if (name.equals("add"))
                    {
                        // type adder.
                        NestedAdder adder = new NestedAdder() {
                            public void add(Object parent, Object arg, Scope scope) throws InvocationTargetException, IllegalAccessException
                            {
                                method.invoke(parent, arg, scope);
                            }
                        };
                        nestedTypeAdders.put(paramTypes[0], adder);
                    }
                    else
                    {
                        String attributeName = getPropertyName(name, "add");
                        nestedAdders.put(attributeName, new NestedAdder(){
                            public void add(Object parent, Object arg, Scope scope) throws InvocationTargetException, IllegalAccessException
                            {
                                method.invoke(parent, arg, scope);
                            }

                        });
                    }
                }
            }
        }
    }

    public boolean hasSetText()
    {
        return setText != null;
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
        void set(Object parent, String value, ReferenceResolver.ResolutionStrategy resolutionStrategy, ReferenceMap referenceMap)
                throws InvocationTargetException, IllegalAccessException, FileLoadException, ResolutionException;
    }

    /**
     *
     */
    private interface NestedAdder
    {
        public void add(Object parent, Object arg, Scope scope) throws InvocationTargetException, IllegalAccessException;
    }

     // NOTE: This method does not follow the standard bean conventions.
    private String getPropertyName(String methodName, String prefix)
    {
        // cut of the set, lowercase the first letter.
        return methodName.substring(prefix.length(), prefix.length() + 1).toLowerCase() + methodName.substring(prefix.length() + 1);
    }

    private AttributeSetter createAttributeSetter(final Method method, Class arg, final Map<String, Class> typeDefinitions)
    {
        // Simplify things by treating primities like there wrapper classes.
        // Javas introspection does not mind.
        final Class reflectedArg = PRIMITIVE_TYPE_MAP.containsKey(arg) ? PRIMITIVE_TYPE_MAP.get(arg) : arg;

        if (String.class.equals(reflectedArg))
        {
            // String argument requires no conversion.
            return new AttributeSetter()
            {
                public void set(Object parent, String value, ReferenceResolver.ResolutionStrategy resolutionStrategy, ReferenceMap referenceMap)
                        throws InvocationTargetException, IllegalAccessException, FileLoadException, ResolutionException
                {
                    method.invoke(parent, ReferenceResolver.resolveReferences(value, referenceMap, resolutionStrategy));
                }
            };
        }
        else if (Boolean.class.equals(reflectedArg))
        {
            // Boolean argument uses custom conversion.
            return new AttributeSetter()
            {
                public void set(Object parent, String value, ReferenceResolver.ResolutionStrategy resolutionStrategy, ReferenceMap referenceMap)
                        throws InvocationTargetException, IllegalAccessException
                {
                    method.invoke(parent, toBoolean(value));
                }

            };

        }
        else if (Character.class.equals(reflectedArg))
        {
            // char and Character get special treatment - take the first character
            return new AttributeSetter()
            {
                public void set(Object parent, String value, ReferenceResolver.ResolutionStrategy resolutionStrategy, ReferenceMap referenceMap)
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
        else if (Class.class.equals(reflectedArg))
        {
            // Class argument uses Class.forName() conversion.
            return new AttributeSetter()
            {
                public void set(Object parent, String value, ReferenceResolver.ResolutionStrategy resolutionStrategy, ReferenceMap referenceMap)
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
                public void set(Object parent, String value, ReferenceResolver.ResolutionStrategy resolutionStrategy, ReferenceMap referenceMap)
                        throws InvocationTargetException, IllegalAccessException, FileLoadException, ResolutionException
                {
                    // lookup the type object within the projects references.
                    Object obj = ReferenceResolver.resolveReference(value, referenceMap);

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
        else if(List.class.equals(reflectedArg))
        {
            return new AttributeSetter()
            {
                public void set(Object parent, String value, ReferenceResolver.ResolutionStrategy resolutionStrategy, ReferenceMap referenceMap)
                        throws InvocationTargetException, IllegalAccessException, FileLoadException, ResolutionException
                {
                    method.invoke(parent, ReferenceResolver.splitAndResolveReferences(value, referenceMap, resolutionStrategy));
                }
            };
        }
        else
        {
            // The default conversion is using the single string argument constructor, if it
            // exists. This is how most of the primitive types will be handled.
            try
            {
                final Constructor c = reflectedArg.getConstructor(new Class[]{String.class});

                return new AttributeSetter()
                {
                    public void set(Object parent, String value, ReferenceResolver.ResolutionStrategy resolutionStrategy, ReferenceMap referenceMap) throws InvocationTargetException, IllegalAccessException, FileLoadException, ResolutionException
                    {
                        try
                        {
                            Object attribute = c.newInstance(ReferenceResolver.resolveReferences(value, referenceMap, resolutionStrategy));
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

    public void set(String name, Object parent, String value, ReferenceResolver.ResolutionStrategy resolutionStrategy, ReferenceMap referenceMap)
            throws IllegalAccessException, InvocationTargetException, ParseException, FileLoadException, ResolutionException
    {
        AttributeSetter setter = attributeSetters.get(name);
        if (setter == null)
        {
            throw new UnknownAttributeException("Unrecognised attribute '" + name + "'.");
        }
        attributeSetters.get(name).set(parent, value, resolutionStrategy, referenceMap);
    }
    
    public void add(String name, Object parent, Object arg, Scope scope) throws IllegalAccessException, InvocationTargetException
    {
        nestedAdders.get(name).add(parent, arg, scope);
    }
    
    public void add(Object parent, Object arg, Scope scope) throws IllegalAccessException, InvocationTargetException
    {
        List<NestedAdder> adders = getTypeAdders(arg.getClass());
        for (NestedAdder adder: adders)
        {
            adder.add(parent, arg, scope);
        }
    }
    
    public void setText(Object parent, String txt) throws IllegalAccessException, InvocationTargetException
    {
        setText.invoke(parent, txt);
    }

    /**
     * Custom data conversion for string to boolean. Expand on default
     * conversion and include on and yes.
     *
     * @param str string to convert
     * @return will return true if the specified string is 'on', 'true' or 'yes'
     *         and false otherwise.
     */
    public static boolean toBoolean(String str)
    {
        try
        {
            return new BooleanSqueezer().unsqueeze(str);
        }
        catch (SqueezeException e)
        {
            return false;
        }
    }
    
}
