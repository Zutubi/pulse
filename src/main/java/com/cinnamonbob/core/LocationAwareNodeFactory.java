package com.cinnamonbob.core;

import nu.xom.NodeFactory;
import nu.xom.Element;
import nu.xom.Document;
import nu.xom.Nodes;
import org.xml.sax.Locator;

import com.cinnamonbob.core.LocationAwareElement;

/**
 * NodeFactory extension that creates LocationAwareElements. These elements
 * contain data about where in the original xml document they were located.
 * This information is a best guess only.
 */
public class LocationAwareNodeFactory extends NodeFactory
{
    private Location lastLocation;
    
    public Element startMakingElement(String name, String namespace)
    {
        LocationAwareElement newElement = new LocationAwareElement(name, namespace);
        
        if (lastLocation != null)
        {
            newElement.setLineNumber(lastLocation.line);
            newElement.setColumnNumber(lastLocation.column);
        }
        recordLocation();
        
        return newElement;
    }

    public void finishMakingDocument(Document d)
    {
        recordLocation();
        super.finishMakingDocument(d);
    }
    
    public Nodes finishMakingElement(Element e)
    {
        recordLocation();
        return super.finishMakingElement(e);  
    }
    
    public Nodes makeComment(String data)
    {
        recordLocation();
        return super.makeComment(data);  
    }
    
    public Nodes makeText(String data) {
        recordLocation();
        return super.makeText(data);  
    }

    private void recordLocation()
    {
        Locator locator = getDocumentLocator();
        if (locator != null)
        {
            lastLocation  = new Location(locator);
        }
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
