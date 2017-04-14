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

package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmFile;

import java.util.*;

/**
 *
 *
 */
public class ExpectedTestResults
{
    private List<Changelist> changelists;

    private List<Revision> revisions;
    private String pathPrefix = "";
    private boolean versionedDirectorySupport = false;

    public ExpectedTestResults(List<Revision> revisions)
    {
        this(revisions, "");
    }

    public ExpectedTestResults(List<Revision> revisions, String pathPrefix)
    {
        if (revisions.size() != 5)
        {
            throw new IllegalArgumentException("expecting 5 revisions in the test data.");
        }

        this.revisions = revisions;

        if (pathPrefix == null)
        {
            pathPrefix = "";
        }
        else if (pathPrefix.equals(""))
        {
            // noop.
        }
        else if (!pathPrefix.endsWith("/"))
        {
            pathPrefix = pathPrefix + "/";
        }

        this.pathPrefix = pathPrefix;

        setupResults();
    }

    private void setupResults()
    {
        changelists = new LinkedList<Changelist>();

        List<FileChange> changes = new LinkedList<FileChange>();
        changes.add(new FileChange(pathPrefix + "project/README.txt", null, FileChange.Action.ADD));
        changes.add(new FileChange(pathPrefix + "project/src/Src.java", null, FileChange.Action.ADD));
        changes.add(new FileChange(pathPrefix + "project/test/Test.java", null, FileChange.Action.ADD));
        if (versionedDirectorySupport)
        {
            changes.add(new FileChange("project", null, FileChange.Action.ADD, true));
            changes.add(new FileChange("project/src", null, FileChange.Action.ADD, true));
            changes.add(new FileChange("project/test", null, FileChange.Action.ADD, true));
        }

        changelists.add(new Changelist(revisions.get(0), 0, null, null, changes));

        changes.clear();

        changes.add(new FileChange(pathPrefix + "project/build.xml", null, FileChange.Action.ADD));
        changes.add(new FileChange(pathPrefix + "project/src/com/Com.java", null, FileChange.Action.ADD));
        changes.add(new FileChange(pathPrefix + "project/src/com/package.properties", null, FileChange.Action.ADD));
        changes.add(new FileChange(pathPrefix + "project/README.txt", null, FileChange.Action.EDIT));
        changes.add(new FileChange(pathPrefix + "project/src/Src.java", null, FileChange.Action.EDIT));
        if (versionedDirectorySupport)
        {
            changes.add(new FileChange("project/src/com", null, FileChange.Action.ADD, true));
        }

        changelists.add(new Changelist(revisions.get(1), 0, null, null, changes));

        changes.clear();

        changes.add(new FileChange(pathPrefix + "project/README.txt", null, FileChange.Action.EDIT));
        changes.add(new FileChange(pathPrefix + "project/src/Src.java", null, FileChange.Action.EDIT));
        changes.add(new FileChange(pathPrefix + "project/src/com/package.properties", null, FileChange.Action.EDIT));
        changes.add(new FileChange(pathPrefix + "project/test/Test.java", null, FileChange.Action.EDIT));

        changelists.add(new Changelist(revisions.get(2), 0, null, null, changes));

        changes.clear();

        changes.add(new FileChange(pathPrefix + "project/README.txt", null, FileChange.Action.EDIT));
        changes.add(new FileChange(pathPrefix + "project/src/Src.java", null, FileChange.Action.DELETE));

        changelists.add(new Changelist(revisions.get(3), 0, null, null, changes));

        changes.clear();

        changes.add(new FileChange(pathPrefix + "project/README.txt", null, FileChange.Action.EDIT));
        changes.add(new FileChange(pathPrefix + "project/test/Test.java", null, FileChange.Action.EDIT));
        changelists.add(new Changelist(revisions.get(4), 0, null, null, changes));
    }

    public void setVersionDirectorySupport(boolean b)
    {
        if (b != this.versionedDirectorySupport)
        {
            this.versionedDirectorySupport = b;
            setupResults();
        }
    }

    public Revision getRevision(int index)
    {
        return revisions.get(index);
    }

