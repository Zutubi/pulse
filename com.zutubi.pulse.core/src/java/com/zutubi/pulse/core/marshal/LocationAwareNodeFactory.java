/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.marshal;

import nu.xom.Element;
import nu.xom.NodeFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;


/**
 * NodeFactory extension that creates LocationAwareElements. These elements
 * contain data about where in the original xml document they were located.
 * This information is a best guess only.
 */
public class LocationAwareNodeFactory extends NodeFactory
{
    private String file;
    private Locator locator;
    private Location startLocation;
    private Location endLocation;

    public LocationAwareNodeFactory(String file)
    {
        super();
        this.file = file;
    }

    public Element startMakingElement(String name, String namespace)
    {
        LocationAwareElement newElement = new LocationAwareElement(name, namespace);
        
        if (startLocation != null)
        {
            newElement.setFile(file);
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
