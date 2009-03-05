package com.zutubi.pulse.master.tove.i18n;

import com.zutubi.i18n.context.Context;
import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizard;

import java.io.InputStream;

/**
 *
 *
 */
public class WizardContext implements Context
{
    private AbstractTypeWizard wizard;

    public WizardContext(AbstractTypeWizard wizard)
    {
        this.wizard = wizard;
    }

    public AbstractTypeWizard getWizard()
    {
        return wizard;
    }

    public InputStream getResourceAsStream(String name)
    {
        if (wizard.getCurrentState() != null)
        {
            return wizard.getCurrentState().getType().getClazz().getResourceAsStream(name);
        }
        return wizard.getType().getClazz().getResourceAsStream(name);
    }
}
