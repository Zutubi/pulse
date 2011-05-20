package com.zutubi.pulse.master.xwork.actions;

import java.util.Map;

/**
 * JSON data for the Zutubi.pulse.Graph JS component.
 */
public class GraphModel
{
    private String location;
    private int width;
    private int height;
    private String imageMap;
    private String imageMapName;

    public GraphModel(Map report)
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
