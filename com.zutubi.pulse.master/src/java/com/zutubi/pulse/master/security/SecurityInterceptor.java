/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.security;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.logging.Logger;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.AccessDeniedException;

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
                result = secureResult(((SecureResult) annotation).value(), methodInvocation.getMethod().getReturnType(), result);
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
                else if (parameterType instanceof Class<?>)
                {
                    Class<?> clazz = (Class<?>) parameterType;
                    if (clazz.isArray() && clazz.getComponentType() == type)
                    {
                        found = true;
                        secureArray(action, arg);
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
                        secureArray(action, arg);
                    }
                }
            }

            if (!found)
            {
                LOG.severe("Invalid parameter type " + type.getName() + " for securing method " + methodName);
            }
        }
    }

    private void secureArray(String action, Object arg)
    {
        if (arg != null)
        {
            Object[] array = (Object[]) arg;
            for (Object item : array)
            {
                ensureAccess(item, action);
            }
        }
    }

    Object secureResult(String action, Class<?> resultType, Object result)
    {
        if (result != null)
        {
            if (result instanceof List)
            {
                // Filter the list
                List<?> list = (List) result;
                List<Object> toRemove = new LinkedList<Object>();

                for (Object resource : list)
                {
                    if (!checkAccess(resource, action))
                    {
                        toRemove.add(resource);
                    }
                }

                if (toRemove.size() > 0)
                {
                    List<Object> filtered = new LinkedList<Object>(list);
                    for (Object dead : toRemove)
                    {
                        filtered.remove(dead);
                    }

                    result = filtered;
                }
            }
            else if (resultType.isAssignableFrom(Iterable.class))
            {
                result = Iterables.filter((Iterable) result, new CheckAccessPredicate(action));
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

    private class CheckAccessPredicate implements Predicate<Object>
    {
        private final String action;

        CheckAccessPredicate(String action)
        {
            this.action = action;
        }

        public boolean apply(Object resource)
        {
            return checkAccess(resource, action);
        }
    }
}
