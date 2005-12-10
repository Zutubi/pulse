package com.cinnamonbob.services;

import com.cinnamonbob.BuildService;

/**
 */
public interface SlaveService extends BuildService
{
    /**
     * Do-nothing method just used to test communications.
     */
    void ping();
}
