package com.zutubi.pulse.master.util.hibernate;

import com.zutubi.util.time.TimeStamps;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hsqldb.Types;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A mutable type implementation for identifying com.zutubi.util.time.TimeStamps to
 * hibernates type system.
 */
public class TimeStampsType implements CompositeUserType
{
    private static final LongType LONG = new LongType();

    private static final int[] TYPES = new int[]{Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT};

    public int[] sqlTypes()
    {
        return TYPES;
    }

    public Class returnedClass()
    {
        return TimeStamps.class;
    }

    public boolean equals(Object x, Object y)
    {
        if (x == y)
        {
            return true;
        }
        if (x == null || y == null)
        {
            return false;
        }

        return x.equals(y);
    }

    public Object deepCopy(Object x)
    {
        if (x == null)
        {
            return null;
        }
        if (!(x instanceof TimeStamps))
        {
            return null;
        }
        return new TimeStamps((TimeStamps) x);
    }

    public boolean isMutable()
    {
        return true;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
            throws HibernateException, SQLException
    {

        Long queueTime = (Long) LONG.nullSafeGet(rs, names[0], session);
        Long startTime = (Long) LONG.nullSafeGet(rs, names[1], session);
        Long endTime = (Long) LONG.nullSafeGet(rs, names[2], session);
        Long estimatedRunningTime = (Long) LONG.nullSafeGet(rs, names[3], session);

        return new TimeStamps((queueTime != null) ? queueTime : -1, (startTime != null) ? startTime : -1, (endTime != null) ? endTime : -1, (estimatedRunningTime != null) ? estimatedRunningTime : -1);
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
            throws HibernateException, SQLException
    {

        if (value != null)
        {
            TimeStamps stamps = (TimeStamps) value;

            LONG.nullSafeSet(st, stamps.getQueueTime(), index, session);
            LONG.nullSafeSet(st, stamps.getStartTime(), index + 1, session);
            LONG.nullSafeSet(st, stamps.getEndTime(), index + 2, session);
            LONG.nullSafeSet(st, stamps.getEstimatedRunningTime(), index + 3, session);
        }
        else
        {
            LONG.nullSafeSet(st, null, index, session);
            LONG.nullSafeSet(st, null, index + 1, session);
            LONG.nullSafeSet(st, null, index + 2, session);
            LONG.nullSafeSet(st, null, index + 3, session);
        }
    }

    public String[] getPropertyNames()
    {
        return new String[]{"queueTime", "startTime", "endTime", "estimatedRunningTime"};
    }

    public Type[] getPropertyTypes()
    {
        return new Type[]{LONG, LONG, LONG, LONG};
    }

    public Object getPropertyValue(Object component, int property)
    {
        TimeStamps stamps = (TimeStamps) component;
        if (property == 0)
        {
            return stamps.getQueueTime();
        }
        else if (property == 1)
        {
            return stamps.getStartTime();
        }
        else if (property == 2)
        {
            return stamps.getEndTime();
        }
        else
        {
            return stamps.getEstimatedRunningTime();
        }
    }

    public void setPropertyValue(
            Object component,
            int property,
            Object value)
    {
        TimeStamps stamps = (TimeStamps) component;
        if (property == 0)
        {
            stamps.setQueueTime((Long) value);
        }
        else if (property == 1)
        {
            stamps.setStartTime((Long) value);
        }
        else if(property == 2)
        {
            stamps.setEndTime((Long) value);
        }
        else
        {
            stamps.setEstimatedRunningTime((Long) value);
        }
    }

    public Object assemble(
            Serializable cached,
            SessionImplementor session,
            Object owner)
    {

        return deepCopy(cached);
    }

    public Serializable disassemble(Object value, SessionImplementor session)
    {
        return (Serializable) deepCopy(value);
    }

    public int hashCode(Object x) throws HibernateException
    {
        return x.hashCode();
    }

    public Object replace(Object original, Object target, SessionImplementor session, Object owner) throws HibernateException
    {
        return new TimeStamps((TimeStamps) original);
    }
}
