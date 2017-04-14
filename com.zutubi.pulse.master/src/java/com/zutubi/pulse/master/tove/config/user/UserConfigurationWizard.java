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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.model.WizardModel;
import com.zutubi.tove.ui.wizards.ConfigurationWizard;
import com.zutubi.tove.ui.wizards.WizardContext;
import com.zutubi.tove.ui.wizards.WizardModelBuilder;

/**
 * Customised interface for creating users that handles password confirmation and creation of a
 * default contact point.
 */
public class UserConfigurationWizard implements ConfigurationWizard
{
    private WizardModelBuilder wizardModelBuilder;

    @Override
    public WizardModel buildModel(CompositeType type, FormContext context) throws TypeException
    {
        WizardModel model = new WizardModel();
        model.appendStep(wizardModelBuilder.buildStepForClass("", UserConfigurationCreator.class, context));
        return model;
    }

    @Override
    public MutableRecord buildRecord(CompositeType type, WizardContext wizardContext) throws TypeException
    {
        CompositeType actualType = wizardModelBuilder.typeCheck(wizardContext.getModels(), "", UserConfigurationCreator.class);
        MutableRecord record = wizardModelBuilder.buildRecord(wizardContext.getTemplateParentRecord(), wizardContext.getTemplateOwnerPath(), actualType, wizardContext.getModels().get(""));
        UserConfigurationCreator creator = (UserConfigurationCreator) wizardModelBuilder.buildAndValidateTransientInstance(actualType, wizardContext.getParentPath(), wizardContext.getBaseName(), record);
        UserConfiguration userConfiguration = creator.create();
        return type.unstantiate(userConfiguration, wizardContext.getTemplateOwnerPath());
    }

    public void setWizardModelBuilder(WizardModelBuilder wizardModelBuilder)
    {
        this.wizardModelBuilder = wizardModelBuilder;
    }
}