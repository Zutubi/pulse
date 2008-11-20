package com.zutubi.pulse.acceptance;

/**
 * General bucket for constants used in acceptance tests.
 */
public class Constants
{
    /**
     * Subversion URL for a trivial ant project (just a build.xml file).
     */
    public static final String TRIVIAL_ANT_REPOSITORY = "svn://localhost:3088/accept/trunk/triviant";
    /**
     * Subversion URL for a small sample ant project that contains a build file as well as src and
     * test directories.
     */
    public static final String TEST_ANT_REPOSITORY = "svn://localhost:3088/accept/trunk/testant";
    /**
     * Subversion URL for a small sample maven project that contains the pom.xml, along with a
     * source and test file.
     */
    public static final String TEST_MAVEN_REPOSITORY = "svn://localhost:3088/accept/trunk/testmaven";
}
