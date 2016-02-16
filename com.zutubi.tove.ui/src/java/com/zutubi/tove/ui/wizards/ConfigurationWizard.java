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
