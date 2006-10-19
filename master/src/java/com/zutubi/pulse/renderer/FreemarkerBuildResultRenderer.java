package com.zutubi.pulse.renderer;

import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.StringUtils;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.web.project.CommitMessageHelper;
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
    private CommitMessageHelper commitMessageHelper;
    private ProjectManager projectManager;

    public void render(String baseUrl, BuildResult result, List<Changelist> changelists, String templateName, Writer writer)
    {
        Map<String, Object> dataMap = new HashMap<String, Object>();

        dataMap.put("renderer", this);
        dataMap.put("baseUrl", baseUrl);
        dataMap.put("result", result);
        dataMap.put("changelists", changelists);
        dataMap.put("model", result);
        dataMap.put("errorLevel", Feature.Level.ERROR);
        dataMap.put("warningLevel", Feature.Level.WARNING);

        try
        {
            Template template = freemarkerConfiguration.getTemplate(getTemplatePath() + File.separatorChar + templateName + ".ftl");
            template.process(dataMap, writer);
            writer.flush();
        }
        catch (Exception e)
        {
            // TemplateExceptions also end up in the writer output
            LOG.warning("Unable to render build result: " + e.getMessage(), e);
        }
    }

    private String getTemplatePath()
    {
        return FileSystemUtils.composeFilename("notifications", "builds");
    }

    public List<TemplateInfo> getAvailableTemplates()
    {
        // Templates are stored under <root>/notifications/builds/<name>.ftl
        List<TemplateInfo> result = new ArrayList<TemplateInfo>();
        List<File> templateRoots = systemPaths.getTemplateRoots();
        for(File root: templateRoots)
        {
            File dir = new File(root, getTemplatePath());
            if(dir.isDirectory())
            {
                String[] names = dir.list(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".ftl");
                    }
                });

                for(String name: names)
                {
                    result.add(getTemplateInfo(name.substring(0, name.length() - 4)));
                }
            }
        }

        return result;
    }

    public TemplateInfo getTemplateInfo(String templateName)
    {
        String display = getDefaultDisplay(templateName);
        String mimeType = "text/plain";
        File propertiesFile = findProperties(templateName);

        if(propertiesFile != null)
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

    private File findProperties(String templateName)
    {
        List<File> templateRoots = systemPaths.getTemplateRoots();
        for(File root: templateRoots)
        {
            File dir = new File(root, getTemplatePath());
            if(dir.isDirectory())
            {
                File candidate = new File(dir, templateName + ".properties");
                if(candidate.exists())
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

    public String transformComment(Changelist changelist)
    {
        if(commitMessageHelper == null)
        {
            commitMessageHelper = new CommitMessageHelper(projectManager.getCommitMessageTransformers());
        }

        return commitMessageHelper.applyTransforms(changelist, 60);
    }

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public void setCommitMessageHelper(CommitMessageHelper commitMessageHelper)
    {
        this.commitMessageHelper = commitMessageHelper;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }
}
