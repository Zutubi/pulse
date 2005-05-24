package com.cinnamonbob.core2.type;

import com.cinnamonbob.core2.BobException;

/**
 * 
 *
 */
public class Description extends AbstractType
{

    private String description = "";
    
    public void addText(String text)
    {
        description += text;
    }
    
    public void execute() throws BobException
    {
        getProject().setDescription(description);
    }
}
