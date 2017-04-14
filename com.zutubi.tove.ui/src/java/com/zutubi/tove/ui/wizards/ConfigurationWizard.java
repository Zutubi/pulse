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

import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.model.WizardModel;

/**
 * Wizards present a custom API for creating new instances of a configuration type.  To customise
 * the Wizard for a configuration class MyConfiguration, create a class in the same package named
 * MyConfigurationWizard that implements this interface.  If you can't place the class in the same
 * package you can annotate the configuration type with {@link com.zutubi.tove.annotations.Wizard}
 * and set the value to the fully-qualified class name.
 */
public interface ConfigurationWizard
{
    WizardModel buildModel(CompositeType type, FormContext context) throws TypeException;
    MutableRecord buildRecord(CompositeType type, WizardContext wizardContext) throws TypeException;
}
