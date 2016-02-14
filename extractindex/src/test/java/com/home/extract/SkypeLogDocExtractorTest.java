package com.home.extract;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link SkypeLogDocExtractor} class.
 */
public class SkypeLogDocExtractorTest {
    private SkypeLogDocExtractor skypeLogDocExtractor;

    @Before
    public void setUp() throws IOException {
        skypeLogDocExtractor = SkypeLogDocExtractor.getInstance("src/test/java/resources/TestLog.txt",
                Long.MAX_VALUE, "[dd.mm.yyyy HH:mm:ss]");
    }

    @Test
    public void test_max_boundary_condition() throws IOException {
        skypeLogDocExtractor = SkypeLogDocExtractor.getInstance("src/test/java/resources/TestLog.txt",
                Long.MAX_VALUE, "[dd.mm.yyyy HH:mm:ss]");
        assertTrue(skypeLogDocExtractor.hasNext()); skypeLogDocExtractor.parseNext();
        assertTrue(!skypeLogDocExtractor.hasNext()); skypeLogDocExtractor.parseNext();
        assertTrue(!skypeLogDocExtractor.hasNext());
    }

    @Test
    public void test_min_boundary_condition() throws IOException {
        skypeLogDocExtractor = SkypeLogDocExtractor.getInstance("src/test/java/resources/TestLog.txt",
                0, "[dd.mm.yyyy HH:mm:ss]");
        for(int i = 0; i < 7; ++i) {
            assertTrue(skypeLogDocExtractor.hasNext());
            skypeLogDocExtractor.parseNext();
        }
        assertTrue(!skypeLogDocExtractor.hasNext());
    }

    @Test
    public void test_60_min_gaps() throws IOException {
        skypeLogDocExtractor = SkypeLogDocExtractor.getInstance("src/test/java/resources/TestLog.txt",
                60, "[dd.mm.yyyy HH:mm:ss]");
        for(int i = 0; i < 3; ++i) {
            assertTrue(skypeLogDocExtractor.hasNext());
            skypeLogDocExtractor.parseNext();
        }
        assertTrue(!skypeLogDocExtractor.hasNext());
    }
}
