package com.cinnamonbob.core2;

import junit.framework.TestCase;

import java.io.File;

/**
 * 
 *
 */
public class Core2Test extends TestCase
{
    
    public void testLoad() throws Exception
    {
        ProjectLoader l = new ProjectLoader();
        Project project = l.load(new File("e:/work/project-ci/repository/src/main/config/host-release-3.0.xml"));
        project.getName();
    }
}
