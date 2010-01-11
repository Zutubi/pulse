package com.zutubi.pulse.master.license;

import com.zutubi.util.StringUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Decodes base64 license strings into {@link License} objects.
 */
public class LicenseDecoder
{
    static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss z";

    /**
     * Base 64 encoded public key.
     */
    private static final byte[] PUBLIC_RSA_KEY =
            ("MIGeMA0GCSqGSIb3DQEBAQUAA4GMADCBiAKBgGOte/JJ03YV1QPa7cDwMd5oJEBz15iL1gQzU/QW9EaS" +
                    "xJtqrYf3DVKE+YOOGBoemnfxv2me6BZ2KQl4++xyw98Hou7uTu84qhdR+IU9jCp96sExUV6GGEmqBpca" +
                    "PYVXIwb48/NndEhEktXIc4hpMXhWWxF1GbMb8bLf3umas4onAgMBAAE=").getBytes();

    public License decode(String raw) throws LicenseException
    {
        return decode(raw.getBytes());
    }

    public License decode(byte[] raw) throws LicenseException
    {
        // remove the base 64 encoding.

        // extract the data and the digital signature.
        byte[] data = Base64.decodeBase64(raw);

        if (data.length < 4)
        {
            // there is not enough room for the length field at the start of the data. data is invalid.
            return null;
        }

        // read the first 4 bytes, which make up the length of the license data portion of the license.
        int length = ((0xff & data[0]) << 24) + ((0xff & data[1]) << 16) + ((0xff & data[2]) << 8) + (0xff & data[3]);
        if (length < 0)
        {
            // invalid length.
            return null;
        }

        if (data.length < 4 + length)
        {
            // the reported data length more then the available data. data is invalid.
            return null;
        }

        byte[] licenseStr = new byte[length];
        System.arraycopy(data, 4, licenseStr, 0, licenseStr.length);

        byte[] sig = new byte[data.length - 4 - length];
        System.arraycopy(data, 4 + length, sig, 0, sig.length);

        // verify the digital signature.
        if (!verifySignature(licenseStr, sig))
        {
            return null;
        }

        // create the license instance.
        try
        {
            return fromString(new String(licenseStr, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new LicenseException(e);
        }
    }

    private License fromString(String data) throws LicenseException
    {
        try
        {
            LineNumberReader reader = new LineNumberReader(new StringReader(data));
            String code = reader.readLine();
            String holder = reader.readLine();
            String expiryString = reader.readLine();
            String agents = reader.readLine();
            String projects = reader.readLine();
            String users = reader.readLine();

            // contact points were not restricted until Pulse v1.2, License generated before then will not have
            // contact points specified.
            String contactPoints = reader.readLine();

            // Support old 1-letter codes
            if(code != null && code.length() == 1)
            {
                code = mapCode(code);
            }

            // verify that all of the expected fields where available.
            if (!StringUtils.stringSet(code) ||
                    !StringUtils.stringSet(holder) ||
                    !StringUtils.stringSet(expiryString))
            {
                return null;
            }


            LicenseType type;
            try
            {
                type = LicenseType.valueOf(code);
            }
            catch(IllegalArgumentException e)
            {
                return null;
            }

            Date expiryDate = null;
            if (!expiryString.equals("Never"))
            {
                DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
                expiryDate = dateFormat.parse(expiryString);
            }

            License license = new License(type, holder, expiryDate);
            license.setSupported(Integer.valueOf(agents), Integer.valueOf(projects), Integer.valueOf(users));
            if (contactPoints != null)
            {
                license.setSupportedContactPoints(Integer.valueOf(contactPoints));
            }
            
            return license;
        }
        catch (IOException e)
        {
            throw new LicenseException(e);
        }
        catch (ParseException e)
        {
            throw new LicenseException(e);
        }
    }

    private String mapCode(String code)
    {
        if(code.equals("e"))
        {
            return "EVALUATION";
        }
        else
        {
            return "CUSTOM";
        }
    }

    private static boolean verifySignature(byte[] licData, byte[] signData) throws LicenseException
    {
        try
        {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // re-create the public key.
            byte[] rawPublicKey = Base64.decodeBase64(PUBLIC_RSA_KEY);
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(rawPublicKey);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            // verify the signature
            Signature sign = Signature.getInstance("SHA1withRSA");
            sign.initVerify(publicKey);
            sign.update(licData);
            return sign.verify(signData);
        }
        catch (Exception e)
        {
            throw new LicenseException(e);
        }
    }

    public static void main(String[] argv)
    {
        LicenseDecoder decoder = new LicenseDecoder();
        try
        {
            String licenseString = StringUtils.join("", argv);
            License license = decoder.decode(licenseString.getBytes());
            System.out.println("license.getType() = " + license.getType());
            System.out.println("license.getHolder() = " + license.getHolder());
            System.out.println("license.getSupportedAgents() = " + license.getSupportedAgents());
            System.out.println("license.getExpiryDate() = " + license.getExpiryDate());
        }
        catch (LicenseException e)
        {
            e.printStackTrace();
        }
    }
}
