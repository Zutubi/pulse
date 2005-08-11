package com.cinnamonbob.util;

import org.hibernate.HibernateException;
import org.hibernate.type.MutableType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

/**
 *
 *
 */
public class PropertiesType extends MutableType
{

    public Object get(ResultSet rs, String name) throws SQLException
    {
        return fromStringValue(rs.getString(name));
    }

    public Class getReturnedClass()
    {
        return Properties.class;
    }

    public void set(PreparedStatement st, Object value, int index) throws SQLException
    {
        st.setString(index, toString(value));
    }

    public int sqlType()
    {
        return Types.VARCHAR;
    }

    public String getName()
    {
        return "properties";
    }

    public String toString(Object value)
    {
        if (value != null)
        {
            Properties properties = (Properties) value;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                properties.store(baos, "");
                return baos.toString();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Object fromStringValue(String xml)
    {
        if (xml != null)
        {
            Properties properties = new Properties();
            try
            {
                properties.load(new ByteArrayInputStream(xml.getBytes()));
                return properties;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected Object deepCopyNotNull(Object value) throws HibernateException
    {
        Properties original = (Properties) value;
        Properties copy = new Properties();
        copy.putAll(original);
        return copy;
    }
}
