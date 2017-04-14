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

package com.zutubi.tove.ui.wizards;

import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.tove.ui.model.CompositeModel;

import java.util.Map;

/**
 * Simplifies signatures of wizard methods by bundling together the information collected when a
 * wizard is POSTed.
 */
public class WizardContext
{
    private final String parentPath;
    private final String baseName;
    private final TemplateRecord templateParentRecord;
    private final String templateOwnerPath;
    private final boolean concrete;
    private final Map<String, CompositeModel> models;

    public WizardContext(String parentPath, String baseName, TemplateRecord templateParentRecord, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models)
    {
        this.parentPath = parentPath;
        this.baseName = baseName;
        this.templateParentRecord = templateParentRecord;
        this.templateOwnerPath = templateOwnerPath;
        this.concrete = concrete;
        this.models = models;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public String getBaseName()
    {
        return baseName;
    }

    public TemplateRecord getTemplateParentRecord()
    {
        return templateParentRecord;
    }

    public String getTemplateOwnerPath()
    {
        return templateOwnerPath;
    }

    public boolean isConcrete()
    {
        return concrete;
    }

    public Map<String, CompositeModel> getModels()
    {
        return models;
    }
}
