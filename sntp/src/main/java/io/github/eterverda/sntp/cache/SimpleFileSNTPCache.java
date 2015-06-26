package io.github.eterverda.sntp.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            final String line = reader.readLine();

            return SNTPResponse.unflattenFromString(line);

        } catch (ParseException e) {
            if (!file.delete()) {
                throw new IOException("cannot delete malformed file " + file, e);
            }
            throw e;
        }
    }

    @Override
    public void put(SNTPResponse response) {
        try {
            write(response, file);
        } catch (IOException ignore) {
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static void write(SNTPResponse response, File file) throws IOException {
        if (response == null) {
            if (!file.delete()) {
                throw new IOException("cannot delete file " + file);
            }
            return;
        }

        final String string = response.flattenToString();

        final File dir = file.getParentFile();
        if (!dir.mkdirs()) {
            throw new IOException("cannot make directory " + dir);
        }

        final PrintWriter out = new PrintWriter(new FileWriter(file));
        try {
            out.println(string);
        } finally {
            out.close();
        }
    }

}
