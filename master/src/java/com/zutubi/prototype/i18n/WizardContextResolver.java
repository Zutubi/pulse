package com.zutubi.prototype.i18n;

import com.zutubi.i18n.context.ExtendedClassContext;
import com.zutubi.i18n.context.ContextResolver;
import com.zutubi.i18n.context.ExtendedClassContextResolver;
import com.zutubi.i18n.context.ClassContext;
import com.zutubi.i18n.context.ClassContextResolver;
import com.zutubi.prototype.wizard.TypeWizardState;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;

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
    private ExtendedClassContextResolver delegateExtendedClassResolver = new ExtendedClassContextResolver();

    public String[] resolve(WizardContext context)
    {
        List<String> resolvedNames = new LinkedList<String>();

        AbstractTypeWizard wizard = context.getWizard();
        TypeWizardState currentState = wizard.getCurrentState();

        resolveUsingDelegateResolver(new ClassContext(currentState.getType().getClazz()), resolvedNames);
        resolveUsingDelegateResolver(new ClassContext(wizard.getClass()), resolvedNames);

        String wizardTypeResourceName = wizard.getType().getClazz().getName();
        wizardTypeResourceName = wizardTypeResourceName.replace('.', '/');
        wizardTypeResourceName = wizardTypeResourceName + ".wizard";
        resolvedNames.add(wizardTypeResourceName);

        resolveUsingDelegateResolver(new ExtendedClassContext(wizard.getType().getClazz()), resolvedNames);

        return resolvedNames.toArray(new String[resolvedNames.size()]);
    }

    private void resolveUsingDelegateResolver(ClassContext context, List<String> resolvedNames)
    {
        resolvedNames.addAll(Arrays.asList(delegateClassResolver.resolve(context)));
    }

    private void resolveUsingDelegateResolver(ExtendedClassContext context, List<String> resolvedNames)
    {
        resolvedNames.addAll(Arrays.asList(delegateExtendedClassResolver.resolve(context)));
    }

    public Class<WizardContext> getContextType()
    {
        return WizardContext.class;
    }
}
