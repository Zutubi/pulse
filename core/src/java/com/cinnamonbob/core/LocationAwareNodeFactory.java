package com.cinnamonbob.core;

import nu.xom.NodeFactory;
import nu.xom.Element;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;


/**
 * NodeFactory extension that creates LocationAwareElements. These elements
 * contain data about where in the original xml document they were located.
 * This information is a best guess only.
 */
public class LocationAwareNodeFactory extends NodeFactory
{
    private Locator locator;
    private Location startLocation;
    private Location endLocation;
    
    public LocationAwareNodeFactory()
    {
        super();
    }

    public Element startMakingElement(String name, String namespace)
    {
        LocationAwareElement newElement = new LocationAwareElement(name, namespace);
        
        if (startLocation != null)
        {
            newElement.setLineNumber(startLocation.line);
            newElement.setColumnNumber(startLocation.column);
        }
        recordLocation();
        
        return newElement;
    }

    private void recordLocation()
    {
        if (locator != null)
        {
            startLocation = endLocation;
            endLocation  = new Location(locator);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
        this.locator = locator;
    }

    @Override
    public void startDocument()
    {
        recordLocation();
    }

    @Override
    public void startElement(String string, String string1, String string2, Attributes attributes)
    {
        recordLocation();
    }

    @Override
    public void endElement(String string, String string1, String string2)
    {
        recordLocation();
    }

    @Override
    public void characters(char[] chars, int i, int i1)
    {
        recordLocation();
    }

    @Override
    public void ignorableWhitespace(char[] chars, int i, int i1)
    {
        recordLocation();
    }

    @Override
    public void processingInstruction(String string, String string1)
    {
        recordLocation();
    }

    private class Location
    {
        private int line;
        private int column;
        Location(Locator l)
        {
            line = l.getLineNumber();
            column = l.getColumnNumber();           
        }
        public String toString()
        {
            return "[" + line + "," + column + "]";
        }
    }
} 
