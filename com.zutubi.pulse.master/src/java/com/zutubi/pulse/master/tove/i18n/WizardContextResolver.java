package com.zutubi.pulse.master.tove.i18n;

import com.zutubi.i18n.context.*;
import com.zutubi.pulse.master.tove.wizard.TypeWizardState;
import com.zutubi.pulse.master.tove.wizard.webwork.AbstractTypeWizard;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class WizardContextResolver implements ContextResolver<WizardContext>
{
    private ClassContextResolver delegateClassResolver = new ClassContextResolver();
    private ExtendedPackageContextResolver delegatePackageContextResolver = new ExtendedPackageContextResolver();

    public String[] resolve(WizardContext context)
    {
        List<String> resolvedNames = new LinkedList<String>();

        AbstractTypeWizard wizard = context.getWizard();
        TypeWizardState currentState = wizard.getCurrentState();

        String wizardTypeResourceName = wizard.getType().getClazz().getName();
        wizardTypeResourceName = wizardTypeResourceName.replace('.', '/');
        wizardTypeResourceName = wizardTypeResourceName + ".wizard";
        resolvedNames.add(wizardTypeResourceName);

        resolveUsingDelegateResolver(new ClassContext(wizard.getClass()), delegateClassResolver, resolvedNames);
        resolveUsingDelegateResolver(new ClassContext(currentState.getType().getClazz()), delegateClassResolver, resolvedNames);
        resolveUsingDelegateResolver(new ClassContext(wizard.getType().getClazz()), delegateClassResolver, resolvedNames);
        resolveUsingDelegateResolver(new ClassContext(currentState.getType().getClazz()), delegatePackageContextResolver, resolvedNames);
        resolveUsingDelegateResolver(new ClassContext(wizard.getType().getClazz()), delegatePackageContextResolver, resolvedNames);

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    private void resolveUsingDelegateResolver(Context context, ContextResolver resolver, List<String> resolvedNames)
    {
        resolvedNames.addAll(Arrays.asList(resolver.resolve(context)));
    }

    public Class<WizardContext> getContextType()
    {
        return WizardContext.class;
    }
}
