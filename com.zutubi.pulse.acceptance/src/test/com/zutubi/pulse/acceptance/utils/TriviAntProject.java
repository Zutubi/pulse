package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.tmatesoft.svn.core.SVNException;

import java.io.File;
import java.io.IOException;

/**
 * Helper for the trivial ant project in the test Subversion repository.
 */
public class TriviAntProject extends AntProjectHelper
{
    public static final String CHANGE_AUTHOR = "pulse";

    protected TriviAntProject(ProjectConfiguration config, ConfigurationHelper helper)
    {
        super(config, helper);
    }

    /**
     * Helper that checks out, updates and commits the build.xml file for this project.
     * 
     * @param comment     the change comment
     * @param echoMessage a message that the build file should echo after editing (note that you
     *                    must ensure this is different from the existing message or no change will
     *                    be committed)
     * @return the committed revision
     * @throws IOException on i/o error
     * @throws SVNException on an error interacting with the working copy or repository
     */
    public String editAndCommitBuildFile(String comment, String echoMessage) throws IOException, SVNException
    {
        SubversionConfiguration scm = (SubversionConfiguration) getConfig().getScm();
        return editAndCommitFile(scm.getUrl(), "build.xml", comment,
                "<?xml version=\"1.0\"?>\n" +
                        "<project default=\"default\">\n" +
                        "    <target name=\"default\">\n" +
                        "        <echo message=\"" + echoMessage + "\"/>\n" +
                        "    </target>\n" +
                        "</project>");
    }

    private String editAndCommitFile(String repository, String filename, String comment, String newContent) throws IOException, SVNException
    {
        File wcDir = FileSystemUtils.createTempDir(getClass().getName());
        SubversionWorkspace workspace = new SubversionWorkspace(wcDir, CHANGE_AUTHOR, CHANGE_AUTHOR);
        try
        {
            workspace.doCheckout(repository);
            return workspace.editAndCommitFile(filename, comment, newContent);
        }
        finally
        {
            IOUtils.close(workspace);
        }
    }
}
