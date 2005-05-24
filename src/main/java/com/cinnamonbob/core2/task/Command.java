package com.cinnamonbob.core2.task;

import com.cinnamonbob.core2.BobException;

/**
 * 
 *
 */
public class Command extends AbstractTask
{    
    private Executable exe;
    private String name;
    
    public Executable createExecutable() throws BobException
    {
        if (exe != null)
        {
            throw new BobException("Only single exe per command currently supported.");
        }
        exe = new Executable();
        return exe;
    }
    
    public void setName(String str)
    {
        this.name = str;
    }
    
    public void execute()
    {
        
    }
}
