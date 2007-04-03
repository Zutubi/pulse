package com.zutubi.prototype.type;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class TypeConversionException extends TypeException
{
    private Map<String, String> fieldErrors = new HashMap<String, String>();

    public TypeConversionException()
    {
    }

    public TypeConversionException(String message)
    {
        super(message);
    }

    public TypeConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TypeConversionException(Throwable cause)
    {
        super(cause);
    }

    public List<String> getFieldErrors()
    {
        return new LinkedList<String>(fieldErrors.keySet());
    }

    public void addFieldError(String field, String errorMessage)
    {
        fieldErrors.put(field, "" + errorMessage);
    }

    public String getFieldError(String field)
    {
        return fieldErrors.get(field);
    }
}
