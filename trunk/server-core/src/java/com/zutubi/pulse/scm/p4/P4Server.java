package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.filesystem.remote.CachingRemoteFile;
import com.zutubi.pulse.scm.*;
import static com.zutubi.pulse.scm.p4.P4Constants.*;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class P4Server extends CachingSCMServer
{
    private static final Logger LOG = Logger.getLogger(P4Server.class);

    private P4Client client;
    private String templateClient;
    private File clientRoot;
    private String port;
    private Pattern syncPattern;
    private List<String> excludedPaths;

    public void setExcludedPaths(List<String> filteredPaths)
    {
        this.excludedPaths = filteredPaths;
    }

    private void createClient(String clientName, File toDirectory) throws SCMException
    {
        client.createClient(templateClient, clientName, toDirectory);
    }

    private boolean clientExists(String clientName) throws SCMException
    {
        P4Client.P4Result result = client.runP4(null, P4_COMMAND, COMMAND_CLIENTS);
        String [] lines = client.splitLines(result);
        for (String line : lines)
        {
            String [] parts = line.split(" ");
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

    private String updateClient(String id, File toDirectory) throws SCMException
    {
        if (toDirectory == null)
        {
            toDirectory = new File(".");
            clientRoot = toDirectory;
        }

        String clientName;

        if (id == null)
        {
            id = Long.toString((long) (Math.random() * 100000));
            clientName = "pulse-temp-" + id;
        }
        else
        {
            clientName = "pulse-" + id;
        }

        // If the client exists, perforce will just update the details.  This
        // is important in case the template is changed.
        createClient(clientName, toDirectory);

        return clientName;
    }

    public NumericalRevision getLatestRevision() throws SCMException
    {
        return getLatestRevision(null);
    }

    private NumericalRevision getLatestRevision(String clientName) throws SCMException
    {
        boolean cleanup = false;

        if(clientName == null)
        {
            clientName = updateClient(null, null);
            cleanup = true;
        }

        try
        {
            return client.getLatestRevisionForFiles(clientName);
        }
        finally
        {
            if(cleanup)
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

        String clientName = updateClient(null, null);

        try
        {
            P4Client.P4Result result = client.runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_SYNC, FLAG_FORCE, FLAG_PREVIEW);
            Matcher matcher = syncPattern.matcher(result.stdout);
            while(matcher.find())
            {
                String localFile = matcher.group(4);
                if(localFile.startsWith(clientRoot.getAbsolutePath()))
                {
                    localFile = localFile.substring(clientRoot.getAbsolutePath().length());
                }

                // Separators must be normalised
                localFile = localFile.replace('\\', '/');
                if(localFile.startsWith("/"))
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
            client.runP4(null, P4_COMMAND, COMMAND_CLIENT, FLAG_DELETE, clientName);
        }
        catch (SCMException e)
        {
            LOG.warning("Unable to delete client: " + e.getMessage(), e);
        }
    }

    private void populateChanges(StringBuffer stdout, List<Change> changes)
    {
        // RE to capture depot file, revision and local file
        Matcher matcher = syncPattern.matcher(stdout);

        while (matcher.find())
        {
            FileRevision fileRevision = new NumericalFileRevision(Long.parseLong(matcher.group(2)));
            changes.add(new Change(matcher.group(1), fileRevision, decodeAction(matcher.group(3))));
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
        P4Client.P4Result result = client.runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_DESCRIBE, FLAG_SHORT, Long.toString(number));
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

        NumericalRevision revision = new NumericalRevision(number);
        revision.setDate(date);
        revision.setAuthor(user);
        revision.setComment(comment);
        // branch??

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
        String clientName = updateClient(id, toDirectory);

        try
        {
            if (revision == null)
            {
                revision = getLatestRevision(clientName);
            }

            long number = ((NumericalRevision) revision).getRevisionNumber();
            P4CheckoutHandler p4Handler = new P4CheckoutHandler(force, handler);

            if(force)
            {
                client.runP4WithHandler(p4Handler, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_SYNC, FLAG_FORCE, "@" + Long.toString(number));
            }
            else
            {
                client.runP4WithHandler(p4Handler, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_SYNC, "@" + Long.toString(number));
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

    public P4Server(String port, String user, String password, String client)
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

        if (password != null)
        {
            this.client.setEnv(ENV_PASSWORD, password);
        }

        this.client.setEnv(ENV_CLIENT, client);
    }

    public Map<String, String> getServerInfo() throws SCMException
    {
        return client.getServerInfo(templateClient);
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
        if(!clientExists(templateClient))
        {
            throw new SCMException("Client '" + templateClient + "' does not exist");
        }
    }

    public Revision checkout(String id, File toDirectory, Revision revision, SCMCheckoutEventHandler handler) throws SCMException
    {
        return sync(id, toDirectory, revision, handler, true);
    }

    public String checkout(Revision revision, String file) throws SCMException
    {
        String clientName = updateClient(null, null);

        try
        {
            File fullFile = new File(clientRoot, file);

            String fileArgument = fullFile.getAbsolutePath();
            if (revision != null)
            {
                fileArgument = fileArgument + "@" + revision;
            }

            P4Client.P4Result result = client.runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, "print", "-q", fileArgument);
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

        String clientName = updateClient(null, null);

        if(to == null)
        {
            to = getLatestRevision(clientName);
        }

        long start = ((NumericalRevision) from).getRevisionNumber() + 1;
        long end = ((NumericalRevision) to).getRevisionNumber();

        try
        {
            if (start <= end)
            {
                P4Client.P4Result p4Result = client.runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_CHANGES, FLAG_STATUS, VALUE_SUBMITTED, clientRoot.getAbsoluteFile() + "/...@" + Long.toString(start) + "," + Long.toString(end));
                Matcher matcher = client.getChangesPattern().matcher(p4Result.stdout);

                while (matcher.find())
                {
                    NumericalRevision revision = new NumericalRevision(Long.parseLong(matcher.group(1)));
                    result.add(0, revision);

                    if(changes != null)
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

    public boolean hasChangedSince(Revision since) throws SCMException
    {
        String clientName = updateClient(null, null);
        try
        {
            String root = new File(clientRoot.getAbsolutePath(), VALUE_ALL_FILES).getAbsolutePath();
            long latestRevision = client.getLatestRevisionForFiles(clientName, root).getRevisionNumber();
            long sinceRevision = ((NumericalRevision) since).getRevisionNumber();
            if(latestRevision > sinceRevision)
            {
                if(excludedPaths != null && excludedPaths.size() > 0)
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
        for(long revision = sinceRevision + 1; revision <= latestRevision; revision++)
        {
            if(getChangelist(clientName, revision) != null)
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
        String clientName = updateClient(null, null);
        try
        {
            if(!labelExists(clientName, name))
            {
                createLabel(clientName, name);
            }
            else if(!moveExisting)
            {
                throw new SCMException("Cannot create label '" + name + "': label already exists");
            }

            client.runP4(false, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_LABELSYNC, FLAG_LABEL, name, clientRoot.getAbsoluteFile() + "/...@" + revision.toString());
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public void writeConnectionDetails(File outputDir) throws SCMException, IOException
    {
        P4Client.P4Result result = client.runP4(null, P4_COMMAND, FLAG_CLIENT, templateClient, COMMAND_INFO);
        FileSystemUtils.createFile(new File(outputDir, "server-info.txt"), result.stdout.toString());

        result = client.runP4(null, P4_COMMAND, FLAG_CLIENT, templateClient, COMMAND_CLIENT, FLAG_OUTPUT);
        FileSystemUtils.createFile(new File(outputDir, "template-client.txt"), result.stdout.toString());
    }

    public FileStatus.EOLStyle getEOLPolicy() throws SCMException
    {
        final FileStatus.EOLStyle[] eol = new FileStatus.EOLStyle[]{FileStatus.EOLStyle.NATIVE};

        client.runP4WithHandler(new P4ErrorDetectingHandler(true)
        {
            public void handleStdout(String line) throws SCMException
            {
                if(line.startsWith("LineEnd:"))
                {
                    String ending = line.substring(8).trim();
                    if(ending.equals("local"))
                    {
                       eol[0] = FileStatus.EOLStyle.NATIVE;
                    }
                    else if(ending.equals("unix") || ending.equals("share"))
                    {
                       eol[0] = FileStatus.EOLStyle.LINEFEED;
                    }
                    else if(ending.equals("mac"))
                    {
                       eol[0] = FileStatus.EOLStyle.CARRIAGE_RETURN;
                    }
                    else if(ending.equals("win"))
                    {
                       eol[0] = FileStatus.EOLStyle.CARRIAGE_RETURN_LINEFEED;
                    }
                }
            }

            public void checkCancelled() throws SCMCancelledException
            {
            }
        }, null, P4_COMMAND, FLAG_CLIENT, templateClient, COMMAND_CLIENT, FLAG_OUTPUT);

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
        String clientName = updateClient(null, null);
        try
        {
            File f = new File(clientRoot.getAbsoluteFile(), path);
            P4Client.P4Result result = client.runP4(false, null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_FSTAT,  f.getAbsolutePath() + "@" + repoRevision.getRevisionString());
            if(result.stderr.length() > 0)
            {
                String error = result.stderr.toString();
                if(error.contains("no file(s) at that changelist number") || error.contains("no such file(s)"))
                {
                    return null;
                }
                else
                {
                    throw new SCMException("Error running p4 fstat: " + result.stderr);
                }
            }
            else if(result.stdout.toString().contains("... headAction delete"))
            {
                return null;
            }
            else
            {
                Pattern revPattern = Pattern.compile("... headRev ([0-9]+)");
                for(String line: client.splitLines(result))
                {
                    Matcher m = revPattern.matcher(line);
                    if(m.matches())
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

    public boolean labelExists(String client, String name) throws SCMException
    {
        P4Client.P4Result p4Result = this.client.runP4(null, P4_COMMAND, FLAG_CLIENT, client, COMMAND_LABELS);

        // $ p4 labels
        // Label jim 2006/06/20 'Created by Jason. '
        Pattern splitter = Pattern.compile("^Label (.+) [0-9/]+ '.*'$", Pattern.MULTILINE);
        Matcher matcher = splitter.matcher(p4Result.stdout);
        while (matcher.find())
        {
            if(matcher.group(1).equals(name))
            {
                return true;
            }
        }

        return false;
    }

    private void createLabel(String client, String name) throws SCMException
    {
        P4Client.P4Result p4Result = this.client.runP4(null, P4_COMMAND, FLAG_CLIENT, client, COMMAND_LABEL, FLAG_OUTPUT, name);
        this.client.runP4(p4Result.stdout.toString(), P4_COMMAND, FLAG_CLIENT, client, COMMAND_LABEL, FLAG_INPUT);
    }

    public static void main(String argv[])
    {
        P4Server server = new P4Server("localhost:1666", "jsankey", "", "pulse-demo");

        try
        {
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
