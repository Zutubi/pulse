package com.zutubi.pulse.master.license;

import org.apache.commons.cli.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * A command line interface that provides the website with access to the
 * license encoding / decoding system.
 */
public class LicenseCommandLineInterface
{
    private Command command;

    public int execute()
    {
        return command.execute();
    }

    private void parse(String... argv) throws Exception
    {
        String request = argv[0];
        String[] requestArgs = new String[argv.length - 1];
        System.arraycopy(argv, 1, requestArgs, 0, requestArgs.length);

        if (request.equals("encode"))
        {
            command = new Encode();
            command.parse(requestArgs);
        }
        else if (request.equals("decode"))
        {
            command = new Decode();
            command.parse(requestArgs);
        }
        else
        {
            throw new ParseException("Unknown request type: " + request);
        }
    }

    public static void main(String[] argv) throws Exception
    {
        LicenseCommandLineInterface cli = new LicenseCommandLineInterface();
        cli.parse(argv);
        System.exit(cli.execute());
    }

    private interface Command
    {
        void parse(String... argv) throws Exception;
        int execute();
    }

    private SimpleDateFormat getDateFormat()
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }

    private class Encode implements Command
    {

        private License license;

        @SuppressWarnings({"AccessStaticViaInstance" })
        public void parse(String... argv) throws Exception
        {
            Options options = new Options();
            options.addOption(OptionBuilder.withLongOpt("type")
                    .hasArg()
                    .create('t'));

            options.addOption(OptionBuilder.withLongOpt("holder")
                    .hasArg()
                    .create('h'));

            options.addOption(OptionBuilder.withLongOpt("expiry")
                    .hasArg()
                    .create('e'));

            options.addOption(OptionBuilder.withLongOpt("supported")
                    .hasArg()
                    .create('s'));

            CommandLineParser parser = new PosixParser();
            CommandLine commandLine = parser.parse(options, argv, true);

            String type = commandLine.getOptionValue('t');
            String holder = commandLine.getOptionValue('h');
            String expiryString = commandLine.getOptionValue('e');
            Date expiry = null;
            if (!expiryString.equals("Never"))
            {
                expiry = getDateFormat().parse(expiryString);
            }

            String supportedString = commandLine.getOptionValue('s');
            StringTokenizer tokens = new StringTokenizer(supportedString, ":", false);
            int supportedAgents = Integer.valueOf(tokens.nextToken());
            int supportedProjects = Integer.valueOf(tokens.nextToken());
            int supportedUsers = Integer.valueOf(tokens.nextToken());
            int supportedContactPoints = Integer.valueOf(tokens.nextToken());

            license = new License(LicenseType.valueOf(type), holder, expiry);
            license.setSupported(supportedAgents, supportedProjects, supportedUsers);
            license.setSupportedContactPoints(supportedContactPoints);
        }

        public int execute()
        {
            LicenseEncoder encoder = new LicenseEncoder();
            String encodedLicense = new String(encoder.encode(license));
            System.out.println(encodedLicense);
            return 0;
        }

    }

    private class Decode implements Command
    {

        private String key;

        @SuppressWarnings({"AccessStaticViaInstance" })
        public void parse(String... argv) throws Exception
        {
            Options options = new Options();
            options.addOption(OptionBuilder.withLongOpt("key")
                    .hasArg()
                    .create('k'));

            CommandLineParser parser = new PosixParser();
            CommandLine commandLine = parser.parse(options, argv, true);
            key = commandLine.getOptionValue('k');
        }

        public int execute()
        {
            LicenseDecoder decoder = new LicenseDecoder();
            License license = decoder.decode(key);
            System.out.println("type:" + license.getType().toString());
            System.out.println("holder:" + license.getHolder());
            System.out.println("expiry:" + ((license.getExpiryDate() != null) ? getDateFormat().format(license.getExpiryDate()): "Never"));
            System.out.println("supports:" + license.getSupportedAgents() + ":" +
                    license.getSupportedProjects() + ":" +
                    license.getSupportedUsers() + ":" +
                    license.getSupportedContactPoints());
            return 0;
        }

    }

}
