/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * utility methods for reading files from tests
 * 
 * Could have used similiar code in FileUtilities, but cannot have a dependency on services-test (would have
 * introduced a cyclic dependency, as services-test uses the json parser in services-json)
 *
 * @author EEMECOY
 */
public class FileReader {

    public static String readInputStream(final InputStream fileInputStream) throws IOException {
        BufferedInputStream bufferedInputStream = null;
        DataInputStream dataInputStream = null;
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            dataInputStream = new DataInputStream(bufferedInputStream);

            while (dataInputStream.available() != 0) {
                stringBuilder.append(dataInputStream.readLine());
                if (dataInputStream.available() != 0) {
                    stringBuilder.append(System.getProperty("line.separator"));

                }
            }

        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (dataInputStream != null) {
                dataInputStream.close();
            }
        }
        return stringBuilder.toString();
    }

}
