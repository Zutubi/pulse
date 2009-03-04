package com.zutubi.pulse.core.marshal;

/**
 * 
 *
 */
public interface InitComponent
{
    void initBeforeChildren() throws FileLoadException;
    void initAfterChildren();    
}
