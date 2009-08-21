package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.acceptance.XmlRpcHelper;
import com.zutubi.pulse.master.model.ProjectManager;

import java.util.Hashtable;

public class FailAntProjectHelper extends ProjectHelper
{
    public FailAntProjectHelper(XmlRpcHelper xmlRpcHelper, String name)
    {
        super(xmlRpcHelper, name);
    }

    public FailAntProjectHelper(XmlRpcHelper xmlRpcHelper, String name, String org)
    {
        super(xmlRpcHelper, name, org);
    }

    public Hashtable<String, Object> insertProject() throws Exception
    {
        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();

        xmlRpcHelper.insertSingleCommandProject(getName(), ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.FAIL_ANT_REPOSITORY), antConfig);

        return antConfig;
    }
}
