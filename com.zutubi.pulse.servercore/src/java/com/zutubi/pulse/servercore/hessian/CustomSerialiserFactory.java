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

package com.zutubi.pulse.servercore.hessian;

import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;
import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.reflection.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A serialiser factory that handles Java 5 enums.
 *
 * Adapted from an implementation posted to hessian-interest by Jason Stiefel.
 */
public class CustomSerialiserFactory extends AbstractSerializerFactory
{
    private final Map<Class, Serializer> serialisers = new HashMap<Class, Serializer>();
    private final Map<Class, Deserializer> deserialisers = new HashMap<Class, Deserializer>();

    public CustomSerialiserFactory()
    {
        serialisers.put(Enum.class, new EnumSerialiser());

        deserialisers.put(Enum.class, new EnumDeserialiser());
        deserialisers.put(LogRecord.class, new CustomDeserialiser(LogRecord.class));
        deserialisers.put(Level.class, new CustomDeserialiser(Level.class));
    }

    public Serializer getSerializer(Class cl) throws HessianProtocolException
    {
        return lookup(cl, serialisers);
    }

    public Deserializer getDeserializer(Class cl)
    {
        return lookup(cl, deserialisers);
    }

    private <T> T lookup(final Class<?> cl, final Map<Class, T> map)
    {
        if (map.containsKey(cl))
        {
            return map.get(cl);
        }

        Map.Entry<Class, T> e = find(map.entrySet(), new Predicate<Map.Entry<Class, T>>()
        {
            public boolean apply(Map.Entry<Class, T> entry)
            {
                return entry.getKey().isAssignableFrom(cl);
            }
        }, null);
        if (e != null)
        {
            return e.getValue();
        }
        return null;
    }

    public void register(Class cl, Serializer serialiser, Deserializer deserialiser)
    {
        serialisers.put(cl, serialiser);
        deserialisers.put(cl, deserialiser);
    }

    public Pair<Serializer, Deserializer> deregister(Class cl)
    {
        return new Pair<Serializer, Deserializer>(serialisers.remove(cl), deserialisers.remove(cl));
    }

    /**
     * Returns a collection of classes to which the specified class is assignable.
     *
     * @param cls   the class in question
     * @return      the set of types this class is assignable to
     */
    private Set<Class> getAssignableTo(Class cls)
    {
        return ReflectionUtils.getSupertypes(cls, Object.class, false);
    }
}