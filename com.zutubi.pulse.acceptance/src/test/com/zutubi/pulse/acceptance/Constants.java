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

    /**
     * The constants for the property names in the ProjectConfiguration class.
     */
    public static class Project
    {
        public static final String TYPE = "type";

        /**
         * The constants for the property names in the project template type class.
         */
        public static class Type
        {
            public static final String ARTIFACTS = "artifacts";
        }
    }

    /**
     * The constants for the property names in the ArtifactConfiguration class.
     */
    public static class Artifact
    {
        public static final String NAME = "name";
        public static final String POSTPROCESSORS = "postprocessors";
    }

    /**
     * The constants for the property names in the DirectoryArtifactConfiguration class.
     */
    public static class DirectoryArtifact extends Artifact
    {
        public static final String BASE = "base";
        public static final String INCLUDES = "includes";
        public static final String EXCLUDES = "excludes";
        public static final String MIME_TYPE = "type";
    }
}
