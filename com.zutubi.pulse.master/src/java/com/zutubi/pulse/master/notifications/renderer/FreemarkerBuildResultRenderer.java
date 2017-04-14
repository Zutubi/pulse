/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.notifications.renderer;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.committransformers.CommitMessageSupport;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.util.*;

/**
 */
public class FreemarkerBuildResultRenderer implements BuildResultRenderer
{
    public static final String FEATURE_LIMIT_PROPERTY = "pulse.feature.limit";
    public static final int    DEFAULT_FEATURE_LIMIT  = 100;

    private static final Logger LOG = Logger.getLogger(FreemarkerBuildResultRenderer.class);

    private Configuration freemarkerConfiguration;
    private SystemPaths systemPaths;
    private MasterUserPaths userPaths;

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
        for (File root : getAllTemplateRoots())
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
        for (File root : getAllTemplateRoots())
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

    private List<File> getAllTemplateRoots()
    {
        List<File> templateRoots = new LinkedList<File>();
        templateRoots.add(userPaths.getUserTemplateRoot());
        templateRoots.addAll(systemPaths.getTemplateRoots());
        return templateRoots;
    }

    public int getFeatureLimit()
    {
        return Integer.getInteger(FEATURE_LIMIT_PROPERTY, DEFAULT_FEATURE_LIMIT);
    }

    public boolean featureLimitReached(int count)
    {
        int limit = getFeatureLimit();
        return limit > 0 && count >= limit;
    }

    public String trimmedString(String s, int length)
    {
        return StringUtils.trimmedString(s, length);
    }

    public String wrapString(String s, String prefix)
    {
        return StringUtils.wrapString(s, 64, prefix);
    }

    public String transformCommentWithoutTrimming(BuildResult buildResult, PersistentChangelist changelist)
    {
        return createCommitMessageSupport(buildResult, changelist).toString();
    }

    public String transformComment(BuildResult buildResult, PersistentChangelist changelist)
    {
        return transformComment(buildResult, changelist, 60);
    }
    
    public String transformComment(BuildResult buildResult, PersistentChangelist changelist, int trimToLength)
    {
        return createCommitMessageSupport(buildResult, changelist).trim(trimToLength);
    }

    private CommitMessageSupport createCommitMessageSupport(BuildResult buildResult, PersistentChangelist changelist)
    {
        return new CommitMessageSupport(changelist.getComment(), buildResult.getProject().getConfig().getCommitMessageTransformers().values());
    }

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setUserPaths(MasterUserPaths userPaths)
    {
        this.userPaths = userPaths;
    }
}
