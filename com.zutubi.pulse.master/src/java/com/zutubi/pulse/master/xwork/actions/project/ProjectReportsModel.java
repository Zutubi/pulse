package com.zutubi.pulse.master.xwork.actions.project;

import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Defines JSON structure for the project reports page.
 */
public class ProjectReportsModel
{
    private String group;
    private List<String> groupNames;
    private int timeFrame;
    private String timeUnit;
    private int buildCount;
    private List<ReportModel> reports = new LinkedList<ReportModel>();

    public ProjectReportsModel(String group, List<String> groupNames, int timeFrame, String timeUnit, int buildCount)
    {
        this.group = group;
        this.groupNames = groupNames;
        this.timeFrame = timeFrame;
        this.timeUnit = timeUnit;
        this.buildCount = buildCount;
    }

    public String getGroup()
    {
        return group;
    }

    @JSON
    public List<String> getGroupNames()
    {
        return groupNames;
    }

    public int getTimeFrame()
    {
        return timeFrame;
    }

    public String getTimeUnit()
    {
        return timeUnit;
    }

    public int getBuildCount()
    {
        return buildCount;
    }

    @JSON
    public List<ReportModel> getReports()
    {
        return reports;
    }

    public void addReport(ReportModel report)
    {
        reports.add(report);
    }
    
    public static class ReportModel
    {
        private String location;
        private int width;
        private int height;
        private String imageMap;
        private String imageMapName;

        public ReportModel(Map report)
        {
            location = (String) report.get("location");
            width = (Integer) report.get("width");
            height = (Integer) report.get("height");
            imageMap = (String) report.get("imageMap");
            imageMapName = (String) report.get("imageMapName");
        }

        public String getLocation()
        {
            return location;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }

        public String getImageMap()
        {
            return imageMap;
        }

        public String getImageMapName()
        {
            return imageMapName;
        }
    }
}
