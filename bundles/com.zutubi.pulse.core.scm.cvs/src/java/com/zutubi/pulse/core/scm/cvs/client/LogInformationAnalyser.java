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

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LogInformationAnalyser.java

package com.zutubi.pulse.core.scm.cvs.client;

import com.google.common.base.Function;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.cvs.CvsClient;
import com.zutubi.pulse.core.scm.cvs.CvsRevision;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;


/**
 * The log analyser helps process the cvs log output.
 */
public class LogInformationAnalyser
{
    //-------------------------------------------------------------------------
    // change set analysis:
    // - cvs changes are not atomic. therefore,
    //    - a change set does not need to occur at the same time
    //    - multiple changesets can be interlevered.
    // characteristics of changesets:
    // - a) single author.
    // - b) single commit statement.
    // - c) each file appears only once.
    // - d) changeset bound to a single branch.
    // - e) contiguous block of time.

    // group by (author,branch,comment)

    private CVSRoot root;

    public LogInformationAnalyser(CVSRoot root)
    {
        this.root = root;
    }

    public Revision latestUpdate(List<LogInformation> rlogResponse)
    {
        // here, we are not interested in the branch/tag of the revision, so use null.
        List<Revision> revisions = extractRevisions(rlogResponse, null);
        if (revisions.size() == 0)
        {
            return null;
        }
        // need to ensure that the log information is ordered by date...
        Collections.sort(revisions, new RevisionDateComparator());

        return revisions.get(revisions.size() - 1);
    }


    /**
     * Process the response from the rlog command and extract the cvs revisions.
     *
     * @param logInformation is the response from the rlog command
     * @param tag is the tag name for which the rlog command was executed. This tag is
     * used when constructing the revision objects since the branch information is not
     * immediately obvious. (read difficult to determine)
     *
     * @return a list of cvs revisions`
     */
    private List<Revision> extractRevisions(List<LogInformation> logInformation, String tag)
    {
        // The rlog response will contain a reference for each relevant file. Those
        // files that have changed will contain revisions that we will extract and analyse.
        List<Revision> revisions = new LinkedList<Revision>();

        for (LogInformation logInfo : logInformation)
        {
            for (Object obj : logInfo.getRevisionList())
            {
                LogInformation.Revision rev = (LogInformation.Revision) obj;

                // CIB-831: Ignore revisions that indicate deleted as being the first state for the file.
                //          This happens when a file is added to a branch for the first time - that same file
                //          appears on head in the Attic. It should be safe to ignore.
                if ("dead".equals(rev.getState()) && rev.getNumber().endsWith(".1"))
                {
                    continue;
                }

                Revision revision = new Revision(rev, root);
                revision.setTag(tag);
                revisions.add(revision);
            }
        }

        return revisions;
    }

    public List<Changelist> extractChangelists(List<LogInformation> rlogResponse, String tag)
    {
        // retrieve the log info for all of the files that have been modified.
        List<Revision> simpleChanges = extractRevisions(rlogResponse, tag);

        // group by author, branch, sort by date. this will have the affect of grouping
        // all of the changes in a single changeset together, ordered by date.
        Collections.sort(simpleChanges, new Comparator<Revision>()
        {
            public int compare(Revision changeA, Revision changeB)
            {
                int comparison = changeA.getAuthor().compareTo(changeB.getAuthor());
                if (comparison != 0)
                {
                    return comparison;
                }
                // tags should never be different.
                comparison = changeA.getTag().compareTo(changeB.getTag());
                if (comparison != 0)
                {
                    return comparison;
                }
                return changeA.getDate().compareTo(changeB.getDate());
            }
        });

        // create change sets by author. ie: each change set object will contain
        // all of the changes made by a particular author.
        List<LocalChangeSet> changeSets = new LinkedList<LocalChangeSet>();
        LocalChangeSet changeSet = null;
        for (Revision change : simpleChanges)
        {
            if (changeSet == null)
            {
                changeSet = new LocalChangeSet(change);
            }
            else
            {
                if (changeSet.belongsTo(change))
                {
                    changeSet.add(change);
                }
                else
                {
                    changeSets.add(changeSet);
                    changeSet = new LocalChangeSet(change);
                }
            }
        }
        if (changeSet != null)
        {
            changeSets.add(changeSet);
        }

        // refine the changesets, splitting it up according to file names. ie: duplicate filenames
        // should trigger a new changeset.
        List<LocalChangeSet> refinedSets = new LinkedList<LocalChangeSet>();
        for (LocalChangeSet set : changeSets)
        {
            refinedSets.addAll(set.refine());
        }

        // now that we have the changeset information, lets create the final product.
        List<Changelist> changelists = new LinkedList<Changelist>();
        for (LocalChangeSet set : refinedSets)
        {
            List<Revision> localChanges = set.getChanges();
            // we use the last change because it has the most recent date. all the other information is
            // is common to all the changes.
            Revision lastChange = null;
            for (int i = localChanges.size() - 1; 0 <= i; i--)
            {
                // CIB-1627: we want the last change that contains a date.
                lastChange = localChanges.get(i);
                if (lastChange.getDate() != null)
                {
                    break;
                }
            }

            assert(lastChange != null);
            CvsRevision rev = new CvsRevision(lastChange.getAuthor(), lastChange.getTag(), lastChange.getMessage(), lastChange.getDate());
            Changelist changelist = new Changelist(
                    CvsClient.convertRevision(rev),
                    lastChange.getDate().getTime(),
                    lastChange.getAuthor(),
                    lastChange.getMessage(),
                    newArrayList(transform(localChanges, new Function<Revision, FileChange>()
                    {
                        public FileChange apply(Revision revision)
                        {
                            return new FileChange(revision.getFilename(), new com.zutubi.pulse.core.scm.api.Revision(revision.getRevision()), revision.getAction());
                        }
                    })
            ));
            changelists.add(changelist);
        }

        return changelists;
    }

