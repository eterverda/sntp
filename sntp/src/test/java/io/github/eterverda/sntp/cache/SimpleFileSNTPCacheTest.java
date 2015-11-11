package io.github.eterverda.sntp.cache;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import io.github.eterverda.sntp.SNTPResponse;

public class SimpleFileSNTPCacheTest {
    private static final String WELL_FORMED_RESPONSE_STRING = "sys 2015-11-11T08:56:13.884Z ntp 2015-11-11T08:56:14.885Z off 1001";
    private static SNTPResponse RESPONSE;

    private File file;
    private SNTPCache cache;

    @BeforeClass
    public static void setUpClass() throws ParseException {
        RESPONSE = SNTPResponse.unflattenFromString(WELL_FORMED_RESPONSE_STRING);
    }

    @Before
    public void setUp() throws IOException {
        file = File.createTempFile("sntp", null);
        Assume.assumeTrue(file.exists());

        cache = new SimpleFileSNTPCache(file);
    }

    @After
    public void tearDown() throws IOException {
        cache = null;
        if (file.exists() && !file.delete()) {
            throw new IOException("cannot delete " + file);
        }
        file = null;
    }

    @Test
    public void testWrite() {
        cache.put(RESPONSE);

        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.length() > 0);
    }

    @Test
    public void testWriteNull() throws IOException {
        cache.put(null);

        Assert.assertFalse(file.exists());
    }

    @Test
    public void testRead() throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(WELL_FORMED_RESPONSE_STRING);
        writer.close();

        final SNTPResponse response = cache.get();

        Assert.assertNotNull(response);
        Assert.assertEquals(WELL_FORMED_RESPONSE_STRING, response.flattenToString());
    }

    @Test
    public void testReadNonExistent() {
        Assume.assumeTrue(file.delete());

        final SNTPResponse response = cache.get();

        Assert.assertNull(response);
    }

    @Test
    public void testReadEmpty() {
        Assume.assumeTrue(file.length() == 0);

        final SNTPResponse response = cache.get();

        Assert.assertNull(response);
        Assert.assertFalse(file.exists());
    }

    @Test
    public void testReadAbracadabra() throws IOException {
        final PrintWriter writer = new PrintWriter (new FileWriter(file));
        writer.println("ABRACADABRA");
        writer.println("ABRACADABR");
        writer.println("ABRACADAB");
        writer.println("ABRACADA");
        writer.println("ABRACAD");
        writer.println("ABRACA");
        writer.println("ABRAC");
        writer.println("ABRA");
        writer.println("ABR");
        writer.println("AB");
        writer.println("A");
        writer.println();
        writer.close();

        Assume.assumeFalse(writer.checkError());

        final SNTPResponse response = cache.get();

        Assert.assertNull(response);
        Assert.assertFalse(file.exists());
    }

    @Test
    public void testReadWrite() {
        cache.put(RESPONSE);
        final SNTPResponse response = cache.get();

        Assert.assertEquals(WELL_FORMED_RESPONSE_STRING, response.flattenToString());
    }
}
