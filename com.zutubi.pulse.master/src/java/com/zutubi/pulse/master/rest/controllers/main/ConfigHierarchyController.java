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

package com.zutubi.pulse.master.rest.controllers.main;

import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.ui.model.MoveModel;
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
