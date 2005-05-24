package com.cinnamonbob.core2.type;

import com.cinnamonbob.core2.BobException;

/**
 * 
 *
 */
public class Property extends AbstractType
{
    private String name;
    private String value;        
    
    public void execute() throws BobException
    {
        validate();
        getProject().setProperty(name, value);
    }

    private void validate() throws BobException
    {
        if (name == null)
        {
            throw new BobException();
        }
        if (value == null)
        {
            throw new BobException();
        }
    }
    
    public void setName(String name)
    {
        this.name = name;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
