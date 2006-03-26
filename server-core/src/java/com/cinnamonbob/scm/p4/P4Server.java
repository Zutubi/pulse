package com.cinnamonbob.scm.p4;

import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.NumericalRevision;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.util.logging.Logger;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class P4Server implements SCMServer
{
    private static final Logger LOG = Logger.getLogger(P4Server.class);

    private static final String ENV_PORT = "P4PORT";
    private static final String ENV_USER = "P4USER";
    private static final String ENV_PASSWORD = "P4PASSWD";
    private static final String ENV_CLIENT = "P4CLIENT";
    private static final String P4_COMMAND = "p4";
    private static final String COMMAND_CHANGES = "changes";
    private static final String COMMAND_CLIENT = "client";
    private static final String COMMAND_DESCRIBE = "describe";
    private static final String COMMAND_SYNC = "sync";
    private static final String FLAG_CLIENT = "-c";
    private static final String FLAG_DELETE = "-d";
    private static final String FLAG_FORCE = "-f";
    private static final String FLAG_INPUT = "-i";
    private static final String FLAG_MAXIMUM = "-m";
    private static final String FLAG_OUTPUT = "-o";
    private static final String FLAG_SHORT = "-s";
    private static final String FLAG_STATUS = "-s";
    private static final String VALUE_SUBMITTED = "submitted";
    private static final String VALUE_ALL_FILES = "...";
    private static final String ASCII_CHARSET = "US-ASCII";

    private ProcessBuilder p4Builder;
    private Pattern changesPattern;
    private String templateClient;
    private File clientRoot;
    private String port;

    private class P4Result
    {
        public StringBuffer stdout;
        public StringBuffer stderr;
        public int exitCode;
    }

    private void setEnv(String variable, String value)
    {
        if (value != null)
        {
            p4Builder.environment().put(variable, value);
        }
    }

    private P4Result runP4(String input, String ...commands) throws SCMException
    {
        P4Result result = new P4Result();
        Process child;

        p4Builder.command(commands);

        try
        {
            child = p4Builder.start();
        }
        catch (IOException e)
        {
            throw new SCMException("Could not start p4 process", e);
        }

        if (input != null)
        {
            try
            {
                OutputStream stdinStream = child.getOutputStream();

                stdinStream.write(input.getBytes(ASCII_CHARSET));
                stdinStream.close();
            }
            catch (IOException e)
            {
                throw new SCMException("Error writing to input of p4 process", e);
            }
        }

        try
        {
            InputStreamReader stdoutReader = new InputStreamReader(child.getInputStream(), ASCII_CHARSET);
            InputStreamReader stderrReader = new InputStreamReader(child.getErrorStream(), ASCII_CHARSET);
            StringWriter stdoutWriter = new StringWriter();
            StringWriter stderrWriter = new StringWriter();

            IOUtils.joinReaderToWriter(stdoutReader, stdoutWriter);
            IOUtils.joinReaderToWriter(stderrReader, stderrWriter);

            result.exitCode = child.waitFor();
            result.stdout = stdoutWriter.getBuffer();
            result.stderr = stderrWriter.getBuffer();
        }
        catch (IOException e)
        {
            throw new SCMException("Error reading output of p4 process", e);
        }
        catch (InterruptedException e)
        {
            // Do nothing
        }

        if (result.exitCode != 0)
        {
            String message = "p4 process returned non-zero exit code: " + Integer.toString(result.exitCode);

            if (result.stderr.length() > 0)
            {
                message += ", error '" + result.stderr.toString() + "'";
            }

            throw new SCMException(message);
        }

        if (result.stderr.length() > 0)
        {
            throw new SCMException("p4 process returned error '" + result.stderr.toString().trim() + "'");
        }

        return result;
    }

    private String updateClient(long id, File toDirectory) throws SCMException
    {
        if (id == 0)
        {
            id = (long) (Math.random() * 100000);
        }

        if (toDirectory == null)
        {
            toDirectory = new File(".");
        }

        P4Result result = runP4(null, P4_COMMAND, COMMAND_CLIENT, FLAG_OUTPUT);
        String clientSpec = result.stdout.toString();
        String clientName = "bob-temp-" + id;

        clientSpec = clientSpec.replaceAll("\nRoot:.*", Matcher.quoteReplacement("\nRoot: " + toDirectory.getAbsolutePath()));
        clientSpec = clientSpec.replaceAll("\nHost:.*", Matcher.quoteReplacement("\nHost: "));
        clientSpec = clientSpec.replaceAll("\nClient:.*" + templateClient, Matcher.quoteReplacement("\nClient: " + clientName));
        clientSpec = clientSpec.replaceAll("//" + templateClient + "/", Matcher.quoteReplacement("//" + clientName + "/"));

        runP4(clientSpec, P4_COMMAND, COMMAND_CLIENT, FLAG_INPUT);
        clientRoot = toDirectory;

        return clientName;
    }

    public NumericalRevision getLatestRevision() throws SCMException
    {
        String clientName = updateClient(0, null);

        try
        {
            return getLatestRevisionForFiles(clientName);
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
            runP4(null, P4_COMMAND, COMMAND_CLIENT, FLAG_DELETE, clientName);
        }
        catch (SCMException e)
        {
            LOG.warning("Unable to delete client: " + e.getMessage(), e);
        }
    }

    private NumericalRevision getLatestRevisionForFiles(String clientName, String ...files) throws SCMException
    {
        String args[] = new String[8 + files.length];

        args[0] = P4_COMMAND;
        args[1] = FLAG_CLIENT;
        args[2] = clientName;
        args[3] = COMMAND_CHANGES;
        args[4] = FLAG_STATUS;
        args[5] = VALUE_SUBMITTED;
        args[6] = FLAG_MAXIMUM;
        args[7] = "1";

        for (int i = 0; i < files.length; i++)
        {
            args[8 + i] = files[i];
        }

        P4Result result = runP4(null, args);
        Matcher matcher = changesPattern.matcher(result.stdout);

        if (matcher.find())
        {
            return new NumericalRevision(Long.parseLong(matcher.group(1)));
        }
        else
        {
            return new NumericalRevision(0);
        }
    }

    private void populateChanges(StringBuffer stdout, List<Change> changes)
    {
        // Output of p4 sync -f:
        //   <depot file>#<revision> - (refreshing|added as) <local file>
        //   <depot file>#<revision> - (refreshing|added as) <local file>
        //   ...

        // RE to capture depot file, revision and local file
        Pattern re = Pattern.compile("^(.+)#([0-9]+) - (refreshing|added as) (.+)$", Pattern.MULTILINE);
        Matcher matcher = re.matcher(stdout);

        while (matcher.find())
        {
            changes.add(new Change(matcher.group(1), matcher.group(2), Change.Action.ADD));
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
        P4Result result = runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_DESCRIBE, FLAG_SHORT, Long.toString(number));
        Pattern splitter = Pattern.compile("\r?\n");
        String[] lines = splitter.split(result.stdout);

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

        Changelist changelist = new Changelist(revision);

        for (int i = affectedFilesIndex + 2; i < lines.length; i++)
        {
            changelist.addChange(getChangelistChange(lines[i]));
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
            return new Change(matcher.group(1), matcher.group(2), decodeAction(matcher.group(3)));
        }
        else
        {
            throw new SCMException("Could not parse affected file line from p4 describe '" + line + "'");
        }
    }

    private Change.Action decodeAction(String action)
    {
        if (action.equals("add"))
        {
            return Change.Action.ADD;
        }
        else if (action.equals("branch"))
        {
            return Change.Action.BRANCH;
        }
        else if (action.equals("delete"))
        {
            return Change.Action.DELETE;
        }
        else if (action.equals("edit"))
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
        p4Builder = new ProcessBuilder();
        templateClient = client;
        this.port = port;
        // Output of p4 changes -s submitted -m 1:
        //   Change <number> on <date> by <user>@<client>
        changesPattern = Pattern.compile("^Change ([0-9]+) on (.+) by (.+)@(.+) '(.+)'$", Pattern.MULTILINE);

        setEnv(ENV_PORT, port);
        setEnv(ENV_USER, user);

        if (password != null)
        {
            setEnv(ENV_PASSWORD, password);
        }

        setEnv(ENV_CLIENT, client);
    }

    public String getLocation()
    {
        return templateClient + "@" + port;
    }

    public void testConnection() throws SCMException
    {
        runP4(null, P4_COMMAND, COMMAND_CLIENT, FLAG_OUTPUT);
    }

    public Revision checkout(long id, File toDirectory, Revision revision, List<Change> changes) throws SCMException
    {
        String clientName = updateClient(id, toDirectory);

        try
        {
            if (revision == null)
            {
                revision = getLatestRevision();
            }

            long number = ((NumericalRevision) revision).getRevisionNumber();
            P4Result result = runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_SYNC, FLAG_FORCE, "@" + Long.toString(number));

            if (changes != null)
            {
                populateChanges(result.stdout, changes);
            }
        }
        finally
        {
            deleteClient(clientName);
        }

        return revision;
    }

    public String checkout(long id, Revision revision, String file) throws SCMException
    {
        String clientName = updateClient(id, null);

        try
        {
            File fullFile = new File(clientRoot, file);

            String fileArgument = fullFile.getAbsolutePath();
            if (revision != null)
            {
                fileArgument = fileArgument + "@" + revision;
            }

            P4Result result = runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, "print", "-q", fileArgument);
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

        long start = ((NumericalRevision) from).getRevisionNumber() + 1;
        long end = ((NumericalRevision) to).getRevisionNumber();

        String clientName = updateClient(0, null);

        try
        {
            if (start <= end)
            {
                P4Result p4Result = runP4(null, P4_COMMAND, FLAG_CLIENT, clientName, COMMAND_CHANGES, FLAG_STATUS, VALUE_SUBMITTED, clientRoot.getAbsoluteFile() + "/...@" + Long.toString(start) + "," + Long.toString(end));
                Matcher matcher = changesPattern.matcher(p4Result.stdout);

                while (matcher.find())
                {
                    Changelist list = getChangelist(clientName, Long.parseLong(matcher.group(1)));

                    if (list != null)
                    {
                        result.add(list);
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
        String clientName = updateClient(0, null);
        try
        {
            String root = new File(clientRoot.getAbsolutePath(), VALUE_ALL_FILES).getAbsolutePath();
            return getLatestRevisionForFiles(clientName, root).getRevisionNumber() > ((NumericalRevision) since).getRevisionNumber();
        }
        finally
        {
            deleteClient(clientName);
        }
    }

    public static void main(String argv[])
    {
        P4Server server = new P4Server("localhost:1666", "jsankey", "", "bob-demo");

        try
        {
            server.checkout(1, new NumericalRevision(2), "file");
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
