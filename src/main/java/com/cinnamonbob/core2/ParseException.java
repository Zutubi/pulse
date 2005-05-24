package com.cinnamonbob.core2;

import nu.xom.Element;

/**
 * 
 *
 */
public class ParseException extends BobException
{
    private String sourceName;
    private int lineNumber = -1;
    private int columnNumber = -1;

    public ParseException()
    {
        super();
    }

    public ParseException(String errorMessage)
    {
        super(errorMessage);
    }

    public ParseException(Throwable cause)
    {
        super(cause);
    }

    public ParseException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }

    public ParseException(String errorMessage, Throwable cause, Element source)
    {
        this(errorMessage, cause);
        if (source instanceof LocationAwareElement)
        {
            setElement((LocationAwareElement) source);
        }
    }

    public ParseException(String errorMessage, Element source)
    {
        this(errorMessage);
        if (source instanceof LocationAwareElement)
        {
            setElement((LocationAwareElement) source);
        }
    }
    
    public String getMessage()
    {
        return super.getMessage() + " at line " + lineNumber + ", column " + columnNumber + ".";
    }

    protected void setElement(LocationAwareElement e)
    {
        this.lineNumber = e.getLineNumber();
        this.columnNumber = e.getColumnNumber();
    }
    
    protected void setSourceName(String name)
    {
        this.sourceName = name;
    }

    protected void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    protected void setColumnNumber(int columnNumber)
    {
        this.columnNumber = columnNumber;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public int getColumnNumber()
    {
        return columnNumber;
    }

}
