package com.zutubi.pulse.servercore.hessian;

import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
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

    private <T> T lookup(final Class cl, final Map<Class, T> map)
    {
        if (map.containsKey(cl))
        {
            return map.get(cl);
        }

        Map.Entry<Class, T> e = CollectionUtils.find(map.entrySet(), new Predicate<Map.Entry<Class, T>>()
        {
            public boolean satisfied(Map.Entry<Class, T> entry)
            {
                return entry.getKey().isAssignableFrom(cl);
            }
        });
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