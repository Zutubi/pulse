package com.zutubi.pulse.core;

/**
 * 
 *
 */
public interface InitComponent
{
    void initBeforeChildren() throws FileLoadException;
    void initAfterChildren();    
}
