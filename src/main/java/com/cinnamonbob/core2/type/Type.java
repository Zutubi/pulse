package com.cinnamonbob.core2.type;

import com.cinnamonbob.core2.Project;
import com.cinnamonbob.core2.BobException;

/**
 * 
 *
 */
public interface Type
{
    void setProject(Project p);    
    void setId(String id);
    String getId();    
    void execute() throws BobException;
}
