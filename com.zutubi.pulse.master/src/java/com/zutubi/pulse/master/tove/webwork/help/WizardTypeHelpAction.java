package com.zutubi.pulse.master.tove.webwork.help;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizard;
import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizardState;
import com.zutubi.pulse.master.tove.wizard.webwork.ConfigurationWizardAction;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;

/**
 * Looks up the documentation for a type being rendered in a wizard.  Uses
 * the wizard to determine the displayed fields.
 */
public class WizardTypeHelpAction extends TypeHelpActionSupport
{
    private AbstractTypeWizardState state;

    private void ensureState()
    {
        if(state == null)
        {
            AbstractTypeWizard wizardInstance = (AbstractTypeWizard) ConfigurationWizardAction.getWizardInstance(getPath());
            state = (AbstractTypeWizardState) wizardInstance.getCurrentState();
        }
    }

    protected CompositeType getType()
    {
        ensureState();
        return state.getType();
    }

    protected Predicate<TypeProperty> getPropertyPredicate()
    {
        ensureState();
        return new Predicate<TypeProperty>()
        {
            public boolean apply(TypeProperty typeProperty)
            {
                return ToveUtils.isFormField(typeProperty) && state.includesField(getType(), typeProperty.getName());
            }
        };
    }
}
