package com.cinnamonbob.bootstrap.velocity;

import org.apache.velocity.app.VelocityEngine;
import com.cinnamonbob.bootstrap.StartupManager;

/**
 * 
 *
 */
public class VelocityManager
{
    private static final String BEAN_NAME = "velocityEngine";

    public static VelocityEngine getEngine()
    {
        return (VelocityEngine) StartupManager.getBean(BEAN_NAME);
    }
}
