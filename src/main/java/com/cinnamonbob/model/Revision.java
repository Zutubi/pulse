package com.cinnamonbob.model;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: daniel
 * Date: 14/08/2005
 * Time: 13:48:33
 * To change this template use File | Settings | File Templates.
 */
public class Revision extends Entity
{
    private Properties properties;

    protected Revision()
    {

    }

    protected Properties getProperties()
    {
        if(properties == null)
        {
            properties = new Properties();
        }
        return properties;
    }

    private void setProperties(Properties properties)
    {
        this.properties = properties;
    }
}
