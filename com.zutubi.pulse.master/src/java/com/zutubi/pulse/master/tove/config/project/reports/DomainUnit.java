package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.ConventionSupport;

/**
 * Units that may be used on the domain (horizontal) axis of a chart.
 */
public enum DomainUnit
{
    /**
     * The units are build ids: i.e. each build is show separately.
     */
    BUILD_IDS,
    /**
     * The domain is measured in time, with builds on the same day aggregated
     * together.
     */
    DAYS;

    private static final Messages I18N = Messages.getInstance(DomainUnit.class);

    /**
     * @return a human-readable name for the unit
     */
    public String getLabel()
    {
        return I18N.format(name() + ConventionSupport.I18N_KEY_SUFFIX_LABEL);
    }
}
