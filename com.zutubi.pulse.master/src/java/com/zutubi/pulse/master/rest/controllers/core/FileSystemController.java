package com.zutubi.pulse.master.rest.controllers.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.rest.Utils;
import com.zutubi.pulse.master.rest.model.fs.FileModel;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.TypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

        final String path = Utils.getRequestedPath(request, true);

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

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}