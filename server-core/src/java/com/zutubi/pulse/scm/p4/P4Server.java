package com.zutubi.pulse.scm.p4;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.filesystem.remote.CachingRemoteFile;
import com.zutubi.pulse.scm.*;
import static com.zutubi.pulse.scm.p4.P4Constants.*;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.util.process.AsyncProcess;
import com.zutubi.pulse.util.process.BufferingCharHandler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class P4Server extends CachingSCMServer
{
    private static final Logger LOG = Logger.getLogger(P4Server.class);

    private static final long RESOLVE_COMMAND_TIMEOUT = Long.getLong("pulse.p4.client.command.timeout", 300);

    private P4Client client;
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

    private String resolveClient(NumericalRevision revision) throws SCMException
    {
        return resolveClient(revision, true);
    }

    private String resolveClient(NumericalRevision revision, boolean cache) throws SCMException
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
                    throw new SCMException("Error starting template client generation command: " + e.getMessage(), e);
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
                    throw new SCMException("Error running template client generation command: " + e.getMessage(), e);
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

    private void createClient(String clientName, File toDirectory, NumericalRevision revision) throws SCMException
    {
        client.createClient(resolveClient(revision), clientName, toDirectory);
    }

    private boolean clientExists(String clientName) throws SCMException
    {
        P4Client.P4Result result = client.runP4(null, getP4Command(COMMAND_CLIENTS), COMMAND_CLIENTS);
        String[] lines = client.splitLines(result);
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

    private String updateClient(String id, File toDirectory, NumericalRevision revision) throws SCMException
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
                // Noop
            }
            clientName = clientPrefix + id;
        }
        return clientName;
    }

    public NumericalRevision getLatestRevision() throws SCMException
    {
        return getLatestRevision(null);
    }

    private NumericalRevision getLatestRevision(String clientName) throws SCMException
    {
        boolean cleanup = false;

        if (clientName == null)
        {
            clientName = updateClient(null, null, null);
            cleanup = true;
        }

        try
        {
            return client.getLatestRevisionForFiles(clientName);
        }
        finally
        {
            if (cleanup)
            {
                deleteClient(clientName);
            }
        }
    }

    public void populate(SCMFileCache.CacheItem item) throws SCMException
    {
        item.cachedRevision = getLatestRevision();
        item.cachedListing = new TreeMap<String, CachingRemoteFile>();

        CachingRemoteFile rootFile = new CachingRemoteFile("", true, null, "");
        item.cachedListing.put("", rootFile);

        String clientName = updateClient(null, null, null);

        try
        {
            P4Client.P4Result result = client.runP4(null, getP4Command(COMMAND_SYNC), FLAG_CLIENT, clientName, COMMAND_SYNC, FLAG_FORCE, FLAG_PREVIEW);
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
            client.runP4(null, getP4Command(COMMAND_CLIENT), COMMAND_CLIENT, FLAG_DELETE, clientName);
        }
        catch (SCMException e)
        {
            LOG.warning("Unable to delete client: " + e.getMessage(), e);
        }
    }

    private Changelist getChangelist(String clientName, long number) throws SCMException
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
        P4Client.P4Result result = client.runP4(false, null, getP4Command(COMMAND_DESCRIBE), FLAG_CLIENT, clientName, COMMAND_DESCRIBE, FLAG_SHORT, Long.toString(number));
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
                throw new SCMException("p4 process returned error '" + result.stderr.toString().trim() + "'");
            }
        }

        String[] lines = client.splitLines(result);

        if (lines.length < 1)
        {
            throw new SCMException("Unexpected output from 'p4 describe -s " + Long.toString(number) + "'");
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
                throw new SCMException("Unable to parse date from p4 describe", e);
            }
        }
        else
        {
            throw new SCMException("Unexpected first line of output from p4 describe '" + lines[0] + "'");
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

        NumericalRevision revision = new NumericalRevision(user, comment, date, number);
        ScmFilepathFilter filter = new ScmFilepathFilter(excludedPaths);
        Changelist changelist = new Changelist(revision);

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

    private Change getChangelistChange(String line) throws SCMException
    {
        // ... <depot file>#<revision> <action>
        Pattern re = Pattern.compile("\\.\\.\\. (.+)#([0-9]+) (.+)");
        Matcher matcher = re.matcher(line);

        if (matcher.matches())
        {
            FileRevision fileRevision = new NumericalFileRevision(Long.parseLong(matcher.group(2)));
            return new Change(matcher.group(1), fileRevision, decodeAction(matcher.group(3)));
        }
        else
        {
            throw new SCMException("Could not parse affected file line from p4 describe '" + line + "'");
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

    private Revision sync(String id, File toDirectory, Revision revision, SCMCheckoutEventHandler handler, boolean force) throws SCMException
    {
        String clientName = updateClient(id, toDirectory, (NumericalRevision) revision);

        try
        {
            if (revision == null)
            {
                revision = getLatestRevision(clientName);
            }

            long number = ((NumericalRevision) revision).getRevisionNumber();
            P4CheckoutHandler p4Handler = new P4CheckoutHandler(force, handler);

            if (force)
            {
                client.runP4WithHandler(p4Handler, null, getP4Command(COMMAND_SYNC), FLAG_CLIENT, clientName, COMMAND_SYNC, FLAG_FORCE, "@" + Long.toString(number));
            }
            else
            {
                client.runP4WithHandler(p4Handler, null, getP4Command(COMMAND_SYNC), FLAG_CLIENT, clientName, COMMAND_SYNC, "@" + Long.toString(number));
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

    public P4Server(String port, String user, String password, String client) throws SCMException
    {
        this.client = new P4Client();
        templateClient = client;
        this.port = port;

        // Output of p4 sync -f:
        //   <depot file>#<revision> - (refreshing|added as) <local file>
        //   <depot file>#<revision> - (refreshing|updating|added as|deleted as) <local file>
        //   ...
        syncPattern = Pattern.compile("^(.+)#([0-9]+) - (refreshing|updating|added as|deleted as) (.+)$", Pattern.MULTILINE);

        this.client.setEnv(ENV_PORT, port);
        this.client.setEnv(ENV_USER, user);

        if (TextUtils.stringSet(password))
        {
            this.client.setEnv(ENV_PASSWORD, password);
        }

        this.client.setEnv(ENV_CLIENT, resolveClient(null, false));
    }

    public Map<String, String> getServerInfo() throws SCMException
    {
        return client.getServerInfo(resolveClient(null));
    }

    public String getUid()
    {
        return port;
    }

    public String getLocation()
    {
        return templateClient + "@" + port;
    }

    public void testConnection() throws SCMException
    {
        String client = resolveClient(null);
        if (!clientExists(client))
        {
            throw new SCMException("Client '" + client + "' does not exist");
        }
    }

    public Revision checkout(String id, File toDirectory, Revision revision, SCMCheckoutEventHandler handler) throws SCMException
    {
        return sync(id, toDirectory, revision, handler, true);
    }

    public String checkout(Revision revision, String file) throws SCMException
    {
        String clientName = updateClient(null, null, (NumericalRevision) revision);

        try
        {
            File fullFile = new File(clientRoot, file);

            String fileArgument = fullFile.getAbsolutePath();
            if (revision != null)
            {
                fileArgument = fileArgument + "@" + revision;
            }

            P4Client.P4Result result = client.runP4(null, getP4Command("print"), FLAG_CLIENT, clientName, "print", "-q", fileArgument);
            return result.stdout.toString();
        }
        catch (SCMException e)
        {
            if (e.getMessage().contains("no such file") || e.getMessage().contains("not in client view"))
            {
                String rev = revision == null ? "head" : revision.getRevisionString();
                throw new SCMException("File '" + file + "' revision " + rev + " does not exist in the client's view (" + e.getMessage() + ")");
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public List<Changelist> getChanges(Revision from, Revision to, String... paths) throws SCMException
    {
        List<Changelist> result = new LinkedList<Changelist>();
        getRevisions(from, to, result);
        return result;
    }

    public List<Revision> getRevisionsSince(Revision from) throws SCMException
    {
        return getRevisions(from, null, null);
    }

    private List<Revision> getRevisions(Revision from, Revision to, List<Changelist> changes) throws SCMException
    {
        List<Revision> result = new LinkedList<Revision>();

        String clientName = updateClient(null, null, null);

        if (to == null)
        {
            to = getLatestRevision(clientName);
        }

        long start = ((NumericalRevision) from).getRevisionNumber() + 1;
        long end = ((NumericalRevision) to).getRevisionNumber();

        try
        {
            if (start <= end)
            {
                P4Client.P4Result p4Result = client.runP4(null, getP4Command(COMMAND_CHANGES), FLAG_CLIENT, clientName, COMMAND_CHANGES, FLAG_STATUS, VALUE_SUBMITTED, clientRoot.getAbsoluteFile() + "/...@" + Long.toString(start) + "," + Long.toString(end));
                Matcher matcher = client.getChangesPattern().matcher(p4Result.stdout);

                while (matcher.find())
                {
                    NumericalRevision revision = new NumericalRevision(Long.parseLong(matcher.group(1)));
                    Changelist list = getChangelist(clientName, revision.getRevisionNumber());
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

    public boolean hasChangedSince(Revision since) throws SCMException
    {
        String clientName = updateClient(null, null, null);
        try
        {
            String root = new File(clientRoot.getAbsolutePath(), VALUE_ALL_FILES).getAbsolutePath();
            long latestRevision = client.getLatestRevisionForFiles(clientName, root).getRevisionNumber();
            long sinceRevision = ((NumericalRevision) since).getRevisionNumber();
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

    private boolean nonExcludedChange(String clientName, long sinceRevision, long latestRevision) throws SCMException
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

    public void update(String id, File workDir, Revision rev, SCMCheckoutEventHandler handler) throws SCMException
    {
        sync(id, workDir, rev, handler, false);
    }

    public boolean supportsUpdate()
    {
        return true;
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws SCMException
    {
        String clientName = updateClient(null, null, (NumericalRevision) revision);
        try
        {
            if (!labelExists(clientName, name))
            {
                createLabel(clientName, name);
            }
            else if (!moveExisting)
            {
                throw new SCMException("Cannot create label '" + name + "': label already exists");
            }

            client.runP4(false, null, getP4Command(COMMAND_LABELSYNC), FLAG_CLIENT, clientName, COMMAND_LABELSYNC, FLAG_LABEL, name, clientRoot.getAbsoluteFile() + "/...@" + revision.toString());
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public List<ResourceProperty> getConnectionProperties(String id, File dir) throws SCMException
    {
        List<ResourceProperty> result = new LinkedList<ResourceProperty>();
        for(Map.Entry<String, String> entry: client.getEnv().entrySet())
        {
            result.add(new ResourceProperty(entry.getKey(), entry.getValue(), true, false, false));
        }
        result.add(new ResourceProperty("P4CLIENT", getClientName(id), true, false, false));
        return result;
    }

    public void writeConnectionDetails(File outputDir) throws SCMException, IOException
    {
        P4Client.P4Result result = client.runP4(null, getP4Command(COMMAND_INFO), FLAG_CLIENT, resolveClient(null), COMMAND_INFO);
        FileSystemUtils.createFile(new File(outputDir, "server-info.txt"), result.stdout.toString());

        result = client.runP4(null, getP4Command(COMMAND_CLIENT), FLAG_CLIENT, resolveClient(null), COMMAND_CLIENT, FLAG_OUTPUT);
        FileSystemUtils.createFile(new File(outputDir, "template-client.txt"), result.stdout.toString());
    }

    public FileStatus.EOLStyle getEOLPolicy() throws SCMException
    {
        final FileStatus.EOLStyle[] eol = new FileStatus.EOLStyle[]{FileStatus.EOLStyle.NATIVE};

        client.runP4WithHandler(new P4ErrorDetectingHandler(true)
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

            public void checkCancelled() throws SCMCancelledException
            {
            }
        }, null, getP4Command(COMMAND_CLIENT), FLAG_CLIENT, resolveClient(null), COMMAND_CLIENT, FLAG_OUTPUT);

        return eol[0];
    }

    public FileRevision getFileRevision(String path, Revision repoRevision) throws SCMException
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
        String clientName = updateClient(null, null, (NumericalRevision) repoRevision);
        try
        {
            File f = new File(clientRoot.getAbsoluteFile(), path);
            P4Client.P4Result result = client.runP4(false, null, getP4Command(COMMAND_FSTAT), FLAG_CLIENT, clientName, COMMAND_FSTAT, f.getAbsolutePath() + "@" + repoRevision.getRevisionString());
            if (result.stderr.length() > 0)
            {
                String error = result.stderr.toString();
                if (error.contains("no file(s) at that changelist number") || error.contains("no such file(s)"))
                {
                    return null;
                }
                else
                {
                    throw new SCMException("Error running p4 fstat: " + result.stderr);
                }
            }
            else if (result.stdout.toString().contains("... headAction delete"))
            {
                return null;
            }
            else
            {
                Pattern revPattern = Pattern.compile("... headRev ([0-9]+)");
                for (String line : client.splitLines(result))
                {
                    Matcher m = revPattern.matcher(line);
                    if (m.matches())
                    {
                        long number = Long.parseLong(m.group(1));
                        return new NumericalFileRevision(number);
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

    public NumericalRevision getRevision(String revision) throws SCMException
    {
        String clientName = updateClient(null, null, null);
        try
        {
            try
            {
                long revisionNumber = Long.parseLong(revision);
                // Run a quick check to ensure that the change exists.
                client.runP4(true, null, getP4Command(COMMAND_CHANGE), FLAG_CLIENT, clientName, COMMAND_CHANGE, FLAG_OUTPUT, revision);
                return new NumericalRevision(revisionNumber);
            }
            catch (NumberFormatException e)
            {
                throw new SCMException("Invalid revision '" + revision + "': must be a valid Perforce changelist number");
            }
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public void close()
    {
        // Noop
    }

    public boolean labelExists(String client, String name) throws SCMException
    {
        P4Client.P4Result p4Result = this.client.runP4(null, getP4Command(COMMAND_LABELS), FLAG_CLIENT, client, COMMAND_LABELS);

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

    private void createLabel(String client, String name) throws SCMException
    {
        P4Client.P4Result p4Result = this.client.runP4(null, getP4Command(COMMAND_LABEL), FLAG_CLIENT, client, COMMAND_LABEL, FLAG_OUTPUT, name);
        this.client.runP4(p4Result.stdout.toString(), getP4Command(COMMAND_LABEL), FLAG_CLIENT, client, COMMAND_LABEL, FLAG_INPUT);
    }

    public static void main(String argv[])
    {
        try
        {
            P4Server server = new P4Server("localhost:1666", "jsankey", "", "pulse-demo");
            server.checkout(new NumericalRevision(2), "file");
            List<Changelist> cls = server.getChanges(new NumericalRevision(2), new NumericalRevision(6), "");

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
                    System.out.println("    " + c.getFilename() + "#" + c.getRevision() + " - " + c.getAction());
                }
            }
        }
        catch (SCMException e)
        {
            e.printStackTrace();
        }
    }
}
