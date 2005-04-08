package com.cinnamonbob.core;

/**
 * Raised on an error in a configuration file.
 */
public class ConfigException extends Exception
{
    private static final long serialVersionUID = 3760560884304656441L;

    /**
     * A value to use when the line number is not known.
     */
    public static final int UNKNOWN_LINE = -1;
    /**
     * A value to use when the column number is not known.
     */
    public static final int UNKNOWN_COLUMN = -1;
    
    /**
     * The name of the configuration file.
     */
    private String filename;
    /**
     * The line at which the error occured.
     */
    private int line;
    /**
     * The column at which the error occured.
     */
    private int column;
    /**
     * The details of the error.
     */
    private String details;
    
    
    /**
     * Creates a new ConfigException for an error at the given position in the
     * given file.  If the line or column number is not known, use the
     * corresponding UNKNOWN_ constant.
     * 
     * @param filename
     *        the name of the file in which the error was found
     * @param line
     *        the line number the error occured on (or UNKNOWN_LINE)
     * @param column
     *        the column number the error occured on (or UNKNOWN_COLUMN)
     * @param details
     *        a human-readable description of the error
     */
    public ConfigException(String filename, int line, int column, String details)
    {
        this.filename = filename;
        this.line = line;
        this.column = column;
        this.details = details;
    }
    
    
    /**
     * Creates a new ConfigException for an error loading the given file.
     * Line and column number information is not stored or reported.
     * 
     * @param filename
     *        name of the file in which the error was found 
     * @param details
     *        a human-readable description of teh error
     */
    public ConfigException(String filename, String details)
    {
        this.filename = filename;
        this.line = UNKNOWN_LINE;
        this.column = UNKNOWN_COLUMN;
        this.details = details;
    }

    
    /**
     * @return Returns the column.
     */
    public int getColumn()
    {
        return column;
    }
    

    /**
     * @return Returns the details.
     */
    public String getDetails()
    {
        return details;
    }
    

    /**
     * @return Returns the filename.
     */
    public String getFilename()
    {
        return filename;
    }
    

    /**
     * @return Returns the line.
     */
    public int getLine()
    {
        return line;
    }
    
    
    /**
     * Returns the details of this exception as a human-readable string.  If
     * line and column information is not known, it will not be reported.
     * 
     * @see java.lang.Object#toString()
     * @return Returns a string representation of this exception.
     */
    public String toString()
    {
        String result = filename + ": ";
        
        if(line != UNKNOWN_LINE)
        {
            result += Integer.toString(line) + ": ";
        }
        
        if(column != UNKNOWN_COLUMN)
        {
            result += Integer.toString(column) + ": ";
        }
        
        result += details;
        return result;
    }
}
