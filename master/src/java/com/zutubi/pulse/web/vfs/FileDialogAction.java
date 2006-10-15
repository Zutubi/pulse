package com.zutubi.pulse.web.vfs;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;

import java.io.File;

/**
 * The FileDialog action provides the basic model and default values for the file dialog widget.
 */
public class FileDialogAction extends VFSActionSupport
{
    private MasterConfigurationManager configurationManager;

    private String pulseHome;

    private String userHome;

    private String separator;

    private String root;

    private String initialPath;

    /**
     * When true, showFiles indicates that the file dialog should show files.
     */
    private boolean showFiles = false;

    /**
     * When true, showToolbar indicates that the file dialog toolbar should be visible.
     */
    private boolean showToolbar = true;

    /**
     * When true, showHidden indicates that hidden files should be displayed.
     */
    private boolean showHidden = false;

    public String getPulseHome()
    {
        return pulseHome;
    }

    public String getUserHome()
    {
        return userHome;
    }

    public String getSeparator()
    {
        return separator;
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public String getInitialPath()
    {
        return initialPath;
    }

    public void setInitialPath(String initialPath)
    {
        this.initialPath = initialPath;
    }

    public boolean isShowFiles()
    {
        return showFiles;
    }

    public void setShowFiles(boolean showFiles)
    {
        this.showFiles = showFiles;
    }

    public boolean isShowToolbar()
    {
        return showToolbar;
    }

    public void setShowToolbar(boolean showToolbar)
    {
        this.showToolbar = showToolbar;
    }

    public boolean isShowHidden()
    {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden)
    {
        this.showHidden = showHidden;
    }

    public String execute() throws Exception
    {
        // pulse home
        File f = configurationManager.getHomeDirectory();
        if (f != null)
        {
            pulseHome = f.getAbsolutePath();
            pulseHome = makeJavascriptFriendly(pulseHome);
        }

        // user home
        userHome = System.getProperty("user.home");
        userHome = makeJavascriptFriendly(userHome);

        separator = makeJavascriptFriendly(File.separator);

        return SUCCESS;
    }

    /**
     * The '\' character is the javascript escape character. It is also the separator character on
     * some systems. When we render a path that contains this character, the path is corrupted with the
     * '\' escaping the first character of the files it separates. SO, to fix this, we escape it if
     * necessary.
     *
     * @param str
     *
     * @return a version of str with the '\' escaped.
     */
    private String makeJavascriptFriendly(String str)
    {
        return str.replace("\\", "\\\\");
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
