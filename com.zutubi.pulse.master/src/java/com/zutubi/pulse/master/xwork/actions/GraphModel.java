/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
