package com.zutubi.pulse.license;

import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Date;

/**
 * <class-comment/>
 */
public class LicenseDecoder
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    /**
     * Base 64 encoded public key.
     */
    private static final byte[] PUBLIC_RSA_KEY =
            ("MIGeMA0GCSqGSIb3DQEBAQUAA4GMADCBiAKBgGOte/JJ03YV1QPa7cDwMd5oJEBz15iL1gQzU/QW9EaS" +
                    "xJtqrYf3DVKE+YOOGBoemnfxv2me6BZ2KQl4++xyw98Hou7uTu84qhdR+IU9jCp96sExUV6GGEmqBpca" +
                    "PYVXIwb48/NndEhEktXIc4hpMXhWWxF1GbMb8bLf3umas4onAgMBAAE=").getBytes();

    public License decode(byte[] raw) throws LicenseException
    {
        // remove the base 64 encoding.

        // extract the data and the digital signature.
        byte[] data = Base64.decodeBase64(raw);

        // read the first 4 bytes, which make up the length of the license data portion of the license.
        int length = ((0xff & data[0]) << 24) + ((0xff & data[1]) << 16) + ((0xff & data[2]) << 8) + (0xff & data[3]);

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
        return fromString(new String(licenseStr));
    }

    private License fromString(String data) throws LicenseException
    {
        try
        {
            LineNumberReader reader = new LineNumberReader(new StringReader(data));
            String holder = reader.readLine();
            String expiryString = reader.readLine();
            Date expiryDate = null;
            if (!expiryString.equals("Never"))
            {
                expiryDate = DATE_FORMAT.parse(expiryString);
            }
            return new License(holder, expiryDate);
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

}
