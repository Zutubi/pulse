package com.cinnamonbob.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Daniel Ostermeier
 */
public class IOHelper {


    public static void close(Socket s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (IOException e) {
            // nop
        }
    }
    

    public static void joinStreams(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[1024];
        int    n;
        
        while((n = input.read(buffer)) > 0)
        {
            output.write(buffer, 0, n);
        }
    }

    
    public static void copyFile(File fromFile, File toFile) throws IOException
    {
        FileInputStream  inStream  = null;
        FileOutputStream outStream = null;
        
        try
        {
            inStream  = new FileInputStream(fromFile);
            outStream = new FileOutputStream(toFile);
            joinStreams(inStream, outStream);
        }
        finally
        {
            if(inStream != null)
            {
                inStream.close();
            }
            
            if(outStream != null)
            {
                outStream.close();
            }
        }
    }
}
