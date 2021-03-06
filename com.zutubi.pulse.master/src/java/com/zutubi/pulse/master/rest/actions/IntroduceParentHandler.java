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

package com.zutubi.pulse.master.rest.actions;

import com.zutubi.pulse.master.rest.Validation;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.type.MapType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.ui.model.ActionModel;
import com.zutubi.tove.ui.model.forms.CheckboxFieldModel;
import com.zutubi.tove.ui.model.forms.FormModel;
import com.zutubi.tove.ui.model.forms.TextFieldModel;
import com.zutubi.validation.i18n.MessagesTextProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the introduce parent refactoring.
 */
public class IntroduceParentHandler implements ActionHandler
{
    private static final String FIELD_PARENT_KEY = "parentKey";
    private static final String LABEL_PARENT_KEY = "new parent name";
    private static final String FIELD_PULL_UP = "pullUp";
    private static final String LABEL_PULL_UP = "pull up existing configuration";

    private ConfigurationRefactoringManager configurationRefactoringManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    @Override
    public ActionModel getModel(String path, String variant)
    {
        if (!configurationRefactoringManager.canIntroduceParentTemplate(path))
        {
            throw new IllegalArgumentException("Cannot introduce parent for path '" + path + "'");
        }

        ActionModel model = new ActionModel(ConfigurationRefactoringManager.ACTION_INTRODUCE_PARENT, "introduce parent template", null, true);
        FormModel form = new FormModel();
        Map<String, Object> formDefaults = new HashMap<>();

        form.addField(new TextFieldModel(FIELD_PARENT_KEY, LABEL_PARENT_KEY));
        formDefaults.put(FIELD_PARENT_KEY, PathUtils.getBaseName(path) + " template");

        form.addField(new CheckboxFieldModel(FIELD_PULL_UP, LABEL_PULL_UP));

        model.setForm(form);
        model.setFormDefaults(formDefaults);
        return model;
    }

    private String validateParentKey(String path, Map<String, Object> input)
    {
        if (!configurationRefactoringManager.canIntroduceParentTemplate(path))
        {
            throw new IllegalArgumentException("Cannot introduce parent for path '" + path + "'");
        }

        String parentKey = Validation.getRequiredString(FIELD_PARENT_KEY, LABEL_PARENT_KEY, input);
        try
        {
            String parentPath = PathUtils.getParentPath(path);
            MapType mapType = configurationTemplateManager.getType(parentPath, MapType.class);
            MessagesTextProvider textProvider = new MessagesTextProvider(mapType.getTargetType().getClazz());
            configurationTemplateManager.validateNameIsUnique(parentPath, parentKey, mapType.getKeyProperty(), textProvider);
        }
        catch(com.zutubi.validation.ValidationException e)
        {
            throw Validation.newFieldError(FIELD_PARENT_KEY, e.getMessage());
        }

        return parentKey;
    }

    @Override
    public ActionResult doAction(String path, String variant, Map<String, Object> input)
    {
        String parentKey = validateParentKey(path, input);
        boolean pullUp = Validation.getBoolean(FIELD_PULL_UP, LABEL_PULL_UP, input, false);
        configurationRefactoringManager.introduceParentTemplate(PathUtils.getParentPath(path), Collections.singletonList(PathUtils.getBaseName(path)), parentKey, pullUp);
        return new ActionResult(ActionResult.Status.SUCCESS, "added new parent");
    }

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
