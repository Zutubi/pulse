package com.zutubi.pulse.core.marshal;

import com.zutubi.util.StringUtils;
import nu.xom.Element;

/**
 * Simple extension of the basic Element type that supports
 * the storage of line number and column number. The idea is to
 * store the location information of this element as it appeared
 * within the original XML document.
 */
public class LocationAwareElement extends Element
{
    private String file;
    /**
     * Original document line number.
     */
    private int lineNumber;

    /**
     * Original document column number.
     */
    private int columnNumber;

    /**
     * @param name of the element
     */
    public LocationAwareElement(String name)
    {
        super(name);
    }

    /**
     * @param name
     * @param namespace
     */
    public LocationAwareElement(String name, String namespace)
    {
        super(name, namespace);
    }

    /**
     * Get the file within which this element was found, if it is known.
     *
     * @return the path of the file in which this element was found, or null
     *         if the file is not known
     */
    public String getFile()
    {
        return file;
    }

    protected void setFile(String file)
    {
        this.file = file;
    }

    /**
     * Get the line number of this element as it appeared in the xml document.
     *
     * @return
     * @see org.xml.sax.Locator#getLineNumber()
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    protected void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    /**
     * Get the column number of this element as it appeared in the xml document.
     *
     * @return
     * @see org.xml.sax.Locator#getColumnNumber()
     */
    public int getColumnNumber()
    {
        return columnNumber;
    }

    protected void setColumnNumber(int columnNumber)
    {
        this.columnNumber = columnNumber;
    }

    /**
     * Formats the location of this element in a string that is suitable for
     * human consumption.
     * 
     * @return the location of this element in a human-readable form
     */
    public String formatLocation()
    {
        String location = "line " + lineNumber + " column " + columnNumber;
        if (StringUtils.stringSet(file))
        {
            location += " of file " + file;
        }
        
        return location;
    }

    public String toString()
    {
        return "[" + getQualifiedName() + " @ " + getLineNumber() +
                ":" + getColumnNumber() + "]";
    }
}
