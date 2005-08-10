package com.cinnamonbob.core;

import com.cinnamonbob.BobRuntimeException;

/**
 * An InternalBuildFailureException is raised when something catastrophic
 * happens during a project build.  It is "normal" for builds to fail, this
 * is just recorded in the result.  This exception is used when terrible
 * things like I/O errors occur when trying to, for example, store command
 * output.
 * 
 * @author jsankey
 */
public class InternalBuildFailureException extends BobRuntimeException
{
    private static final long serialVersionUID = 3257005458144178485L;
    
    //=========================================================================
    // Construction
    //=========================================================================
    
    public InternalBuildFailureException(String message, Exception cause)
    {
        super(message, cause);
    }

    
    public InternalBuildFailureException(String message)
    {
        super(message);
    }
    
    public InternalBuildFailureException(Exception cause)
    {
        super(cause);
    }
    
    public InternalBuildFailureException()
    {
        super();
    }
}