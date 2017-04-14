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

package com.zutubi.tove.ui.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract base for steps of a wizard.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(TypedWizardStepModel.class),
        @JsonSubTypes.Type(CustomWizardStepModel.class)
})
public abstract class WizardStepModel
{
    private String key;
    private String label;

    public WizardStepModel(String label, String key)
    {
        this.label = label;
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }
}
