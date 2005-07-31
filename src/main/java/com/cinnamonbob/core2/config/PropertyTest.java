package com.cinnamonbob.core2.config;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class PropertyTest extends TestCase
{
    public void testSetProperty()
    {
        BobFile project = new BobFile();
        Property property = new Property();
        property.setBobFile(project);
        property.setName("name");
        property.setValue("value");
        property.init();
        assertEquals("value", project.getProperty("name"));
    }
}
