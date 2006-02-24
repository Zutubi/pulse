package com.cinnamonbob.security;

import com.cinnamonbob.jetty.JettyManager;

/**
 * <class-comment/>
 */
public class SecurityManager
{
    private JettyManager jettyManager;

    public void init()
    {
        jettyManager.addFilter();
    }

    public void setJettyManager(JettyManager jettyManager)
    {
        this.jettyManager = jettyManager;
    }
}
