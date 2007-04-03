package com.zutubi.pulse.web.vfs;

import java.io.File;

/**
 * The FileDialog action provides the basic model and default values for the file dialog widget.
 */
public class FileDialogAction extends VFSActionSupport
{
    /**
     * The file system separator.
     */
    private String separator;

    //---( the following properties are generally applicable. )---

    /**
     * The file system root that defines base directory to be browsed.
     */
    private String root;

    /**
     * The prefix is stripped when the value is eventually selected.
     */
    private String prefix;

    /**
     * The initial path defines what directory path should be opened
     */
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

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
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

    public String getTitle()
    {
        return getText("dialog.title");
    }

    public String getHeader()
    {
        return getText("dialog.header");
    }

    public String getDescription()
    {
        return getText("dialog.description");
    }

    public String execute() throws Exception
    {
        super.execute();
        
        separator = makeJavascriptFriendly(File.separator);
        return SUCCESS;
    }

    /**
     * The '\' character is the javascript escape character. It is also the separator character on
     * some systems. When we render a path that contains this character, the path is corrupted with the
     * '\' escaping the first character of the files it separates. SO, to fix this, we escape it if
     * necessary.
     *
     * @param str string to be processed.
     * 
     * @return a version of str with the '\' escaped.
     */
    protected String makeJavascriptFriendly(String str)
    {
        return str.replace("\\", "\\\\");
    }


}
