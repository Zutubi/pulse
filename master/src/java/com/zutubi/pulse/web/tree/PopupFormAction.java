package com.zutubi.pulse.web.tree;

import com.zutubi.pulse.filesystem.File;
import com.zutubi.pulse.filesystem.FileSystem;
import com.zutubi.pulse.filesystem.FileSystemException;
import com.zutubi.pulse.filesystem.local.LocalFileSystem;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.codec.binary.Base64;

import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class PopupFormAction extends ActionSupport
{
    private MasterConfigurationManager configurationManager;

    private String formname;
    private String fieldname;

    private String pulseHome;
    private String userHome;
    private String initPath;

    public String getFormname()
    {
        return formname;
    }

    public void setFormname(String formname)
    {
        this.formname = formname;
    }

    public String getFieldname()
    {
        return fieldname;
    }

    public void setFieldname(String fieldname)
    {
        this.fieldname = fieldname;
    }

    public String getPulseHome()
    {
        return pulseHome;
    }

    public String getUserHome()
    {
        return userHome;
    }

    public void setInitPath(String initPath)
    {
        this.initPath = initPath;
    }

    public String getInitPath()
    {
        return initPath;
    }

    public String execute() throws Exception
    {
        // pulse home
        java.io.File f = configurationManager.getHomeDirectory();
        if (f != null)
        {
            pulseHome = f.getAbsolutePath();
            pulseHome = pulseHome.replace("\\", "\\\\");
        }

        // user home
        userHome = System.getProperty("user.home");
        userHome = userHome.replace("\\", "\\\\");

        initPath = initPath.replace("\\", "\\\\");
        return SUCCESS;
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
