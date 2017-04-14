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

package com.zutubi.pulse.dev.local;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ExternalPulseFileProvider;
import com.zutubi.pulse.core.engine.ResourcesConfiguration;
import com.zutubi.pulse.core.engine.marshal.ResourceFileLoader;
import com.zutubi.pulse.core.plugins.ResourceLocatorExtensionManager;
import com.zutubi.pulse.core.resources.ResourceDiscoverer;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.dev.bootstrap.DevBootstrapManager;
import com.zutubi.pulse.dev.client.UserAbortException;
import com.zutubi.pulse.dev.sync.SynchronisePluginsClientFactory;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;

/**
 * Entry point for executing local builds within a development tree.
 */
public class LocalBuild
{
    private EventManager eventManager;
    private RecipeProcessor recipeProcessor;
    private ResourceFileLoader resourceFileLoader;
    private ResourceLocatorExtensionManager resourceLocatorExtensionManager;

    public static void main(String argv[])
    {
        LocalBuild b = bootstrap();
        LocalBuildOptions options = null;
        try
        {
            options =  new LocalBuildOptions(argv);

            new SynchronisePluginsClientFactory().newInstance().syncIfBare();

            b.runBuild(FileSystemUtils.getWorkingDirectory(), options);
        }
        catch (UserAbortException e)
        {
            System.exit(2);
        }
        catch (Exception e)
        {
            fatal(e, options != null && options.isVerbose());
        }
        finally
        {
            DevBootstrapManager.shutdown();
        }
    }

    public static LocalBuild bootstrap()
    {
        DevBootstrapManager.startup("com/zutubi/pulse/dev/local/bootstrap/context/applicationContext.xml");
        return SpringComponentContext.getBean("localBuild");
    }

    private ResourcesConfiguration createRepository(String resourcesFile) throws PulseException
    {
        if (resourcesFile == null)
        {
            return new ResourcesConfiguration();
        }

        try
        {
            return resourceFileLoader.load(new File(resourcesFile));
        }
        catch (IOException e)
        {
            throw new PulseException("Unable to read resources file '" + resourcesFile + "'");
        }
    }

    /**
     * Executes a local build in the given directory, with the given options.  All paths provided
     * must be absolute or relative to the current working directory.
     *
     * @param baseDir the base directory in which to execute the build
     * @param options options controlling the behaviour of the build
     * @throws PulseException if a build error occurs
     */
    public void runBuild(File baseDir, LocalBuildOptions options) throws PulseException
    {
        printPrologue(options);

        ResourcesConfiguration resourcesConfiguration = createRepository(options.getResourcesFile());

        InMemoryResourceRepository repository = resourcesConfiguration.createRepository();
        ResourceDiscoverer discoverer = resourceLocatorExtensionManager.createResourceDiscoverer();
        discoverer.discoverAndAdd(repository);

        List<ResourceRequirement> resourceRequirements = new LinkedList<ResourceRequirement>(options.getResourceRequirements());
        resourceRequirements.addAll(resourcesConfiguration.createRequirements());
        
        RecipePaths paths = new LocalRecipePaths(baseDir, options.getOutputDir());

        if (!paths.getBaseDir().isDirectory())
        {
            throw new PulseException("Base directory '" + paths.getBaseDir().getAbsolutePath() + "' does not exist");
        }

        try
        {
            FileSystemUtils.cleanOutputDir(paths.getOutputDir());
        }
        catch (IOException e)
        {
            throw new PulseException("Unable to clean output directory '" + paths.getOutputDir().getAbsolutePath() + "': " + e.getMessage(), e);
        }

        File logFile = new File(baseDir, "build.log");
        FileOutputStream logStream = null;

        try
        {
            logStream = new FileOutputStream(logFile);

            eventManager.register(new BuildStatusPrinter(paths.getBaseDir(), paths.getOutputDir(), logStream, options.getFailureLimit()));

            PulseExecutionContext context = new PulseExecutionContext();
            context.setWorkingDir(baseDir);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_LOCAL_BUILD, Boolean.toString(true));
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, paths);
            context.addValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, repository);
            context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE, options.getRecipe());
            Bootstrapper bootstrapper = new LocalBootstrapper();
            RecipeRequest request = new RecipeRequest(bootstrapper, buildPulseFileProvider(baseDir, options), context);
            request.addAllResourceRequirements(resourceRequirements);
            recipeProcessor.build(request);
        }
        catch (FileNotFoundException e)
        {
            throw new PulseException("Unable to create log file '" + logFile.getPath() + "': " + e.getMessage());
        }
        finally
        {
            IOUtils.close(logStream);
        }

        printEpilogue(logFile);
    }

    private ExternalPulseFileProvider buildPulseFileProvider(File baseDir, LocalBuildOptions options)
    {
        String pulseFileString = options.getPulseFile();
        File pulseFile = new File(pulseFileString);
        if (!pulseFile.isAbsolute())
        {
            pulseFile = new File(baseDir, pulseFileString);
        }

        return new ExternalPulseFileProvider(pulseFile.getName(), pulseFile.getParentFile());
    }

    private void printPrologue(LocalBuildOptions options)
    {
        System.out.println("pulse file      : '" + options.getPulseFile() + "'");
        System.out.println("output directory: '" + options.getOutputDir() + "'");

        String resourcesFile = options.getResourcesFile();
        if (resourcesFile != null)
        {
            System.out.println("resources file  : '" + resourcesFile + "'");
        }

        System.out.println();
    }

    private void printEpilogue(File logFile)
    {
        System.out.println();
        System.out.println("Build report saved to '" + logFile.getPath() + "'.");
    }

    private static void fatal(Throwable throwable, boolean verbose)
    {
        if (verbose)
        {
            throwable.printStackTrace(System.err);
        }
        else
        {
            System.err.println(throwable.getMessage());            
        }
        System.exit(1);
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setRecipeProcessor(RecipeProcessor recipeProcessor)
    {
        this.recipeProcessor = recipeProcessor;
    }

    public void setResourceFileLoader(ResourceFileLoader resourceFileLoader)
    {
        this.resourceFileLoader = resourceFileLoader;
    }

    public void setResourceLocatorExtensionManager(ResourceLocatorExtensionManager resourceLocatorExtensionManager)
    {
        this.resourceLocatorExtensionManager = resourceLocatorExtensionManager;
    }
}
