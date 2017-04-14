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

package com.zutubi.pulse.master.vfs.provider.pulse;

/**
 * Represents an action that a user can perform on a file.  The actions are
 * rendered as linked icons in the UI, and include such things as selecting 
 */
public class FileAction
{
    /**
     * This action url represents the ability to download the file.
     */
    public static final String TYPE_DOWNLOAD = "download";
    /**
     * This action url represents the ability to download an archived version
     * of this file.
     */
    public static final String TYPE_ARCHIVE = "archive";
    /**
     * This action url represents a link to viewing an online representation of
     * the file.
     */
    public static final String TYPE_LINK = "link";
    /**
     * This action url represents the ability to view a decorated version
     * of the file.
     */
    public static final String TYPE_DECORATE = "decorate";
    /**
     * Similar to the download action, but used when the target in HTML where
     * it will be viewed in the browser.
     */
    public static final String TYPE_VIEW = "view";

    /**
     * This action type determines how it is rendered.
     */
    private String type;

    /**
     * The URL for the action link.
     */
    private String url;

    public FileAction(String type, String url)
    {
        this.type = type;
        this.url = url;
    }

    public String getType()
    {
        return type;
    }

    public String getUrl()
    {
        return url;
    }
}
