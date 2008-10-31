package com.zutubi.pulse.master.security;

import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.AccessDeniedException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 */
public class SecurityInterceptor implements MethodInterceptor
{
    private static final Logger LOG = Logger.getLogger(SecurityInterceptor.class);

    private AccessManager accessManager;

    public Object invoke(MethodInvocation methodInvocation) throws Throwable
    {
        Annotation[] annotations = methodInvocation.getMethod().getAnnotations();
        for (Annotation annotation : annotations)
        {
            if (annotation instanceof SecureParameter)
            {
                SecureParameter sp = (SecureParameter) annotation;
                secureParameter(getMethodName(methodInvocation), sp.parameterIndex(), sp.parameterType(), sp.action(), methodInvocation.getMethod().getGenericParameterTypes(), methodInvocation.getArguments());
            }
        }
        Object result = methodInvocation.proceed();

        for (Annotation annotation : annotations)
        {
            if (annotation instanceof SecureResult)
            {
                result = secureResult(((SecureResult) annotation).value(), result);
            }
        }

        return result;
    }

    void secureParameter(String methodName, int index, Class type, String action, Type[] parameterTypes, Object[] args)
    {
        if (index >= 0)
        {
            if (args.length <= index)
            {
                LOG.severe("Invalid parameter index " + index + " for securing method " + methodName);
                return;
            }

            ensureAccess(args[index], action);
        }
        else if(type == Object.class)
        {
            for(Object arg: args)
            {
                ensureAccess(arg, action);
            }
        }
        else
        {
            boolean found = false;
            for (int i = 0; i < parameterTypes.length; i++)
            {
                Type parameterType = parameterTypes[i];
                Object arg = args[i];
                if(parameterType == type)
                {
                    found = true;
                    if(arg != null)
                    {
                        ensureAccess(arg, action);
                    }
                }
                else if(parameterType instanceof ParameterizedType)
                {
                    ParameterizedType parameterizedType = (ParameterizedType) parameterType;
                    if(parameterizedType.getRawType() instanceof Collection)
                    {
                        Type collectionType = parameterizedType.getActualTypeArguments()[0];
                        if(collectionType == type)
                        {
                            found = true;
                            if(arg != null)
                            {
                                Collection c = (Collection) arg;
                                for (Object item : c)
                                {
                                    ensureAccess(item, action);
                                }
                            }
                        }
                    }
                }
                else if(parameterType instanceof GenericArrayType)
                {
                    GenericArrayType genericArrayType = (GenericArrayType) parameterType;
                    if(genericArrayType.getGenericComponentType() == type)
                    {
                        found = true;
                        if(arg != null)
                        {
                            Object[] array = (Object[]) arg;
                            for (Object item : array)
                            {
                                ensureAccess(item, action);
                            }
                        }
                    }
                }
            }

            if (!found)
            {
                LOG.severe("Invalid parameter type " + type.getName() + " for securing method " + methodName);
            }
        }
    }

    Object secureResult(String action, Object result)
    {
        if (result != null)
        {
            if (result instanceof List)
            {
                // Filter the list
                List list = (List) result;
                List toRemove = new LinkedList();

                for (Object resource : list)
                {
                    if (!checkAccess(resource, action))
                    {
                        toRemove.add(resource);
                    }
                }

                if (toRemove.size() > 0)
                {
                    List filtered = new LinkedList(list);
                    for (Object dead : toRemove)
                    {
                        filtered.remove(dead);
                    }

                    result = filtered;
                }
            }
            else if (result.getClass().isArray())
            {
                // Filter the array
                Object[] array = (Object[]) result;
                Set<Integer> toRemove = new HashSet<Integer>();
                for (int i = 0; i < array.length; i++)
                {
                    if (!checkAccess(array[i], action))
                    {
                        toRemove.add(i);
                    }
                }

                if (toRemove.size() > 0)
                {
                    Object[] filtered = (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length - toRemove.size());
                    int i = 0;
                    for (int candidate = 0; candidate < array.length; candidate++)
                    {
                        if (!toRemove.contains(candidate))
                        {
                            filtered[i++] = array[candidate];
                        }
                    }

                    result = filtered;
                }
            }
            else
            {
                ensureAccess(result, action);
            }
        }

        return result;
    }

    private void ensureAccess(Object resource, String action)
    {
        if (!checkAccess(resource, action))
        {
            throw new AccessDeniedException("Not authorised to perform action '" + action + "'");
        }
    }

    private boolean checkAccess(Object resource, String action)
    {
        return resource == null || accessManager.hasPermission(action, resource);
    }

    private String getMethodName(MethodInvocation methodInvocation)
    {
        Method method = methodInvocation.getMethod();
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }
}
