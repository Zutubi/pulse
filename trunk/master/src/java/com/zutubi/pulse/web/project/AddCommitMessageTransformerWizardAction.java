package com.zutubi.pulse.web.project;

import com.zutubi.pulse.web.wizard.auto.WizardAction;
import com.zutubi.pulse.committransformers.CommitMessageTransformerManager;
import com.zutubi.pulse.committransformers.AddCommitMessageTransformerWizard;
import com.zutubi.pulse.model.CommitMessageTransformer;

import java.util.List;
import java.util.LinkedList;

/**
 * <class comment/>
 */
public class AddCommitMessageTransformerWizardAction extends WizardAction
{
    private CommitMessageTransformerManager transformerManager;

    private List<String> existingOptions;

    public List<String> getExistingOptions()
    {
        if (existingOptions == null)
        {
            existingOptions = new LinkedList<String>();
            long projectId = ((AddCommitMessageTransformerWizard)getWizardInstance()).getProjectId();
                    
            for (CommitMessageTransformer t : transformerManager.getCommitMessageTransformers())
            {
                // filter out the transformers that are already applied to this project.
                if (!t.getProjects().contains(projectId))
                {
                    existingOptions.add(t.getName());
                }
            }
        }
        return existingOptions;
    }

    public void setCommitMessageTransformerManager(CommitMessageTransformerManager commitMessageTransformerManager)
    {
        this.transformerManager = commitMessageTransformerManager;
    }
}
