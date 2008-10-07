package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmFile;
import com.zutubi.pulse.core.scm.api.Change;

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
        changelists.add(new Changelist(revisions.get(0), 0, null, null));
        changelists.add(new Changelist(revisions.get(1), 0, null, null));
        changelists.add(new Changelist(revisions.get(2), 0, null, null));
        changelists.add(new Changelist(revisions.get(3), 0, null, null));
        changelists.add(new Changelist(revisions.get(4), 0, null, null));


        Changelist cl1 = changelists.get(0);
        cl1.addChange(new Change(pathPrefix + "project/README.txt", null, Change.Action.ADD));
        cl1.addChange(new Change(pathPrefix + "project/src/Src.java", null, Change.Action.ADD));
        cl1.addChange(new Change(pathPrefix + "project/test/Test.java", null, Change.Action.ADD));
        if (versionedDirectorySupport)
        {
            cl1.addChange(new Change("project", null, Change.Action.ADD, true));
            cl1.addChange(new Change("project/src", null, Change.Action.ADD, true));
            cl1.addChange(new Change("project/test", null, Change.Action.ADD, true));
        }

        Changelist cl2 = changelists.get(1);
        cl2.addChange(new Change(pathPrefix + "project/build.xml", null, Change.Action.ADD));
        cl2.addChange(new Change(pathPrefix + "project/src/com/Com.java", null, Change.Action.ADD));
        cl2.addChange(new Change(pathPrefix + "project/src/com/package.properties", null, Change.Action.ADD));
        cl2.addChange(new Change(pathPrefix + "project/README.txt", null, Change.Action.EDIT));
        cl2.addChange(new Change(pathPrefix + "project/src/Src.java", null, Change.Action.EDIT));
        if (versionedDirectorySupport)
        {
            cl2.addChange(new Change("project/src/com", null, Change.Action.ADD, true));
        }

        Changelist cl3 = changelists.get(2);
        cl3.addChange(new Change(pathPrefix + "project/README.txt", null, Change.Action.EDIT));
        cl3.addChange(new Change(pathPrefix + "project/src/Src.java", null, Change.Action.EDIT));
        cl3.addChange(new Change(pathPrefix + "project/src/com/package.properties", null, Change.Action.EDIT));
        cl3.addChange(new Change(pathPrefix + "project/test/Test.java", null, Change.Action.EDIT));

        Changelist cl4 = changelists.get(3);
        cl4.addChange(new Change(pathPrefix + "project/README.txt", null, Change.Action.EDIT));
        cl4.addChange(new Change(pathPrefix + "project/src/Src.java", null, Change.Action.DELETE));

        Changelist cl5 = changelists.get(4);
        cl5.addChange(new Change(pathPrefix + "project/README.txt", null, Change.Action.EDIT));
        cl5.addChange(new Change(pathPrefix + "project/test/Test.java", null, Change.Action.EDIT));
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
            for (Change change : changelist.getChanges())
            {
                switch (change.getAction())
                {
                    case ADD:
                        files.add(new ScmFile(change.getFilename(), change.isDirectory()));
                        break;
                    case DELETE:
                        files.remove(new ScmFile(change.getFilename(), change.isDirectory()));
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
        Map<String, Change> latestChanges = new HashMap<String, Change>();
        for (Changelist changelist : changelists)
        {
            for (Change change : changelist.getChanges())
            {
                String file = change.getFilename();
                if (latestChanges.containsKey(file))
                {
                    switch (latestChanges.get(file).getAction())
                    {
                        case ADD:
                            if (change.getAction() == Change.Action.DELETE)
                            {
                                latestChanges.remove(file);
                            }
                            break;
                        case EDIT:
                            if (change.getAction() == Change.Action.DELETE)
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

        Changelist result = new Changelist(null, 0, null, null);
        for (Change change : latestChanges.values())
        {
            result.addChange(change);
        }
        return result;
    }

    public List<ScmFile> browse(String path, Revision rev)
    {
        throw new RuntimeException("Not Yet Implemented.");
    }
}
