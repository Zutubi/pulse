package com.zutubi.pulse.renderer;

import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.web.project.CommitMessageSupport;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.util.*;

/**
 */
public class FreemarkerBuildResultRenderer implements BuildResultRenderer
{
    private static final Logger LOG = Logger.getLogger(FreemarkerBuildResultRenderer.class);

    private Configuration freemarkerConfiguration;
    private SystemPaths systemPaths;

    public void render(BuildResult result, Map<String, Object> dataMap, String templateName, Writer writer)
    {
        try
        {
            Template template = freemarkerConfiguration.getTemplate(getTemplatePath(result.isPersonal()) + File.separatorChar + templateName + ".ftl");
            template.process(dataMap, writer);
            writer.flush();
        }
        catch (Exception e)
        {
            // TemplateExceptions also end up in the writer output
            LOG.warning("Unable to render build result: " + e.getMessage(), e);
        }
    }

    public boolean hasTemplate(String templateName, boolean personal)
    {
        try
        {
            Template template = freemarkerConfiguration.getTemplate(getTemplatePath(personal) + File.separatorChar + templateName + ".ftl");
            return template != null;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private String getTemplatePath(boolean personal)
    {
        return FileSystemUtils.composeFilename("notifications", (personal ? "personal" : "project") + "-builds");
    }

    public List<TemplateInfo> getAvailableTemplates(boolean personal)
    {
        // Templates are stored under <root>/notifications/builds/<name>.ftl
        List<TemplateInfo> result = new ArrayList<TemplateInfo>();
        List<File> templateRoots = systemPaths.getTemplateRoots();
        for (File root : templateRoots)
        {
            File dir = new File(root, getTemplatePath(personal));
            if (dir.isDirectory())
            {
                String[] names = dir.list(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".ftl") && !name.endsWith("-subject.ftl");
                    }
                });

                for (String name : names)
                {
                    result.add(getTemplateInfo(name.substring(0, name.length() - 4), personal));
                }
            }
        }

        return result;
    }

    public TemplateInfo getTemplateInfo(String templateName, boolean personal)
    {
        String display = getDefaultDisplay(templateName);
        String mimeType = "text/plain";
        File propertiesFile = findProperties(templateName, personal);

        if (propertiesFile != null)
        {
            Properties properties = new Properties();
            try
            {
                properties.load(new FileInputStream(propertiesFile));
                display = properties.getProperty("display", display);
                mimeType = properties.getProperty("type", mimeType);
            }
            catch (IOException e)
            {
                LOG.warning("Unable to load template properties file '" + propertiesFile.getAbsolutePath() + "'", e);
            }
        }

        return new TemplateInfo(templateName, display, mimeType);
    }

    public String getDefaultDisplay(String templateName)
    {
        return templateName.replaceAll("-", " ");
    }

    private File findProperties(String templateName, boolean personal)
    {
        List<File> templateRoots = systemPaths.getTemplateRoots();
        for (File root : templateRoots)
        {
            File dir = new File(root, getTemplatePath(personal));
            if (dir.isDirectory())
            {
                File candidate = new File(dir, templateName + ".properties");
                if (candidate.exists())
                {
                    return candidate;
                }
            }
        }

        return null;
    }

    public String trimmedString(String s, int length)
    {
        return StringUtils.trimmedString(s, length);
    }

    public String wrapString(String s, String prefix)
    {
        return StringUtils.wrapString(s, 64, prefix);
    }

    public String transformCommentWithoutTrimming(Changelist changelist)
    {
        // FIXME
        CommitMessageSupport support = new CommitMessageSupport(changelist, Collections.EMPTY_LIST);
        return support.toString();
    }

    public String transformComment(Changelist changelist)
    {
        return transformComment(changelist, 60);
    }
    
    public String transformComment(Changelist changelist, int trimToLength)
    {
        // FIXME
        CommitMessageSupport support = new CommitMessageSupport(changelist, Collections.EMPTY_LIST);
        return support.trim(trimToLength);
    }

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }
}
