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

import java.util.Set;

/**
 * Filters availability of a wizard step type based on the selected type in an earlier step.
 */
public class WizardTypeFilter
{
    private String stepKey;
    private Set<String> compatibleTypes;

    public WizardTypeFilter(String stepKey, Set<String> compatibleTypes)
    {
        this.stepKey = stepKey;
        this.compatibleTypes = compatibleTypes;
    }

    public String getStepKey()
    {
        return stepKey;
    }

    public Set<String> getCompatibleTypes()
    {
        return compatibleTypes;
    }
}
