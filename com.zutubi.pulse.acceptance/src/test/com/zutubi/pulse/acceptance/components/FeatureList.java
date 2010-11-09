package com.zutubi.pulse.acceptance.components;

import com.zutubi.pulse.acceptance.SeleniumBrowser;

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
    protected String getPresentExpression()
    {
        return getComponentJS() + ".data != null";
    }
}
