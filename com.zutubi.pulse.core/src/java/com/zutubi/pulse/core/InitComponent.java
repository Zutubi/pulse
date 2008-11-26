package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.FileLoadException;

/**
 * 
 *
 */
public interface InitComponent
{
    void initBeforeChildren() throws FileLoadException;
    void initAfterChildren();    
}