    public List<ScmFile> getFilesFor(Revision revision)
    {
        int revisionIndex = revisions.indexOf(revision);

        List<ScmFile> files = new LinkedList<ScmFile>();

        int currentRevision = 0;

        do
        {
            Changelist changelist = changelists.get(currentRevision);

            // apply changelist to files.
            for (FileChange change : changelist.getChanges())
            {
                switch (change.getAction())
                {
                    case ADD:
                        files.add(new ScmFile(change.getPath(), change.isDirectory()));
                        break;
                    case DELETE:
                        files.remove(new ScmFile(change.getPath(), change.isDirectory()));
                        break;
                }
            }
            currentRevision++;
        }
        while (currentRevision != (revisionIndex + 1));

        return files;
    }

    public List<ScmFile> browse(String path)
    {
        List<ScmFile> files = getFilesFor(revisions.get(revisions.size() - 1));

        // special case: directly path defines a file.
        if (path != null && !path.equals("")) // only works while there are no directories in the files.
        {
            for (ScmFile file : files)
            {
                if (file.getPath().equals(path))
                {
                    if (!file.isDirectory())
                    {
                        // if path represents a file, return it
                        return Arrays.asList(new ScmFile(path));
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }

        List<ScmFile> results = new LinkedList<ScmFile>();

        ScmFile parent = null;
        if (path != null && !path.equals(""))
        {
            parent = new ScmFile(path, true);
        }
        // get everything without a parent.
        for (ScmFile file : files)
        {
            while (file.getParentFile() != null)
            {
                if (file.getParentFile().equals(parent))
                {
                    break;
                }
                file = file.getParentFile();
            }

            if (file.getParentFile() == null && parent == null || file.getParentFile() != null && file.getParentFile().equals(parent))
            {
                if (!results.contains(file))
                {
                    results.add(file);
                }
            }
        }
        return results;
    }

    public List<Revision> getRevisions(Revision from, Revision to)
    {
        int fromIndex = (from != null) ? revisions.indexOf(from) + 1 : 0;
        int toIndex = (to != null) ? revisions.indexOf(to) + 1 : revisions.size();
        if (fromIndex == toIndex)
        {
            return Arrays.asList(revisions.get(fromIndex));
        }
        return revisions.subList(fromIndex, toIndex);
    }

    public Revision getLatestRevision()
    {
        return revisions.get(revisions.size() - 1);
    }

    public Changelist getChange(Revision rev)
    {
        return changelists.get(revisions.indexOf(rev));
    }

    public List<Changelist> getChanges(Revision from, Revision to)
    {
        int fromIndex = (from != null) ? revisions.indexOf(from) + 1 : 0;
        int toIndex = (to != null) ? revisions.indexOf(to) + 1 : revisions.size();
        if (fromIndex == toIndex)
        {
            return Arrays.asList(changelists.get(fromIndex));
        }
        return changelists.subList(fromIndex, toIndex);
    }

    public Changelist getAggregatedChanges(Revision from, Revision to)
    {
        // record the latest of each of the changes in the selected changelists
        List<Changelist> changelists = getChanges(from, to);
        Map<String, FileChange> latestChanges = new HashMap<String, FileChange>();
        for (Changelist changelist : changelists)
        {
            for (FileChange change : changelist.getChanges())
            {
                String file = change.getPath();
                if (latestChanges.containsKey(file))
                {
                    switch (latestChanges.get(file).getAction())
                    {
                        case ADD:
                            if (change.getAction() == FileChange.Action.DELETE)
                            {
                                latestChanges.remove(file);
                            }
                            break;
                        case EDIT:
                            if (change.getAction() == FileChange.Action.DELETE)
                            {
                                latestChanges.put(file, change);
                            }
                            break;
                        case DELETE:
                            // re-adding? what happens when a file goes, and comes back - how is it represented?
                            break;
                    }
                }
                else
                {
                    latestChanges.put(file, change);
                }
            }
        }

        return new Changelist(new Revision("test"), 0, null, null, latestChanges.values());
    }

    public List<ScmFile> browse(String path, Revision rev)
    {
        throw new RuntimeException("Not Yet Implemented.");
    }
}
