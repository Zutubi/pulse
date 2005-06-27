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
        Project project = new Project();
        Property property = new Property();
        property.setProject(project);
        property.setName("name");
        property.setValue("value");
        property.init();
        assertEquals("value", project.getProperty("name"));
    }
}
