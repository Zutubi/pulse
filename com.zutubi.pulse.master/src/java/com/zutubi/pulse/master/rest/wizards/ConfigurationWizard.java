package com.zutubi.pulse.master.rest.wizards;

import com.zutubi.pulse.master.rest.model.CompositeModel;
import com.zutubi.pulse.master.rest.model.WizardModel;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;

import java.util.Map;

/**
 * Wizards present a custom API for creating new instances of a configuration type.  To customise
 * the Wizard for a configuration class MyConfiguration, create a class in this package named
 * MyConfigurationWizard that implements this interface.
 *
 * FIXME kendo its more flexible and conventional for these classes to be in the same package as
 * the configuration class, but this requires the models to move down too. Perhaps once a few
 * wizards are implemented this will be a worthwhile change.
 */
public interface ConfigurationWizard
{
    WizardModel buildModel(CompositeType type, String parentPath, String baseName, boolean concrete) throws TypeException;
    MutableRecord buildRecord(CompositeType type, String parentPath, String baseName, String templateOwnerPath, boolean concrete, Map<String, CompositeModel> models) throws TypeException;
}
