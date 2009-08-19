package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.acceptance.XmlRpcHelper;
import com.zutubi.pulse.master.model.ProjectManager;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;

public class WaitAntProject extends Project
{
    private File waitFile;
    private File tmpDir;

    public WaitAntProject(XmlRpcHelper xmlRpcHelper, File tmpDir, String name)
    {
        super(xmlRpcHelper, name);
        this.tmpDir = tmpDir;
    }

    public WaitAntProject(XmlRpcHelper xmlRpcHelper, File tmpDir, String name, String org)
    {
        super(xmlRpcHelper, name, org);
        this.tmpDir = tmpDir;
    }

    public Hashtable<String, Object> insertProject() throws Exception
    {
        waitFile =  new File(tmpDir, getName());

        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put(Constants.Project.AntCommand.ARGUMENTS, getFileArgument(waitFile));

        xmlRpcHelper.insertSingleCommandProject(getName(), ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.WAIT_ANT_REPOSITORY), antConfig);

        return antConfig;
    }

    public void releaseBuild() throws IOException
    {
        FileSystemUtils.createFile(waitFile, "test");
    }

    private String getFileArgument(File waitFile)
    {
        return "-Dfile=" + waitFile.getAbsolutePath().replace("\\", "/");
    }
}
