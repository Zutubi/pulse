package com.zutubi.pulse.master.rest.controllers.main;

import com.zutubi.pulse.master.rest.model.MoveModel;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for template hierarchy refactoring actions.
 */
@RestController
@RequestMapping("/hierarchy")
public class ConfigHierarchyController
{
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private ConfigurationRefactoringManager configurationRefactoringManager;

    @RequestMapping(value = "previewMove", method = RequestMethod.POST)
    public ResponseEntity<MoveModel> previewMove(@RequestBody MoveModel body) throws Exception
    {
        validateMove(body);
        ConfigurationRefactoringManager.MoveResult moveResult = configurationRefactoringManager.previewMove(PathUtils.getPath(body.getScope(), body.getKey()), body.getNewParentKey());
        return new ResponseEntity<>(new MoveModel(body, moveResult.getDeletedPaths()), HttpStatus.OK);
    }

    @RequestMapping(value = "move", method = RequestMethod.POST)
    public ResponseEntity<MoveModel> move(@RequestBody MoveModel body) throws Exception
    {
        validateMove(body);
        ConfigurationRefactoringManager.MoveResult moveResult = configurationRefactoringManager.move(PathUtils.getPath(body.getScope(), body.getKey()), body.getNewParentKey());
        return new ResponseEntity<>(new MoveModel(body, moveResult.getDeletedPaths()), HttpStatus.OK);
    }

    private void validateMove(MoveModel model)
    {
        // Note this is basic checks, the refactoring manager handles full validation.
        String scope = model.getScope();
        if (!StringUtils.stringSet(scope))
        {
            throw new IllegalArgumentException("Scope is required");
        }

        if (!configurationTemplateManager.getTemplateScopes().contains(scope))
        {
            throw new IllegalArgumentException("'" + scope + "' is not a templated scope");
        }

        String key = model.getKey();
        if (!StringUtils.stringSet(key))
        {
            throw new IllegalArgumentException("Key is required");
        }
    }
}
