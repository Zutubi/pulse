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

package com.zutubi.pulse.core.scm.git;

import java.util.Date;
import java.util.List;
import java.util.LinkedList;

/**
 * The git log entry holds the parsed details of a single commit as represented in
 * the git log output.
 */
class GitLogEntry
{
    /**
     * The raw output from the git log command.  Each entry in the list represents a
     * line of output.
     */
    private List<String> raw;
    /**
     * The unique identifier for this commit.
     */
    private String id = "";
    /**
     * Parent commit ids (merge commits have multiple parents).
     */
    private String[] parents = new String[0];
    /**
     * The author of the this commit.
     */
    private String author = "";
    /**
     * The date of the commit.
     */
    private Date date = null;
    /**
     * The raw string date representation of the commit.
     */
    private String dateString = "";
    /**
     * The comment associated with the commit.
     */
    private String comment = "";
    /**
     * The list of files that were changed/added/removed/renamed etc in the commit.
     */
    private List<FileChangeEntry> files = new LinkedList<FileChangeEntry>();
    
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String[] getParents()
    {
        return parents;
    }

    public void setParents(String[] parents)
    {
        this.parents = parents;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public List<String> getRaw()
    {
        return raw;
    }

    public void setRaw(List<String> raw)
    {
        this.raw = raw;
    }

    public String getDateString()
    {
        return dateString;
    }

    public void setDateString(String strDate)
    {
        this.dateString = strDate;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public void addFileChange(String file, String action)
    {
        files.add(new FileChangeEntry(file, action));
    }

    public List<FileChangeEntry> getFiles()
    {
        return files;
    }

    public static class FileChangeEntry
    {
        private String name;
        private String action;

        public FileChangeEntry(String name, String action)
        {
            this.name = name.trim();
            this.action = action;
        }

        public String getName()
        {
            return name;
        }

        public String getAction()
        {
            return action;
        }
    }
}
