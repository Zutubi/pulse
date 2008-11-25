package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.ReferenceResolver;
import com.zutubi.pulse.core.engine.api.*;
import com.zutubi.pulse.core.scm.CachingScmClient;
import com.zutubi.pulse.core.scm.CachingScmFile;
import com.zutubi.pulse.core.scm.ScmFileCache;
import com.zutubi.pulse.core.scm.api.*;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;
import com.zutubi.pulse.core.util.process.AsyncProcess;
import com.zutubi.pulse.core.util.process.BufferingCharHandler;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerforceClient extends CachingScmClient
{
    public static final String TYPE = "p4";

    private static final Logger LOG = Logger.getLogger(PerforceClient.class);

    private static final long RESOLVE_COMMAND_TIMEOUT = Long.getLong("pulse.p4.client.command.timeout", 300);

    private PerforceCore core;
    private String templateClient;
    private String resolvedClient;
    private File clientRoot;
    private String port;
    private Pattern syncPattern;
    private List<String> excludedPaths = Collections.emptyList();

    public void setExcludedPaths(List<String> filteredPaths)
    {
        this.excludedPaths = filteredPaths;
    }

    private String resolveClient(Revision revision) throws ScmException
    {
        return resolveClient(revision, true);
    }

    private String resolveClient(Revision revision, boolean cache) throws ScmException
    {
        String resolved;
        if (resolvedClient == null)
        {
            if (templateClient.startsWith("!"))
            {
                String commandLine = templateClient.substring(1);

                HashReferenceMap referenceMap = new HashReferenceMap();
                String revisionSpec = (revision == null) ?  "#head" : "@" + revision.getRevisionString();
                referenceMap.add(new Property("revision.spec", revisionSpec));

                Process p;
                try
                {
                    List<String> command = ReferenceResolver.splitAndResolveReferences(commandLine, referenceMap, ReferenceResolver.ResolutionStrategy.RESOLVE_NON_STRICT);
                    p = Runtime.getRuntime().exec(command.toArray(new String[command.size()]));
                }
                catch(Exception e)
                {
                    throw new ScmException("Error starting template client generation command: " + e.getMessage(), e);
                }

                BufferingCharHandler handler = new BufferingCharHandler();
                AsyncProcess ap = new AsyncProcess(p, handler, false);

                try
                {
                    ap.waitForSuccessOrThrow(RESOLVE_COMMAND_TIMEOUT, TimeUnit.SECONDS);
                    resolved = handler.getStdout().trim();
                }
                catch (Exception e)
                {
                    LOG.severe(e);
                    throw new ScmException("Error running template client generation command: " + e.getMessage(), e);
                }
                finally
                {
                    ap.destroy();
                }
            }
            else
            {
                resolved = templateClient;
            }

            if(cache)
            {
                resolvedClient = resolved;
            }

            return resolved;
        }
        else
        {
            return resolvedClient;
        }
    }

    private void createClient(String clientName, File toDirectory, Revision revision) throws ScmException
    {
        core.createClient(resolveClient(revision), clientName, toDirectory);
    }

    private boolean clientExists(String clientName) throws ScmException
    {
        PerforceCore.P4Result result = core.runP4(null, getP4Command(COMMAND_CLIENTS), COMMAND_CLIENTS);
        String[] lines = core.splitLines(result);
        for (String line : lines)
        {
            String[] parts = line.split(" ");
            if (parts.length > 1 && parts[1].equals(clientName))
            {
                return true;
            }
        }

        return false;
    }

//    private void ensureClient(String clientName, File toDirectory) throws SCMException
//    {
//        if(clientExists(clientName))
//        {
//            // Just check mapping/root
//        }
//        else
//        {
//            createClient(clientName, toDirectory);
//        }
//    }

    private String updateClient(String id, File toDirectory, Revision revision) throws ScmException
    {
        if (toDirectory == null)
        {
            toDirectory = new File(".");
            clientRoot = toDirectory;
        }

        String clientName = getClientName(id);

        // If the client exists, perforce will just update the details.  This
        // is important in case the template is changed.
        createClient(clientName, toDirectory, revision);

        return clientName;
    }

    private String getClientName(String id)
    {
        String clientPrefix = System.getProperty("pulse.p4.client.prefix", "pulse-");
        String clientName;

        if (id == null)
        {
            id = Long.toString((long) (Math.random() * 100000));
            clientName = clientPrefix + "temp-" + id;
        }
        else
        {
            try
            {
                id = URLEncoder.encode(id, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                // Use raw ID
            }
            clientName = clientPrefix + id;
        }
        return clientName;
    }

    public Revision getLatestRevision(ScmContext context) throws ScmException
    {
        return getLatestRevision((String)null);
    }

    private Revision getLatestRevision(String clientName) throws ScmException
    {
        boolean cleanup = false;

        if (clientName == null)
        {
            clientName = updateClient(null, null, null);
            cleanup = true;
        }

        try
        {
            return core.getLatestRevisionForFiles(clientName, "//" + clientName + "/...");
        }
        finally
        {
            if (cleanup)
            {
                deleteClient(clientName);
            }
        }
    }

    public void populate(ScmFileCache.CacheItem item) throws ScmException
    {
        item.cachedRevision = getLatestRevision((ScmContext)null);
        item.cachedListing = new TreeMap<String, CachingScmFile>();

        CachingScmFile rootFile = new CachingScmFile("", true);
        item.cachedListing.put("", rootFile);

        String clientName = updateClient(null, null, null);

        try
        {
            PerforceCore.P4Result result = core.runP4(null, getP4Command(COMMAND_SYNC), FLAG_CLIENT, clientName, COMMAND_SYNC, FLAG_FORCE, FLAG_PREVIEW);
            Matcher matcher = syncPattern.matcher(result.stdout);
            while (matcher.find())
            {
                String localFile = matcher.group(4);
                if (localFile.startsWith(clientRoot.getAbsolutePath()))
                {
                    localFile = localFile.substring(clientRoot.getAbsolutePath().length());
                }

                // Separators must be normalised
                localFile = localFile.replace('\\', '/');
                if (localFile.startsWith("/"))
                {
                    localFile = localFile.substring(1);
                }

                addToCache(localFile, rootFile, item);
            }
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    private void deleteClient(String clientName)
    {
        try
        {
            core.runP4(null, getP4Command(COMMAND_CLIENT), COMMAND_CLIENT, FLAG_DELETE, clientName);
        }
        catch (ScmException e)
        {
            LOG.warning("Unable to delete client: " + e.getMessage(), e);
        }
    }

    private Changelist getChangelist(String clientName, long number) throws ScmException
    {
        //   Change <number> by <user>@<client> on <date> <time> (*pending*)?
        //
        //           <message, wrapped and indented>
        //
        //   Affected files ...
        //
        //   ... <file>#<revision> <action>
        //   ... <file>#<revision> <action>
        //   ...
        PerforceCore.P4Result result = core.runP4(false, null, getP4Command(COMMAND_DESCRIBE), FLAG_CLIENT, clientName, COMMAND_DESCRIBE, FLAG_SHORT, Long.toString(number));
        if (result.stderr.length() > 0)
        {
            if (result.stderr.indexOf("no such changelist") >= 0)
            {
                // OK, this change must have been deleted at some point
                // (CIB-1010).
                return null;
            }
            else
            {
                throw new ScmException("p4 process returned error '" + result.stderr.toString().trim() + "'");
            }
        }

        String[] lines = core.splitLines(result);

        if (lines.length < 1)
        {
            throw new ScmException("Unexpected output from 'p4 describe -s " + Long.toString(number) + "'");
        }

        Pattern re = Pattern.compile("Change ([0-9]+) by (.+)@(.+) on ([0-9/]+ [0-9:]+)( \\*pending\\*)?");
        Matcher matcher = re.matcher(lines[0].trim());
        String user;
        Date date;

        if (matcher.matches())
        {
            if (matcher.group(5) != null)
            {
                // Change is marked *pending*
                return null;
            }

            user = matcher.group(2);

            try
            {
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                date = format.parse(matcher.group(4));
            }
            catch (ParseException e)
            {
                throw new ScmException("Unable to parse date from p4 describe", e);
            }
        }
        else
        {
            throw new ScmException("Unexpected first line of output from p4 describe '" + lines[0] + "'");
        }

        int affectedFilesIndex;

        for (affectedFilesIndex = lines.length - 1; affectedFilesIndex > 0; affectedFilesIndex--)
        {
            if (lines[affectedFilesIndex].startsWith("Affected files ..."))
            {
                break;
            }
        }

        String comment = getChangelistComment(lines, affectedFilesIndex);

        Revision revision = new Revision(Long.toString(number));
        ExcludePathFilter filter = new ExcludePathFilter(excludedPaths);
        List<FileChange> changes = new LinkedList<FileChange>();
        for (int i = affectedFilesIndex + 2; i < lines.length; i++)
        {
            FileChange change = getChangelistChange(lines[i]);
            if (filter.accept(change.getPath()))
            {
                changes.add(change);
            }
        }

        // if all of the changes have been filtered out, then there is no changelist so we return null.
        if (changes.isEmpty())
        {
            return null;
        }

        return new Changelist(revision, date.getTime(), user, comment, changes);
    }

    private FileChange getChangelistChange(String line) throws ScmException
    {
        // ... <depot file>#<revision> <action>
        Pattern re = Pattern.compile("\\.\\.\\. (.+)#([0-9]+) (.+)");
        Matcher matcher = re.matcher(line);

        if (matcher.matches())
        {
            return new FileChange(matcher.group(1), new Revision(matcher.group(2)), decodeAction(matcher.group(3)));
        }
        else
        {
            throw new ScmException("Could not parse affected file line from p4 describe '" + line + "'");
        }
    }

    public static FileChange.Action decodeAction(String action)
    {
        if (action.equals("add") || action.equals("added as") || action.equals("refreshing"))
        {
            return FileChange.Action.ADD;
        }
        else if (action.equals("branch"))
        {
            return FileChange.Action.BRANCH;
        }
        else if (action.equals("delete") || action.equals("deleted as"))
        {
            return FileChange.Action.DELETE;
        }
        else if (action.equals("edit") || action.equals("updating"))
        {
            return FileChange.Action.EDIT;
        }
        else if (action.equals("integrate"))
        {
            return FileChange.Action.INTEGRATE;
        }
        else
        {
            return FileChange.Action.UNKNOWN;
        }
    }

    private Revision sync(String id, File toDirectory, Revision revision, ScmFeedbackHandler handler, boolean force) throws ScmException
    {
        String clientName = updateClient(id, toDirectory, revision);

        try
        {
            if (revision == null)
            {
                revision = getLatestRevision(clientName);
            }

            long number = Long.valueOf(revision.toString());
            PerforceCheckoutHandler perforceHandler = new PerforceCheckoutHandler(force, handler);

            if (force)
            {
                core.runP4WithHandler(perforceHandler, null, getP4Command(COMMAND_SYNC), FLAG_CLIENT, clientName, COMMAND_SYNC, FLAG_FORCE, "@" + Long.toString(number));
            }
            else
            {
                core.runP4WithHandler(perforceHandler, null, getP4Command(COMMAND_SYNC), FLAG_CLIENT, clientName, COMMAND_SYNC, "@" + Long.toString(number));
            }
        }
        finally
        {
            if (id == null)
            {
                deleteClient(clientName);
            }
        }

        return revision;
    }

    private String getChangelistComment(String[] lines, int affectedFilesIndex)
    {
        String result = "";
        int i;

        for (i = 2; i < affectedFilesIndex - 1; i++)
        {
            if (result.length() > 0)
            {
                result += "\n";
            }

            if (lines[i].startsWith("\t"))
            {
                result += lines[i].substring(1);
            }
            else
            {
                result += lines[i];
            }
        }

        return result;
    }

    public PerforceClient(String port, String user, String password, String client) throws ScmException
    {
        this.core = new PerforceCore();
        templateClient = client;
        this.port = port;

        // Output of p4 sync -f:
        //   <depot file>#<revision> - (refreshing|added as) <local file>
        //   <depot file>#<revision> - (refreshing|updating|added as|deleted as) <local file>
        //   ...
        syncPattern = Pattern.compile("^(.+)#([0-9]+) - (refreshing|updating|added as|deleted as) (.+)$", Pattern.MULTILINE);

        this.core.setEnv(ENV_PORT, port);
        this.core.setEnv(ENV_USER, user);

        if (TextUtils.stringSet(password))
        {
            this.core.setEnv(ENV_PASSWORD, password);
        }

        this.core.setEnv(ENV_CLIENT, resolveClient(null, false));
    }

    public void init(ScmContext context, ScmFeedbackHandler handler) throws ScmException
    {
        // noop
    }

    public void close()
    {
    }

    public Set<ScmCapability> getCapabilities(boolean contextAvailable)
    {
        return new HashSet<ScmCapability>(Arrays.asList(ScmCapability.values()));
    }

    public Map<String, String> getServerInfo() throws ScmException
    {
        return core.getServerInfo(resolveClient(null));
    }

    public String getUid()
    {
        return port;
    }

    public String getLocation()
    {
        return templateClient + "@" + port;
    }

    public List<ResourceProperty> getProperties(ExecutionContext context) throws ScmException
    {
        List<ResourceProperty> result = new LinkedList<ResourceProperty>();
        for (Map.Entry<String, String> entry : core.getEnv().entrySet())
        {
            result.add(new ResourceProperty(entry.getKey(), entry.getValue(), true, false, false));
        }

        String id = getId(context);
        result.add(new ResourceProperty("P4CLIENT", getClientName(id), true, false, false));
        return result;
    }

    private String getId(ExecutionContext context)
    {
        if (context.getBoolean(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_INCREMENTAL_BOOTSTRAP, false))
        {
            return context.getString(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_PROJECT) + "-" +
                context.getString(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT);
        }
        else
        {
            return null;
        }
    }

    public void testConnection() throws ScmException
    {
        String client = resolveClient(null);
        if (!clientExists(client))
        {
            throw new ScmException("Client '" + client + "' does not exist");
        }
    }

    public Revision checkout(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException
    {
        return sync(getId(context), context.getWorkingDir(), revision, handler, true);
    }

    public InputStream retrieve(ScmContext context, String path, Revision revision) throws ScmException
    {
        String clientName = updateClient(null, null, revision);

        try
        {
            File fullFile = new File(clientRoot, path);

            String fileArgument = fullFile.getAbsolutePath();
            if (revision != null)
            {
                fileArgument = fileArgument + "@" + revision;
            }

            PerforceCore.P4Result result = core.runP4(null, getP4Command("print"), FLAG_CLIENT, clientName, "print", "-q", fileArgument);
            return new ByteArrayInputStream(result.stdout.toString().getBytes("US-ASCII"));
        }
        catch (ScmException e)
        {
            if (e.getMessage().contains("no such file") || e.getMessage().contains("not in client view"))
            {
                String rev = revision == null ? "head" : revision.getRevisionString();
                throw new ScmException("File '" + path + "' revision " + rev + " does not exist in the client's view (" + e.getMessage() + ")");
            }
            else
            {
                throw e;
            }
        }
        catch (UnsupportedEncodingException e)
        {
            // Programmer error
            throw new ScmException(e);
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public List<Changelist> getChanges(ScmContext context, Revision from, Revision to) throws ScmException
    {
        List<Changelist> result = new LinkedList<Changelist>();
        getRevisions(from, to, result);
        return result;
    }

    public List<Revision> getRevisions(ScmContext context, Revision from, Revision to) throws ScmException
    {
        return getRevisions(from, to, null);
    }

    private List<Revision> getRevisions(Revision from, Revision to, List<Changelist> changes) throws ScmException
    {
        List<Revision> result = new LinkedList<Revision>();

        String clientName = updateClient(null, null, null);

        if (to == null)
        {
            to = getLatestRevision(clientName);
        }

        long start = Long.valueOf(from.toString()) + 1;
        long end = Long.valueOf(to.toString());

        try
        {
            if (start <= end)
            {
                PerforceCore.P4Result p4Result = core.runP4(null, getP4Command(COMMAND_CHANGES), FLAG_CLIENT, clientName, COMMAND_CHANGES, FLAG_STATUS, VALUE_SUBMITTED, clientRoot.getAbsoluteFile() + "/...@" + Long.toString(start) + "," + Long.toString(end));
                Matcher matcher = core.getChangesPattern().matcher(p4Result.stdout);

                while (matcher.find())
                {
                    Revision revision = new Revision(matcher.group(1));
                    Changelist list = getChangelist(clientName, Long.valueOf(revision.toString()));
                    if (list != null)
                    {
                        result.add(0, revision);

                        if (changes != null)
                        {
                            changes.add(0, list);
                        }
                    }
                }
            }

            return result;
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public boolean hasChangedSince(Revision since) throws ScmException
    {
        String clientName = updateClient(null, null, null);
        try
        {
            String root = new File(clientRoot.getAbsolutePath(), VALUE_ALL_FILES).getAbsolutePath();
            long latestRevision = Long.valueOf(core.getLatestRevisionForFiles(clientName, root).toString());
            long sinceRevision = Long.valueOf(since.toString());
            if (latestRevision > sinceRevision)
            {
                if (excludedPaths != null && excludedPaths.size() > 0)
                {
                    // We have to find a change that includes a non-excluded
                    // path.
                    return nonExcludedChange(clientName, sinceRevision, latestRevision);
                }
                else
                {
                    return true;
                }
            }

            return false;
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    private boolean nonExcludedChange(String clientName, long sinceRevision, long latestRevision) throws ScmException
    {
        for (long revision = sinceRevision + 1; revision <= latestRevision; revision++)
        {
            if (getChangelist(clientName, revision) != null)
            {
                return true;
            }
        }

        return false;
    }

    public Revision update(ExecutionContext context, Revision rev, ScmFeedbackHandler handler) throws ScmException
    {
        sync(getId(context), context.getWorkingDir(), rev, handler, false);
        return rev;
    }

    public void tag(ExecutionContext context, Revision revision, String name, boolean moveExisting) throws ScmException
    {
        String clientName = updateClient(null, null, revision);
        try
        {
            if (!labelExists(clientName, name))
            {
                createLabel(clientName, name);
            }
            else if (!moveExisting)
            {
                throw new ScmException("Cannot create label '" + name + "': label already exists");
            }

            core.runP4(false, null, getP4Command(COMMAND_LABELSYNC), FLAG_CLIENT, clientName, COMMAND_LABELSYNC, FLAG_LABEL, name, clientRoot.getAbsoluteFile() + "/...@" + revision.toString());
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public void storeConnectionDetails(File outputDir) throws ScmException, IOException
    {
        PerforceCore.P4Result result = core.runP4(null, getP4Command(COMMAND_INFO), FLAG_CLIENT, resolveClient(null), COMMAND_INFO);
        FileSystemUtils.createFile(new File(outputDir, "server-info.txt"), result.stdout.toString());

        result = core.runP4(null, getP4Command(COMMAND_CLIENT), FLAG_CLIENT, resolveClient(null), COMMAND_CLIENT, FLAG_OUTPUT);
        FileSystemUtils.createFile(new File(outputDir, "template-client.txt"), result.stdout.toString());
    }

    public EOLStyle getEOLPolicy(ScmContext context) throws ScmException
    {
        final EOLStyle[] eol = new EOLStyle[]{EOLStyle.NATIVE};

        core.runP4WithHandler(new PerforceErrorDetectingHandler(true)
        {
            public void handleStdout(String line)
            {
                if (line.startsWith("LineEnd:"))
                {
                    String ending = line.substring(8).trim();
                    if (ending.equals("local"))
                    {
                        eol[0] = EOLStyle.NATIVE;
                    }
                    else if (ending.equals("unix") || ending.equals("share"))
                    {
                        eol[0] = EOLStyle.LINEFEED;
                    }
                    else if (ending.equals("mac"))
                    {
                        eol[0] = EOLStyle.CARRIAGE_RETURN;
                    }
                    else if (ending.equals("win"))
                    {
                        eol[0] = EOLStyle.CARRIAGE_RETURN_LINEFEED;
                    }
                }
            }

            public void checkCancelled() throws ScmCancelledException
            {
            }
        }, null, getP4Command(COMMAND_CLIENT), FLAG_CLIENT, resolveClient(null), COMMAND_CLIENT, FLAG_OUTPUT);

        return eol[0];
    }

    public String getFileRevision(String path, Revision repoRevision) throws ScmException
    {
        //    jsankey@shiny:~/p4test$ p4 fstat //depot/build.xml@34
        //    //depot/build.xml@34 - no file(s) at that changelist number.
        //    jsankey@shiny:~/p4test$ p4 fstat //depot/jsankey/bob.xml@34
        //    ... depotFile //depot/jsankey/bob.xml
        //    ... clientFile /home/jsankey/sandbox/bob.xml
        //    ... isMapped
        //    ... headAction edit
        //    ... headType text
        //    ... headTime 1142667022
        //    ... headRev 34
        //    ... headChange 34
        //    ... headModTime 1142667014
        //    ... haveRev 36
        //
        //    jsankey@shiny:~/p4test$ p4 fstat //depot/build.xml@37
        //    ... depotFile //depot/build.xml
        //    ... headAction add
        //    ... headType text
        //    ... headTime 1162177253
        //    ... headRev 1
        //    ... headChange 37
        //    ... headModTime 1162177235
        //    ... ... otherOpen0 jsankey@p4test
        //    ... ... otherAction0 edit
        //    ... ... otherChange0 38
        //    ... ... otherOpen 1
        String clientName = updateClient(null, null, repoRevision);
        try
        {
            File f = new File(clientRoot.getAbsoluteFile(), path);
            PerforceCore.P4Result result = core.runP4(false, null, getP4Command(COMMAND_FSTAT), FLAG_CLIENT, clientName, COMMAND_FSTAT, f.getAbsolutePath() + "@" + repoRevision.getRevisionString());
            if (result.stderr.length() > 0)
            {
                String error = result.stderr.toString();
                if (error.contains("no file(s) at that changelist number") || error.contains("no such file(s)"))
                {
                    return null;
                }
                else
                {
                    throw new ScmException("Error running p4 fstat: " + result.stderr);
                }
            }
            else if (result.stdout.toString().contains("... headAction delete"))
            {
                return null;
            }
            else
            {
                Pattern revPattern = Pattern.compile("... headRev ([0-9]+)");
                for (String line : core.splitLines(result))
                {
                    Matcher m = revPattern.matcher(line);
                    if (m.matches())
                    {
                        return m.group(1);
                    }
                }

                return null;
            }
        }
        finally
        {
            deleteClient(clientName);
        }

    }

    public Revision parseRevision(ScmContext context, String revision) throws ScmException
    {
        String clientName = updateClient(null, null, null);
        try
        {
            try
            {
                long revisionNumber = Long.parseLong(revision);
                // Run a quick check to ensure that the change exists.
                core.runP4(true, null, getP4Command(COMMAND_CHANGE), FLAG_CLIENT, clientName, COMMAND_CHANGE, FLAG_OUTPUT, revision);
                return new Revision(revisionNumber);
            }
            catch (NumberFormatException e)
            {
                throw new ScmException("Invalid revision '" + revision + "': must be a valid Perforce changelist number");
            }
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public Revision getPreviousRevision(ScmContext context, Revision revision, boolean isFile) throws ScmException
    {
        try
        {
            return revision.calculatePreviousNumericalRevision();
        }
        catch (NumberFormatException e)
        {
            throw new ScmException("Invalid revision '" + revision.getRevisionString() + "': " + e.getMessage());
        }
    }

    public boolean labelExists(String client, String name) throws ScmException
    {
        PerforceCore.P4Result p4Result = this.core.runP4(null, getP4Command(COMMAND_LABELS), FLAG_CLIENT, client, COMMAND_LABELS);

        // $ p4 labels
        // Label jim 2006/06/20 'Created by Jason. '
        Pattern splitter = Pattern.compile("^Label (.+) [0-9/]+ '.*'$", Pattern.MULTILINE);
        Matcher matcher = splitter.matcher(p4Result.stdout);
        while (matcher.find())
        {
            if (matcher.group(1).equals(name))
            {
                return true;
            }
        }

        return false;
    }

    private void createLabel(String client, String name) throws ScmException
    {
        PerforceCore.P4Result p4Result = this.core.runP4(null, getP4Command(COMMAND_LABEL), FLAG_CLIENT, client, COMMAND_LABEL, FLAG_OUTPUT, name);
        this.core.runP4(p4Result.stdout.toString(), getP4Command(COMMAND_LABEL), FLAG_CLIENT, client, COMMAND_LABEL, FLAG_INPUT);
    }

    public static void main(String argv[])
    {
        try
        {
            PerforceClient client = new PerforceClient("localhost:1666", "jsankey", "", "pulse-demo");
            client.retrieve(null, "file", new Revision("2"));
            List<Changelist> cls = client.getChanges(null, new Revision("2"), new Revision("6"));

            for (Changelist l : cls)
            {
                System.out.println("Changelist:");
                System.out.println("  Revision: " + l.getRevision());
                System.out.println("  Date    : " + new Date(l.getTime()));
                System.out.println("  Author  : " + l.getAuthor());
                System.out.println("  Comment : " + l.getComment());
                System.out.println("  Files   : " + l.getRevision());

                for (FileChange c : l.getChanges())
                {
                    System.out.println("    " + c.getPath() + "#" + c.getRevision().getRevisionString() + " - " + c.getAction());
                }
            }
        }
        catch (ScmException e)
        {
            e.printStackTrace();
        }
    }
}
