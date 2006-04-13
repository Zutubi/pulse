/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

/**
 * Unknown attribute exception is a type of parse exception that deals specifically
 * with the case when an attribute is encountered by not part of the definition.
 *
 * ie: When mapping element to an object that does not have a setSomeAttribute() method,
 * an UnknownAttributeException will be generated.
 *
 *      <element some-attribute=""/>
 *
 * 
 */
public class UnknownAttributeException extends ParseException
{
    public UnknownAttributeException(String errorMessage)
    {
        super(errorMessage);
    }

    public UnknownAttributeException()
    {
    }

    public UnknownAttributeException(Throwable cause)
    {
        super(cause);
    }

    public UnknownAttributeException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
