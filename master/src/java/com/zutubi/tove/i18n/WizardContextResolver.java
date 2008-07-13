package com.zutubi.tove.i18n;

import com.zutubi.i18n.context.*;
import com.zutubi.tove.wizard.TypeWizardState;
import com.zutubi.tove.wizard.webwork.AbstractTypeWizard;

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
    private PackageContextResolver delegatePackageContextResolver = new PackageContextResolver();

    public String[] resolve(WizardContext context)
    {
        List<String> resolvedNames = new LinkedList<String>();

        AbstractTypeWizard wizard = context.getWizard();
        TypeWizardState currentState = wizard.getCurrentState();

        String wizardTypeResourceName = wizard.getType().getClazz().getName();
        wizardTypeResourceName = wizardTypeResourceName.replace('.', '/');
        wizardTypeResourceName = wizardTypeResourceName + ".wizard";
        resolvedNames.add(wizardTypeResourceName);

        resolveUsingDelegateResolver(new ClassContext(wizard.getClass()), resolvedNames);
        resolveUsingDelegateResolver(new ClassContext(currentState.getType().getClazz()), resolvedNames);
        resolveUsingDelegateResolver(new ClassContext(wizard.getType().getClazz()), resolvedNames);
        resolveUsingDelegateResolver(new PackageContext(currentState.getType().getClazz()), resolvedNames);
        resolveUsingDelegateResolver(new PackageContext(wizard.getType().getClazz()), resolvedNames);

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    private void resolveUsingDelegateResolver(ClassContext context, List<String> resolvedNames)
    {
        resolvedNames.addAll(Arrays.asList(delegateClassResolver.resolve(context)));
    }

    private void resolveUsingDelegateResolver(PackageContext context, List<String> resolvedNames)
    {
        resolvedNames.addAll(Arrays.asList(delegatePackageContextResolver.resolve(context)));
    }

    public Class<WizardContext> getContextType()
    {
        return WizardContext.class;
    }
}
