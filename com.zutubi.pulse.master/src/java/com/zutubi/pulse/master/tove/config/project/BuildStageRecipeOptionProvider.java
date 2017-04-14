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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoader;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.pulse.core.marshal.RelativeFileResolver;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scm.ScmFileResolver;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.ListOptionProvider;
import com.zutubi.util.StringUtils;
import com.zutubi.util.concurrent.ConcurrentUtils;
import org.apache.commons.io.input.NullInputStream;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * An option provider for the recipe field of a build stage.  Designed to be
 * used lazily as it could take a long time to load the Pulse file from the
 * SCM.  This may even time out.
 */
public class BuildStageRecipeOptionProvider extends ListOptionProvider
{
    private static final int TIMEOUT_SECONDS = 30;
    
    private ConfigurationProvider configurationProvider;
    private PulseFileLoaderFactory fileLoaderFactory;
    private ProjectManager projectManager;
    private ScmManager scmManager;

    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        return null;
    }

    public List<String> getOptions(TypeProperty property, final FormContext context)
    {
        List<String> recipes = new LinkedList<String>();
        recipes.add("");

        List<String> recipeNames;
        try
        {
            recipeNames = ConcurrentUtils.runWithTimeout(SecurityUtils.callableAsSystem(new Callable<List<String>>()
            {
                public List<String> call() throws Exception
                {
                    ProjectConfiguration projectConfig = configurationProvider.getAncestorOfType(context.getClosestExistingPath(), ProjectConfiguration.class);
                    if (projectConfig != null)
                    {
                        PulseFileProvider pulseFileProvider = projectConfig.getType().getPulseFile();
                        PulseFileLoader pulseFileLoader = fileLoaderFactory.createLoader();

                        FileResolver resolver;
                        Project project = projectManager.getProject(projectConfig.getProjectId(), false);
                        if (project != null)
                        {
                            resolver = new ScmFileResolver(project, Revision.HEAD, scmManager);
                        }
                        else
                        {
                            // Could be a template project, in this case don't support file loading (this still supports
                            // pulse files defined inside configuration, just not versioned projects).
                            resolver = new FileResolver()
                            {
                                public InputStream resolve(String path) throws Exception
                                {
                                    return new NullInputStream(0);
                                }
                            };
                        }

                        String fileContent = pulseFileProvider.getFileContent(resolver);
                        if (StringUtils.stringSet(fileContent))
                        {
                            return pulseFileLoader.loadAvailableRecipes(fileContent, new RelativeFileResolver(pulseFileProvider.getPath(), resolver));
                        }
                    }

                    return Collections.emptyList();
                }
            }), TIMEOUT_SECONDS, TimeUnit.SECONDS, null);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load Pulse file to load available recipes: " + e.getMessage(), e);
        }

        if (recipeNames == null)
        {
            throw new RuntimeException("Timed out listing recipes in Pulse file.");
        }

        for (String recipe: recipeNames)
        {
            recipes.add(recipe);
        }

        return recipes;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}