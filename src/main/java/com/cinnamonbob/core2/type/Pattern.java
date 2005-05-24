package com.cinnamonbob.core2.type;

import com.cinnamonbob.core2.BobException;

/**
 * 
 *
 */
public class Pattern extends AbstractType
{
    private String category;
    private String expression;

    public void setCategory(String category)
    {
        this.category = category;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public void execute() throws BobException
    {
        validate();
        
        // ??
    }
    
    private void validate() throws BobException
    {
        // validate category against FeatureCategoryRegistry.
        if (category == null)
        {
            throw new BobException("Category can not be null.");
        }
        if (expression == null)
        {
            throw new BobException("Expression can not be null.");
        }
    }
}
