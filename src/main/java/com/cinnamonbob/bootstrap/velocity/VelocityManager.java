package com.cinnamonbob.bootstrap.velocity;

import org.apache.velocity.app.VelocityEngine;
import com.cinnamonbob.bootstrap.StartupManager;

/**
 * 
 *
 */
public class VelocityManager
{
    public static VelocityEngine getEngine()
    {
        return (VelocityEngine) StartupManager.getInstance().getApplicationContext().getBean("velocityEngine");
    }
}
