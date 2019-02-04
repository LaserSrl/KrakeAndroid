package com.krake.core.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by joel on 24/07/15.
 */
public class FileUtils {
    public static final String AUTHORITY = "com.mykrake";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    public static void copyToFile(final InputStream input, final File destination) throws IOException {
        final FileOutputStream output = openOutputStream(destination, false);
        try {

            int n;
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            output.flush();
            output.getFD().sync();
            output.close(); // don't swallow close Exception if copy completes normally
        } finally {
            closeQuietly(output);
        }
    }

    public static FileOutputStream openOutputStream(final File file, final boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            final File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }


}
