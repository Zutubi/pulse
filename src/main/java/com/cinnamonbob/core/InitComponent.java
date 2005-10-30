package com.cinnamonbob.core;

/**
 * 
 *
 */
public interface InitComponent
{
    void initBeforeChildren() throws FileLoadException;
    void initAfterChildren();    
}
