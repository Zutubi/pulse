package com.zutubi.pulse.core.scm.p4;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.*;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.process.AsyncProcess;
import com.zutubi.pulse.util.process.BufferingCharHandler;
import com.zutubi.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
    private List<String> excludedPaths;

    public void setExcludedPaths(List<String> filteredPaths)
    {
        this.excludedPaths = filteredPaths;
    }

    private String resolveClient(NumericalRevision revision) throws ScmException
    {
        return resolveClient(revision, true);
    }

    private String resolveClient(NumericalRevision revision, boolean cache) throws ScmException
    {
        String resolved;
        if (resolvedClient == null)
        {
            if (templateClient.startsWith("!"))
            {
                String commandLine = templateClient.substring(1);

                Scope scope = new Scope();
                String revisionSpec;
                if(revision == null)
                {
                    revisionSpec = "#head";
                }
                else
                {
                    revisionSpec = "@" + revision.getRevisionString();
                }

                scope.add(new Property("revision.spec", revisionSpec));

                Process p;
                try
                {
                    List<String> command = VariableHelper.splitAndReplaceVariables(commandLine, scope, true);
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

    private void createClient(String clientName, File toDirectory, NumericalRevision revision) throws ScmException
    {
        core.createClient(resolveClient(revision), clientName, toDirectory);
    }

    private boolean clientExists(String clientName) throws ScmException
    {
        PerforceCore.P4Result result = core.runP4(null, P4_COMMAND, COMMAND_CLIENTS);
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

    private String updateClient(String id, File toDirectory, NumericalRevision revision) throws ScmException
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

    public Revision getLatestRevision() throws ScmException
    {
        return core.convertRevision(getLatestRevision(null));
    }

    private NumericalRevision getLatestRevision(String clientName) throws ScmException
    {
        boolean cleanup = false;

        if (clientName == null)
        {
            clientName = updateClient(null, null, null);
            cleanup = true;
        }

        try
        {
            return core.getLatestRevisionForFiles(clientName);
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
        item.cachedRevision = getLatestRevision();
        item.cachedListing = new TreeMap<String, CachingScmFile>();

        CachingScmFile rootFile = new CachingScmFile("", true, null, "");
        item.cachedListing.put("", rootFile);

        String clientName = updateClient(null, null, null);

        try
        {
            PerforceCore.P4Result result = core.runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_SYNC, FLAG_FORCE, FLAG_PREVIEW);
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
            core.runP4(null, P4_COMMAND, COMMAND_CLIENT, FLAG_DELETE, clientName);
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
        PerforceCore.P4Result result = core.runP4(false, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_DESCRIBE, FLAG_SHORT, Long.toString(number));
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

        Revision revision = new Revision(user, comment, date, Long.toString(number));
        ScmFilepathFilter filter = new ScmFilepathFilter(excludedPaths);
        Changelist changelist = new Changelist(getUid(), revision);

        for (int i = affectedFilesIndex + 2; i < lines.length; i++)
        {
            Change change = getChangelistChange(lines[i]);
            if (filter.accept(change.getFilename()))
            {
                changelist.addChange(change);
            }
        }

        // if all of the changes have been filtered out, then there is no changelist so we return null.
        if (changelist.getChanges().size() == 0)
        {
            return null;
        }
        return changelist;
    }

    private Change getChangelistChange(String line) throws ScmException
    {
        // ... <depot file>#<revision> <action>
        Pattern re = Pattern.compile("\\.\\.\\. (.+)#([0-9]+) (.+)");
        Matcher matcher = re.matcher(line);

        if (matcher.matches())
        {
            return new Change(matcher.group(1), matcher.group(2), decodeAction(matcher.group(3)));
        }
        else
        {
            throw new ScmException("Could not parse affected file line from p4 describe '" + line + "'");
        }
    }

    public static Change.Action decodeAction(String action)
    {
        if (action.equals("add") || action.equals("added as") || action.equals("refreshing"))
        {
            return Change.Action.ADD;
        }
        else if (action.equals("branch"))
        {
            return Change.Action.BRANCH;
        }
        else if (action.equals("delete") || action.equals("deleted as"))
        {
            return Change.Action.DELETE;
        }
        else if (action.equals("edit") || action.equals("updating"))
        {
            return Change.Action.EDIT;
        }
        else if (action.equals("integrate"))
        {
            return Change.Action.INTEGRATE;
        }
        else
        {
            return Change.Action.UNKNOWN;
        }
    }

    private Revision sync(String id, File toDirectory, Revision revision, ScmEventHandler handler, boolean force) throws ScmException
    {
        NumericalRevision numericalRevision = core.convertRevision(revision);
        String clientName = updateClient(id, toDirectory, numericalRevision);

        try
        {
            if (numericalRevision == null)
            {
                numericalRevision = getLatestRevision(clientName);
            }

            long number = numericalRevision.getRevisionNumber();
            PerforceCheckoutHandler perforceHandler = new PerforceCheckoutHandler(force, handler);

            if (force)
            {
                core.runP4WithHandler(perforceHandler, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_SYNC, FLAG_FORCE, "@" + Long.toString(number));
            }
            else
            {
                core.runP4WithHandler(perforceHandler, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_SYNC, "@" + Long.toString(number));
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

    public Set<ScmCapability> getCapabilities()
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

    public void testConnection() throws ScmException
    {
        String client = resolveClient(null);
        if (!clientExists(client))
        {
            throw new ScmException("Client '" + client + "' does not exist");
        }
    }

    public Revision checkout(ScmContext context, ScmEventHandler handler) throws ScmException
    {
        Revision revision = context.getRevision();
        return sync(context.getId(), context.getDir(), revision, handler, true);
    }

    public InputStream retrieve(String path, Revision revision) throws ScmException
    {
        String clientName = updateClient(null, null, core.convertRevision(revision));

        try
        {
            File fullFile = new File(clientRoot, path);

            String fileArgument = fullFile.getAbsolutePath();
            if (revision != null)
            {
                fileArgument = fileArgument + "@" + revision;
            }

            PerforceCore.P4Result result = core.runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, "print", "-q", fileArgument);
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

    public List<Changelist> getChanges(Revision from, Revision to) throws ScmException
    {
        List<Changelist> result = new LinkedList<Changelist>();
        getRevisions(from, to, result);
        return result;
    }

    public List<Revision> getRevisions(Revision from, Revision to) throws ScmException
    {
        return getRevisions(from, to, null);
    }

    private List<Revision> getRevisions(Revision from, Revision to, List<Changelist> changes) throws ScmException
    {
        List<Revision> result = new LinkedList<Revision>();

        String clientName = updateClient(null, null, null);

        NumericalRevision numericalTo = core.convertRevision(to);
        if (numericalTo == null)
        {
            numericalTo = getLatestRevision(clientName);
        }

        long start = core.convertRevision(from).getRevisionNumber() + 1;
        long end = numericalTo.getRevisionNumber();

        try
        {
            if (start <= end)
            {
                PerforceCore.P4Result p4Result = core.runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_CHANGES, FLAG_STATUS, VALUE_SUBMITTED, clientRoot.getAbsoluteFile() + "/...@" + Long.toString(start) + "," + Long.toString(end));
                Matcher matcher = core.getChangesPattern().matcher(p4Result.stdout);

                while (matcher.find())
                {
                    NumericalRevision revision = new NumericalRevision(Long.parseLong(matcher.group(1)));
                    result.add(0, core.convertRevision(revision));

                    if (changes != null)
                    {
                        Changelist list = getChangelist(clientName, revision.getRevisionNumber());

                        if (list != null)
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
            long latestRevision = core.getLatestRevisionForFiles(clientName, root).getRevisionNumber();
            long sinceRevision = core.convertRevision(since).getRevisionNumber();
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

    public void update(ScmContext context, ScmEventHandler handler) throws ScmException
    {
        Revision rev = context.getRevision();
        sync(context.getId(), context.getDir(), rev, handler, false);
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws ScmException
    {
        String clientName = updateClient(null, null, core.convertRevision(revision));
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

            core.runP4(false, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_LABELSYNC, FLAG_LABEL, name, clientRoot.getAbsoluteFile() + "/...@" + revision.toString());
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public List<ResourceProperty> getProperties(String id, File dir) throws ScmException
    {
        List<ResourceProperty> result = new LinkedList<ResourceProperty>();
        for (Map.Entry<String, String> entry : core.getEnv().entrySet())
        {
            result.add(new ResourceProperty(entry.getKey(), entry.getValue(), true, false, false));
        }
        result.add(new ResourceProperty("P4CLIENT", getClientName(id), true, false, false));
        return result;
    }

    public void storeConnectionDetails(File outputDir) throws ScmException, IOException
    {
        PerforceCore.P4Result result = core.runP4(null, P4_COMMAND, FLAG_CLIENT, resolveClient(null), COMMAND_INFO);
        FileSystemUtils.createFile(new File(outputDir, "server-info.txt"), result.stdout.toString());

        result = core.runP4(null, P4_COMMAND, FLAG_CLIENT, resolveClient(null), COMMAND_CLIENT, FLAG_OUTPUT);
        FileSystemUtils.createFile(new File(outputDir, "template-client.txt"), result.stdout.toString());
    }

    public FileStatus.EOLStyle getEOLPolicy() throws ScmException
    {
        final FileStatus.EOLStyle[] eol = new FileStatus.EOLStyle[]{FileStatus.EOLStyle.NATIVE};

        core.runP4WithHandler(new PerforceErrorDetectingHandler(true)
        {
            public void handleStdout(String line)
            {
                if (line.startsWith("LineEnd:"))
                {
                    String ending = line.substring(8).trim();
                    if (ending.equals("local"))
                    {
                        eol[0] = FileStatus.EOLStyle.NATIVE;
                    }
                    else if (ending.equals("unix") || ending.equals("share"))
                    {
                        eol[0] = FileStatus.EOLStyle.LINEFEED;
                    }
                    else if (ending.equals("mac"))
                    {
                        eol[0] = FileStatus.EOLStyle.CARRIAGE_RETURN;
                    }
                    else if (ending.equals("win"))
                    {
                        eol[0] = FileStatus.EOLStyle.CARRIAGE_RETURN_LINEFEED;
                    }
                }
            }

            public void checkCancelled() throws ScmCancelledException
            {
            }
        }, null, P4_COMMAND, FLAG_CLIENT, resolveClient(null), COMMAND_CLIENT, FLAG_OUTPUT);

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
        String clientName = updateClient(null, null, core.convertRevision(repoRevision));
        try
        {
            File f = new File(clientRoot.getAbsoluteFile(), path);
            PerforceCore.P4Result result = core.runP4(false, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_FSTAT, f.getAbsolutePath() + "@" + repoRevision.getRevisionString());
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

    public Revision getRevision(String revision) throws ScmException
    {
        String clientName = updateClient(null, null, null);
        try
        {
            try
            {
                long revisionNumber = Long.parseLong(revision);
                // Run a quick check to ensure that the change exists.
                core.runP4(true, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_CHANGE, FLAG_OUTPUT, revision);
                return core.convertRevision(new NumericalRevision(revisionNumber));
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

    public boolean labelExists(String client, String name) throws ScmException
    {
        PerforceCore.P4Result p4Result = this.core.runP4(null, P4_COMMAND, FLAG_CLIENT, client, COMMAND_LABELS);

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
        PerforceCore.P4Result p4Result = this.core.runP4(null, P4_COMMAND, FLAG_CLIENT, client, COMMAND_LABEL, FLAG_OUTPUT, name);
        this.core.runP4(p4Result.stdout.toString(), P4_COMMAND, FLAG_CLIENT, client, COMMAND_LABEL, FLAG_INPUT);
    }

    public static void main(String argv[])
    {
        try
        {
            PerforceClient client = new PerforceClient("localhost:1666", "jsankey", "", "pulse-demo");
            client.retrieve("file", new Revision(null, null, null, "2"));
            List<Changelist> cls = client.getChanges(new Revision(null, null, null, "2"), new Revision(null, null, null, "6"));

            for (Changelist l : cls)
            {
                System.out.println("Changelist:");
                System.out.println("  Revision: " + l.getRevision());
                System.out.println("  Date    : " + l.getDate());
                System.out.println("  User    : " + l.getUser());
                System.out.println("  Comment : " + l.getComment());
                System.out.println("  Files   : " + l.getRevision());

                for (Change c : l.getChanges())
                {
                    System.out.println("    " + c.getFilename() + "#" + c.getRevisionString() + " - " + c.getAction());
                }
            }
        }
        catch (ScmException e)
        {
            e.printStackTrace();
        }
    }
}
