package com.cinnamonbob.core;

import nu.xom.Element;

/**
 * 
 *
 */
public class Schedule
{
    private String cronSchedule;

    public Schedule(ConfigContext context, Element element, Project parent) throws ConfigException
    {
        cronSchedule = XMLConfigUtils.getAttributeValue(context, element, "frequency");
    }

    public String getCronSchedule()
    {
        return cronSchedule;
    }

}
