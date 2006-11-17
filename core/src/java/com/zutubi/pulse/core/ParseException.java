package com.zutubi.pulse.core;

/**
 * 
 *
 */
public class ParseException extends PulseException
{
    private int line;
    private int column;

    /**
     * @param errorMessage
     */
    public ParseException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * 
     */
    public ParseException()
    {
        super();
    }

    /**
     * @param cause
     */
    public ParseException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param errorMessage
     * @param cause
     */
    public ParseException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }

    public ParseException(int line, int column, String message)
    {
        super(message);
        this.line = line;
        this.column = column;
    }

    public ParseException(int line, int column, Throwable cause)
    {
        super(cause);
        this.line = line;
        this.column = column;
    }

    public int getLine()
    {
        return line;
    }

    public int getColumn()
    {
        return column;
    }
}
