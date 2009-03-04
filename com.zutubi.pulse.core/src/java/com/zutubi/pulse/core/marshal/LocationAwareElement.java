package com.zutubi.pulse.core.marshal;

import nu.xom.Element;

/**
 * Simple extension of the basic Element type that supports
 * the storage of line number and column number. The idea is to
 * store the location information of this element as it appeared
 * within the original XML document.
 */
public class LocationAwareElement extends Element
{
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
    
    public String toString()
    {
        return "[" + getQualifiedName() + " @ " + getLineNumber() + 
                ":" + getColumnNumber() + "]";        
    }
}
