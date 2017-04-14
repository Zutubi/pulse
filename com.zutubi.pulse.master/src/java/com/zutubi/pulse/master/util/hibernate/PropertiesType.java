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

package com.zutubi.pulse.master.util.hibernate;

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
 * A mutable type implementation for identifying java.util.Properties to
 * hibernates type system. 
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
