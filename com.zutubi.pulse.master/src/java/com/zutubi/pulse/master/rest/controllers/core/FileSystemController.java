package com.zutubi.pulse.master.rest.controllers.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.rest.Utils;
import com.zutubi.pulse.master.rest.model.fs.FileModel;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.TypeException;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * A RESTish API controller that allows browsing of a file system.
 */
@RestController
@RequestMapping("/fs")
public class FileSystemController
{
    @Autowired
    private AccessManager accessManager;

    private ProjectManager projectManager;
    private ScmManager scmManager;

    @RequestMapping(value = "/scm/{projectName}/**", method = RequestMethod.GET)
    public ResponseEntity<FileModel[]> getScm(HttpServletRequest request,
                                           @PathVariable final String projectName,
                                           @RequestParam(value = "showFiles", required = false, defaultValue = "true") boolean showFiles) throws TypeException, ScmException
    {
        if (projectManager == null)
        {
            SpringComponentContext.autowire(this);
        }

        final String path = Utils.getRequestedPath(request, true, true);

        Project project = projectManager.getProject(projectName, false);
        if (project == null)
        {
            throw new IllegalArgumentException("Unknown project '" + projectName + "'");
        }

        accessManager.ensurePermission(AccessManager.ACTION_WRITE, project.getConfig());

        Iterable<ScmFile> files = ScmClientUtils.withScmClient(project.getConfig(), project.getState(), scmManager, new ScmClientUtils.ScmContextualAction<List<ScmFile>>()
        {
            @Override
            public List<ScmFile> process(ScmClient client, ScmContext context) throws ScmException
            {
                if (!client.getCapabilities(context).contains(ScmCapability.BROWSE))
                {
                    throw new RuntimeException("The scm of project '" + projectName + "' does not support browsing");
                }

                return client.browse(context, path, null);
            }
        });

        if (!showFiles)
        {
            files = Iterables.filter(files, new Predicate<ScmFile>()
            {
                @Override
                public boolean apply(ScmFile input)
                {
                    return input.isDirectory();
                }
            });
        }

        Iterable<FileModel> models = Iterables.transform(files, new Function<ScmFile, FileModel>()
        {
            @Override
            public FileModel apply(ScmFile input)
            {
                return new FileModel(input.getName(), input.isDirectory());
            }
        });


        return new ResponseEntity<>(Iterables.toArray(models, FileModel.class), HttpStatus.OK);
    }

    @RequestMapping(value = "/local/**", method = RequestMethod.GET)
    public ResponseEntity<FileModel[]> getLocal(HttpServletRequest request,
                                                @RequestParam(value = "showFiles", required = false, defaultValue = "true") final boolean showFiles) throws IOException
    {
        // We use the raw path info because we want to allow leading slashes, i.e. if the URL is
        // pulse/api/fs/local//my/path then path should be /my/path (not my/path).
        String pathString = StringUtils.stripPrefix(request.getPathInfo(), "/fs/local/");

        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);

        FileSystem fileSystem = FileSystems.getDefault();
        FileModel[] models;
        if (StringUtils.stringSet(pathString))
        {
            Path path = fileSystem.getPath(pathString);
            if (Files.isDirectory(path))
            {
                try (DirectoryStream<Path> dir = Files.newDirectoryStream(path, new DirectoryStream.Filter<Path>()
                {
                    @Override
                    public boolean accept(Path entry) throws IOException
                    {
                        return showFiles || Files.isDirectory(entry);
                    }
                }))
                {
                    models = Iterables.toArray(Iterables.transform(dir, new Function<Path, FileModel>()
                    {
                        @Override
                        public FileModel apply(Path input)
                        {
                            return new FileModel(input.getFileName().toString(), Files.isDirectory(input));
                        }
                    }), FileModel.class);
                }
                catch (AccessDeniedException e)
                {
                    models = new FileModel[0];
                }
            }
            else
            {
                throw new IllegalArgumentException("Path '" + pathString + "' does not refer to a directory");
            }
        }
        else
        {
            Iterable<Path> roots = fileSystem.getRootDirectories();
            models = Iterables.toArray(Iterables.transform(roots, new Function<Path, FileModel>()
            {
                @Override
                public FileModel apply(Path input)
                {
                    Path fileName = input.getFileName();
                    return new FileModel(fileName == null ? "/" : fileName.toString(), true);
                }
            }), FileModel.class);
        }

        return new ResponseEntity<>(models, HttpStatus.OK);
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String getHome()
    {
        return FileSystemUtils.normaliseSeparators(System.getProperty(EnvConfig.USER_HOME, ""));
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}