    /**
     * Simple value object used to help store data during the changeset analysis process.
     */
    private static class LocalChangeSet
    {
        private final List<Revision> changes = new LinkedList<Revision>();

        LocalChangeSet(Revision c)
        {
            changes.add(c);
        }

        void add(Revision c)
        {
            changes.add(c);
        }

        boolean belongsTo(Revision otherChange)
        {
            if (changes.size() == 0)
            {
                return true;
            }

            Revision previousChange = changes.get(0);
            return previousChange.getAuthor().equals(otherChange.getAuthor()) &&
                    previousChange.getTag().equals(otherChange.getTag()) &&
                    previousChange.getMessage().equals(otherChange.getMessage());
        }

        public List<LocalChangeSet> refine()
        {
            Map<String, String> filenames = new HashMap<String, String>();
            List<LocalChangeSet> changesets = new LinkedList<LocalChangeSet>();

            LocalChangeSet changeSet = null;
            for (Revision change : changes)
            {
                if (filenames.containsKey(change.getFilename()))
                {
                    // time for a new changeset.
                    filenames.clear();
                    changesets.add(changeSet);
                    filenames.put(change.getFilename(), change.getFilename());
                    changeSet = new LocalChangeSet(change);
                }
                else
                {
                    filenames.put(change.getFilename(), change.getFilename());
                    if (changeSet == null)
                    {
                        changeSet = new LocalChangeSet(change);
                    }
                    else
                    {
                        changeSet.add(change);
                    }
                }
            }
            if (changeSet != null)
            {
                changesets.add(changeSet);
            }
            return changesets;
        }

        public List<Revision> getChanges()
        {
            return changes;
        }
    }

    public class Revision
    {
        private LogInformation.Revision log;

        private String tag;

        private CVSRoot root;

        public Revision(LogInformation.Revision log, CVSRoot root)
        {
            if (log == null)
            {
                throw new IllegalArgumentException("Log Information cannot be null.");
            }
            this.log = log;
            this.root = root;
        }

        public String getAuthor()
        {
            return log.getAuthor();
        }

        public String getRevision()
        {
            return log.getNumber();
        }

        public String getTag()
        {
            if (tag == null)
            {
                return "";
            }
            return tag;
        }

        public void setTag(String branch)
        {
            this.tag = branch;
        }

        public Date getDate()
        {
            return log.getDate();
        }

        public String getMessage()
        {
            return log.getMessage();
        }

        public String getFilename()
        {
            // need to process the filename.

            String filename = log.getLogInfoHeader().getRepositoryFilename();

            // remove the ,v
            if (filename.endsWith(",v"))
            {
                filename = filename.substring(0, filename.length() - 2);
            }

            // remove the repo root.
            if (filename.startsWith(root.getRepository()))
            {
                filename = filename.substring(root.getRepository().length());
            }

            return filename;
        }

        public FileChange.Action getAction()
        {
            if (log.getAddedLines() == 0 && log.getRemovedLines() == 0)
            {
                if (!log.getState().equalsIgnoreCase("dead"))
                {
                    return FileChange.Action.ADD;
                }
                return FileChange.Action.DELETE;
            }
            return FileChange.Action.EDIT;
        }

    }

    private class RevisionDateComparator implements Comparator<Revision>
    {
        public int compare(Revision o1, Revision o2)
        {
            return o1.getDate().compareTo(o2.getDate());
        }
    }
}
