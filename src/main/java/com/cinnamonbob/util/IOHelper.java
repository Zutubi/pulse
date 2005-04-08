package com.cinnamonbob.util;

import java.io.IOException;
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
}
