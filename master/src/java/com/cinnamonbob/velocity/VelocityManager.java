package com.cinnamonbob.velocity;

import com.cinnamonbob.bootstrap.ComponentContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * 
 *
 */
public class VelocityManager
{
    private static final String BEAN_NAME = "velocityEngine";

    public static VelocityEngine getEngine()
    {
        return (VelocityEngine) ComponentContext.getBean(BEAN_NAME);
    }
}
