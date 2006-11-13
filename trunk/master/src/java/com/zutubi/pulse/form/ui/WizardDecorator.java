package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.descriptor.*;
import com.zutubi.pulse.form.FieldType;
import com.zutubi.pulse.wizard.WizardTransition;
import com.zutubi.pulse.wizard.Wizard;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class WizardDecorator
{
    private String state;

    private List<WizardTransition> transitions;

    public WizardDecorator(Wizard wizard)
    {
        state = wizard.getCurrentState().getClass().getName();
        transitions = wizard.getAvailableActions();
    }

    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        // each field has onkeypress handler: onkeypress=return submitenter(this,event)

        String defaultAction = "next";
        if (transitions.contains(WizardTransition.FINISH))
        {
            defaultAction = "finish";
        }

        for (FieldDescriptor fieldDescriptor : descriptor.getFieldDescriptors())
        {
            fieldDescriptor.getParameters().put("onkeypress", "return submitenter(this, event, '"+defaultAction+"')");
        }

        // wizard has extra fields.
        DefaultFieldDescriptor submitField = new DefaultFieldDescriptor();
        submitField.setFieldType(FieldType.HIDDEN);
        submitField.setName("submit");
        submitField.setType(String.class);
        descriptor.addFieldDescriptor(submitField);

        DefaultFieldDescriptor stateField = new DefaultFieldDescriptor();
        stateField.setFieldType(FieldType.HIDDEN);
        stateField.setName("state");
        stateField.getParameters().put("value", state);
        stateField.setType(String.class);
        descriptor.addFieldDescriptor(stateField);

        List<ActionDescriptor> actionDescriptors = new LinkedList<ActionDescriptor>();
        if (transitions.contains(WizardTransition.PREVIOUS))
        {
            actionDescriptors.add(new DefaultActionDescriptor(ActionDescriptor.PREVIOUS));
        }
        if (transitions.contains(WizardTransition.NEXT))
        {
            actionDescriptors.add(new DefaultActionDescriptor(ActionDescriptor.NEXT));
        }
        if (transitions.contains(WizardTransition.FINISH))
        {
            actionDescriptors.add(new DefaultActionDescriptor(ActionDescriptor.FINISH));
        }
        if (transitions.contains(WizardTransition.CANCEL))
        {
            actionDescriptors.add(new DefaultActionDescriptor(ActionDescriptor.CANCEL));
        }

        descriptor.setActionDescriptors(actionDescriptors);

        // wizard has a different heading.
        // set heading...

        return descriptor;
    }
}
