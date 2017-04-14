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

package com.zutubi.tove.type;

import com.zutubi.tove.annotations.ReadOnly;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * An implementation of the TypeProperty that represents a compile time defined property.
 * <p/>
 * A property is named 'x', with accessors getX and setX.
 * <p/>
 * A property is considered writable if it has a public setter and is not marked with
 * the readOnly annotation.  A property is considered readable if it has a public getter.
 */
public class DefinedTypeProperty extends TypeProperty
{
    private Method getter;

    private Method setter;

    protected void setGetter(Method getter)
    {
        this.getter = getter;
    }

    protected void setSetter(Method setter)
    {
        this.setter = setter;
    }

    public Object getValue(Object instance) throws IllegalAccessException, InvocationTargetException
    {
        Object result = null;
        if (getter != null)
        {
            result = getter.invoke(instance);
        }

        if (getType() instanceof PrimitiveType)
        {
            PrimitiveType primitiveType = (PrimitiveType) getType();
            Object nullValue = primitiveType.getNullValue();
            if (nullValue != null && nullValue.equals(result))
            {
                return null;
            }
        }

        return result;
    }

    public void setValue(Object instance, Object value) throws TypeException
    {
        try
        {
            if (value == null && getType() instanceof PrimitiveType)
            {
                PrimitiveType primitiveType = (PrimitiveType) getType();
                value = primitiveType.getNullValue();
            }

            // if a setter is defined, then it will be expected that the setter is used.  In the case that
            // the setter fails, we give up since to go direct to the field would be unexpected.
            if (setter != null)
            {
                setter.invoke(instance, value);
            }
            else
            {
                // should we ever end up in this sort of situation?.
            }
        }
        catch (Exception e)
        {
            throw new TypeException("Failed to set property '" + getName() + "': " + e.getMessage(), e);
        }
    }

    public boolean isReadable()
    {
        return getter != null && Modifier.isPublic(getter.getModifiers());
    }

    public boolean isWritable()
    {
        // annotation overrides all.
        if (getAnnotation(ReadOnly.class) != null)
        {
            return false;
        }

        // public setter or public field.
        return setter != null && Modifier.isPublic(setter.getModifiers());
    }
}
