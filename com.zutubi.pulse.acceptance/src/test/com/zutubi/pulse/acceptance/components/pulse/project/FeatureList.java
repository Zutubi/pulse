package com.zutubi.pulse.acceptance.components.pulse.project;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.Component;

/**
 * Corresponds to the Zutubi.pulse.project.FeatureList JS component.
 */
public class FeatureList extends Component
{
    public FeatureList(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    @Override
    protected String getPresentScript()
    {
        return "return " + getComponentJS() + ".data != null;";
    }
}
