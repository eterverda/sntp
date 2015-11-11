package io.github.eterverda.sntp.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import io.github.eterverda.sntp.SNTPResponse;

final class SimpleFileSNTPCache implements SNTPCache {

    private final File file;

    public SimpleFileSNTPCache(File file) {
        this.file = file;
    }

    @Override
    public SNTPResponse get() {
        try {
            return read(file);
        } catch (IOException | ParseException e) {
            return null;
        }
    }

    public static SNTPResponse read(File file) throws IOException, ParseException {
        if (!file.exists()) {
            throw new IOException("file " + file + " does not exist");
        }

        final String string = readString(file);

        try {
            return SNTPResponse.unflattenFromString(string);
        } catch (ParseException e) {
            if (!file.delete()) {
                throw new IOException("cannot delete malformed file " + file, e);
            }
            throw e;
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static String readString(File file) throws IOException {
        final BufferedReader in = new BufferedReader(new FileReader(file));
        try {
            final StringBuilder result = new StringBuilder(128); // should be enough for well-formed SNTPResponse
            for(int i = in.read(); i >= 0; i = in.read()) {
                final char c = (char) i;
                result.append(c);
            }
            return result.toString().trim();
        } finally {
            in.close();
        }
    }

    @Override
    public void put(SNTPResponse response) {
        try {
            write(file, response);
        } catch (IOException ignore) {
        }
    }

    public static void write(File file, SNTPResponse response) throws IOException {
        if (response == null) {
            if (!file.delete()) {
                throw new IOException("cannot delete file " + file);
            }
            return;
        }

        final String string = response.flattenToString();

        writeString(file, string);
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static void writeString(File file, String string) throws IOException {
        final File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("cannot make directory " + dir);
        }

        final BufferedWriter out = new BufferedWriter(new FileWriter(file));
        try {
            out.write(string);
            out.write('\n'); // some pretty printing
            out.flush();
        } finally {
            out.close();
        }
    }
}
