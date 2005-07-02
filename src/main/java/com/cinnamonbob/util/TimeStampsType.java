package com.cinnamonbob.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

public class TimeStampsType implements CompositeUserType
{

    public String[] getPropertyNames()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Type[] getPropertyTypes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getPropertyValue(Object arg0, int arg1) throws HibernateException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setPropertyValue(Object arg0, int arg1, Object arg2) throws HibernateException
    {
        // TODO Auto-generated method stub
        
    }

    public Class returnedClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean equals(Object arg0, Object arg1) throws HibernateException
    {
        // TODO Auto-generated method stub
        return false;
    }

    public int hashCode(Object arg0) throws HibernateException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object nullSafeGet(ResultSet arg0, String[] arg1, SessionImplementor arg2, Object arg3) throws HibernateException, SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void nullSafeSet(PreparedStatement arg0, Object arg1, int arg2, SessionImplementor arg3) throws HibernateException, SQLException
    {
        // TODO Auto-generated method stub
        
    }

    public Object deepCopy(Object arg0) throws HibernateException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isMutable()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public Serializable disassemble(Object arg0, SessionImplementor arg1) throws HibernateException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Object assemble(Serializable arg0, SessionImplementor arg1, Object arg2) throws HibernateException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Object replace(Object arg0, Object arg1, SessionImplementor arg2, Object arg3) throws HibernateException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
