package com.zutubi.pulse.license;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <class-comment/>
 */
public class LicenseEncoder implements LicenseKeyFactory
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

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
        StringBuffer buffer = new StringBuffer();
        buffer.append(license.getType().ordinal()).append("\n");
        buffer.append(license.getHolder()).append("\n");
        if (license.expires())
        {
            buffer.append(DATE_FORMAT.format(license.getExpiryDate())).append("\n");
        }
        else
        {
            buffer.append("Never").append("\n");
        }
        return buffer.toString();
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
        // todo: some form of validation would be nice.
        // todo: support multiple license types
        try
        {
            SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            int type = Integer.valueOf(argv[0]);
            String name = argv[1];
            Date expiry = expiryFormat.parse(argv[2]);

            License license = new License(LicenseType.valueOf(type), name, expiry);

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
}
