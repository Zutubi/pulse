package com.zutubi.pulse.master.license;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LicenseEncoder implements LicenseKeyFactory
{
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    /**
     * Base 64 encoded Private key
     */
    private static final byte[] PRIVATE_RSA_KEY =
            ("MIICdAIBADANBgkqhkiG9w0BAQEFAASCAl4wggJaAgEAAoGAY6178knTdhXVA9rtwPAx3mgkQHPXmIvW" +
                    "BDNT9Bb0RpLEm2qth/cNUoT5g44YGh6ad/G/aZ7oFnYpCXj77HLD3wei7u5O7ziqF1H4hT2MKn3qwTFR" +
                    "XoYYSaoGlxo9hVcjBvjz82d0SESS1chziGkxeFZbEXUZsxvxst/e6ZqziicCAwEAAQKBgEwVu3upILGN" +
                    "XqjvrvXMIrS615kfE52Md9OC/n1eHB3WoB5l4onbaZ7og7EIgJtHau9NZ6eOtWeX0CE76UiGHb4mZqVu" +
                    "aIAirggW8qYCG+HFdlYS4qsmsLuMZg53kuFsrxdEWrdcKOisxVMs67uM7C/Qvx5j2cEJ6GBLeqrQdkvR" +
                    "AkEAsPqW1VDhf5t4gIuGRacF9r7P//2Bjyx3umXhlgLvI47xeI2jKaACzPCX35VRGRGC7+P/sQRxYTN/" +
                    "NrwwDTgMWQJBAJAvDShUaFthqYtZTzS4uALLCmkJkWS4ue9ASCNidjrc8PXz1nZgIxHL8wJva8n7kd8Z" +
                    "UTJg5fHcXjpyEbm7en8CQG7qTfeYvgqMdGQTjW4/tDQk+BTWWwlQ9CRkz5GFezxMzLciBV0EBF1Od9BP" +
                    "M0lDuU0BFnFpeGlTremu3WqbctkCQDbVbg+Uaku2jKAuSu0mAvUs+ryPovfHOQ9ARy8N1yDzvcAMB9fl" +
                    "H/E4uyaF8VxTjFpoanTaXRjqUfuwPgWAw0kCQEMLVlYciUrCkovc17Tt8Zs6y+kJyyHCZGph2MaqJ/T4" +
                    "QN0u53TOJ82FvVPIDx/eXwGiHVX2gQfOHizksXh/kWw=").getBytes();

    public byte[] encode(License license) throws LicenseException
    {
        // generate license string.
        String licenseStr = toString(license);

        byte[] data = licenseStr.getBytes();

        try
        {
            // generate digital signature.
            byte[] sig = signData(data);

            // create resulting license string.
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4 + data.length + sig.length);
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(data.length);  // length 4.
            dos.write(data);            // length data.length.
            dos.write(sig);             // length sig.length.
            dos.close();

            // use base 64 encoding to make it transfer (email/webpage) friendly
            return Base64.encodeBase64(baos.toByteArray());
        }
        catch (Exception e)
        {
            throw new LicenseException(e);
        }
    }

    private String toString(License license)
    {
        // format:
        StringBuffer buffer = new StringBuffer();
        // 1) license type.
        writeLineTo(buffer, license.getType().toString());
        // 2) holder name.
        writeLineTo(buffer, license.getHolder());
        // 3) expiry date.
        if (license.expires())
        {
            DateFormat dateFormat = new SimpleDateFormat(LicenseDecoder.DATE_FORMAT_STRING);
            writeLineTo(buffer, dateFormat.format(license.getExpiryDate()));
        }
        else
        {
            writeLineTo(buffer, "Never");
        }
        // 4) supported entities.
        writeLineTo(buffer, String.valueOf(license.getSupportedAgents()));
        writeLineTo(buffer, String.valueOf(license.getSupportedProjects()));
        writeLineTo(buffer, String.valueOf(license.getSupportedUsers()));
        writeLineTo(buffer, String.valueOf(license.getSupportedContactPoints()));

        return buffer.toString();
    }

    private void writeLineTo(StringBuffer buffer, String content)
    {
        buffer.append(content).append("\n");
    }

    /**
     * Generate a signature.
     */
    private byte[] signData(byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException
    {
        // re-create the private key.
        byte[] rawPrivateKey = Base64.decodeBase64(PRIVATE_RSA_KEY);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(rawPrivateKey));

        Signature rsa = Signature.getInstance("SHA1withRSA");
        rsa.initSign(privateKey);
        rsa.update(data);

        return rsa.sign();
    }

    public static void main(String argv[])
    {
        try
        {

            String code = argv[0];
            String name = argv[1];
            Date expiry = null;

            if(argv.length > 2)
            {
                SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                expiry = expiryFormat.parse(argv[2]);
            }

            // Validation:
            LicenseType type = LicenseType.valueOf(code);
            if (type == null)
            {
                throw new IllegalArgumentException("Unknown license type "+ code +".");
            }

            if (name == null || name.trim().length() == 0)
            {
                throw new IllegalArgumentException("The name of the license holder is required.");
            }

            License license = new License(type, name, expiry);

            // setup some default supported entity values until they are added to the interface and we want to be
            // able to vary them.
            configureLicenseBasedOnType(license);

            LicenseEncoder encoder = new LicenseEncoder();
            byte[] licenseKey = encoder.encode(license);

            // print the license key to standard out - do not include a new line
            // since it will look like part of the license key to an external process.
            System.out.print(new String(licenseKey));
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void configureLicenseBasedOnType(License license)
    {
        if (license.getType() == LicenseType.ENTERPRISE)
        {
            license.setSupported(16, License.UNRESTRICTED, License.UNRESTRICTED);
            license.setSupportedContactPoints(License.UNRESTRICTED);
        }
        else if (license.getType() == LicenseType.EVALUATION)
        {
            // keep default values of License.UNDEFINED.
            license.setSupported(License.UNRESTRICTED, License.UNRESTRICTED, License.UNRESTRICTED);
            license.setSupportedContactPoints(License.UNRESTRICTED);
        }
        else if (license.getType() == LicenseType.NON_PROFIT)
        {
            license.setSupported(5, 5, License.UNRESTRICTED);
            license.setSupportedContactPoints(License.UNRESTRICTED);
        }
        else if (license.getType() == LicenseType.PROFESSIONAL)
        {
            license.setSupported(6, License.UNRESTRICTED, License.UNRESTRICTED);
            license.setSupportedContactPoints(License.UNRESTRICTED);
        }
        else if (license.getType() == LicenseType.SMALL_TEAM)
        {
            license.setSupported(1, 2, 2);
            license.setSupportedContactPoints(3);
        }
        else if (license.getType() == LicenseType.STANDARD)
        {
            license.setSupported(2, License.UNRESTRICTED, License.UNRESTRICTED);
            license.setSupportedContactPoints(License.UNRESTRICTED);
        }
    }

    private static class NewEvaluationLicense
    {
        public static void main(String[] args)
        {
            String projectName = "insert name here";

            // code, name, expiry
            SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar oneMonthFromToday = Calendar.getInstance();
            oneMonthFromToday.add(Calendar.MONTH, 1);

            // code, name, expiry
            LicenseEncoder.main(new String[]{LicenseType.EVALUATION.toString(), projectName, expiryFormat.format(oneMonthFromToday.getTime())});
        }
    }

    private static class NewOpenSourceLicense
    {
        public static void main(String[] args)
        {
            String projectName = "insert name here";

            // code, name, expiry
            LicenseEncoder.main(new String[]{LicenseType.NON_PROFIT.toString(), projectName});
        }
    }

    private static class NewSmallTeamLicense
    {
        public static void main(String[] args)
        {
            String projectName = "insert name here";

            // code, name, expiry
            LicenseEncoder.main(new String[]{LicenseType.SMALL_TEAM.toString(), projectName});
        }
    }

    private static class NewCommercialLicense
    {
        public void generateKey(String companyName, LicenseType type)
        {
            // code, name, expiry
            SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar oneYearFromToday = Calendar.getInstance();
            oneYearFromToday.add(Calendar.YEAR, 1);

            generateKey(companyName, type, expiryFormat.format(oneYearFromToday.getTime()));
        }

        public void generateKey(String companyName, LicenseType type, String date)
        {
            LicenseEncoder.main(new String[]{type.toString(), companyName, date});
        }
    }

    private static class NewStandardLicense extends NewCommercialLicense
    {
        public void generateKey(String companyName)
        {
            super.generateKey(companyName, LicenseType.STANDARD);
        }

        public static void main(String[] args)
        {
            String companyName = "insert name here";

            new NewStandardLicense().generateKey(companyName);
        }
    }

    private static class NewProfessionalLicense extends NewCommercialLicense
    {
        public void generateKey(String companyName)
        {
            super.generateKey(companyName, LicenseType.PROFESSIONAL);
        }

        public static void main(String[] args)
        {
            String companyName = "insert name here";

            new NewProfessionalLicense().generateKey(companyName);
        }
    }

    private static class NewEnterpriseLicense extends NewCommercialLicense
    {
        public void generateKey(String companyName)
        {
            super.generateKey(companyName, LicenseType.ENTERPRISE);
        }

        public static void main(String[] args)
        {
            String companyName = "insert name here";

            new NewEnterpriseLicense().generateKey(companyName);
        }
    }

    private static class RenewLicense
    {
        private String licenseKey;

        public RenewLicense(String licenseKey)
        {
            this.licenseKey = licenseKey;
        }

        public String renew()
        {
            LicenseDecoder decoder = new LicenseDecoder();
            License license = decoder.decode(licenseKey.getBytes());
            Calendar expiry = Calendar.getInstance();
            expiry.setTime(license.getExpiryDate());
            expiry.add(Calendar.YEAR, 1);
            license.setExpiryDate(expiry.getTime());

            System.out.println("Name: " + license.getHolder());
            System.out.println("Type: " + license.getType());
            System.out.println("Expiry: " + license.getExpiryDate());
            System.out.println(" - projects: " + license.getSupportedProjects());
            System.out.println(" - users: " + license.getSupportedUsers());
            System.out.println(" - agents: " + license.getSupportedAgents());
            System.out.println("");
            
            LicenseEncoder encoder = new LicenseEncoder();
            return new String(encoder.encode(license));
        }

        public static void main(String argv[])
        {
            String licenseKey = "";
            System.out.println(new RenewLicense(licenseKey).renew());
        }
    }

    private static class UpgradeLicense
    {
        public static void main(String[] argv)
        {
            String licenseKey = "";

            LicenseDecoder decoder = new LicenseDecoder();
            License license = decoder.decode(licenseKey.getBytes());

            System.out.println("Name: " + license.getHolder());
            System.out.println("Type: " + license.getType());
            System.out.println("Expiry: " + license.getExpiryDate());
            System.out.println(" - projects: " + license.getSupportedProjects());
            System.out.println(" - users: " + license.getSupportedUsers());
            System.out.println(" - agents: " + license.getSupportedAgents());
            System.out.println("");

            license.setType(LicenseType.ENTERPRISE);
            LicenseEncoder.configureLicenseBasedOnType(license);

            System.out.println("Name: " + license.getHolder());
            System.out.println("Type: " + license.getType());
            System.out.println("Expiry: " + license.getExpiryDate());
            System.out.println(" - projects: " + license.getSupportedProjects());
            System.out.println(" - users: " + license.getSupportedUsers());
            System.out.println(" - agents: " + license.getSupportedAgents());
            System.out.println("");

            LicenseEncoder encoder = new LicenseEncoder();
            System.out.println(new String(encoder.encode(license)));
        }
    }

    private static class ReadLicense
    {
        public static void main(String argv[])
        {
            String key = "";

            LicenseDecoder decoder = new LicenseDecoder();
            License license = decoder.decode(key.getBytes());

            System.out.println("Name: " + license.getHolder());
            System.out.println("Type: " + license.getType());
            System.out.println("Expiry: " + license.getExpiryDate());
            System.out.println(" - projects: " + license.getSupportedProjects());
            System.out.println(" - users: " + license.getSupportedUsers());
            System.out.println(" - agents: " + license.getSupportedAgents());
            System.out.println("");

        }
    }
}
