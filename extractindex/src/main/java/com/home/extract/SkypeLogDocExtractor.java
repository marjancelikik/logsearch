package com.home.extract;

import com.home.common.LogDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class extracts documents from a text file by using the time stamp of each currentLine.
 * Lines close to each other w.r.t. to the time stamp form a single document.
 */
public class SkypeLogDocExtractor implements LogDocExtractor {

    private static final Log logger = LogFactory.getLog(SkypeLogDocExtractor.class);

    private static final String[] DEFAULT_DATETIME_PATTERNS = { "[dd.mm.yyyy HH:mm:ss]",
                                                                "[dd/mm/yyyy HH:mm:ss]",
                                                                "dd.mm.yyyy HH:mm:ss",
                                                                "dd.mm.yyyy HH:mm:ss" };

    private long maxMillisecs = 60;

    private final List<Pattern> dateTimeFormatters = new ArrayList<>();

    private BufferedReader fileReader;

    private DateTime lastDateTime = null;

    private String currentLine = null;

    private LogDocument logDocument = new LogDocument(0);

    private int docCounter = 0;

    /**
     * Create a log extractor object for list of provided date-time patterns according to joda.time.DateTime formats.
     *
     * @param filename Location of the log filename on disk.
     * @param maxMinutes Maximum minutes allowed between two consecutive lines in the log.
     * @param patterns A list of provided patterns. The first valid pattern will be used to parse the date and time.
     * @return A newly created SkypeLogDocExtractor object able to parse the provided patterns.
     * @throws IllegalArgumentException if none of the array of patterns is valid.
     * @throws IOException if the file cannot be open for reading.
     */
    public static SkypeLogDocExtractor getInstance(final String filename, final long maxMinutes,
                                                   final String... patterns) throws IllegalArgumentException, IOException {
        if (filename == null || patterns == null) {
            throw new NullPointerException("Null string passed!");
        }
        SkypeLogDocExtractor instance = new SkypeLogDocExtractor();
        instance.createDateTimeFormatters(patterns);
        instance.setMaxGapTime(maxMinutes);
        instance.openLogFileForReading(filename);
        instance.parseNext();
        return instance;
    }

    private void setMaxGapTime(final long maxMinutes) {
        if (maxMinutes >= Long.MAX_VALUE / (60 * 1000)) {
            maxMillisecs = Long.MAX_VALUE;
        } else {
            maxMillisecs = maxMinutes * 60 * 1000;
        }
        assert maxMinutes >= 0;
    }

    private void createDateTimeFormatters(final String... patterns) {
        for(String pattern : patterns) {
            try {
                dateTimeFormatters.add(new Pattern(DateTimeFormat.forPattern(pattern), pattern.length()));
            }
            catch(final IllegalArgumentException e) {
                logger.error("Invalid date time pattern specified: " + pattern);
            }
        }
        if (dateTimeFormatters.size() == 0) {
            throw new IllegalArgumentException("None of the patterns was valid!");
        }
    }

    private void openLogFileForReading(final String filename) throws IOException {
        fileReader = new BufferedReader(new FileReader(filename));
    }

    /**
     * Create a log extractor object for list of provided date-time patterns according to joda.time.DateTime formats.
     *
     * @param filename Location of the log filename on disk.
     * @param maxMinutes Maximum minutes allowed between two consecutive lines in the log.
     * @return A newly created SkypeLogDocExtractor object able to parse the provided patterns.
     * @throws IllegalArgumentException if none of the array of patterns is valid.
     * @throws IOException if the file cannot be open for reading.
     */
    public static SkypeLogDocExtractor getInstance(final String filename, final long maxMinutes)
            throws IllegalArgumentException, IOException {
        return getInstance(filename, maxMinutes, DEFAULT_DATETIME_PATTERNS);
    }

    /**
     * Parses the next document in the log based on time stamps.
     *
     * @return The newly parsed document or empty string if there are no more lines to parse.
     */
    @Override
    public LogDocument parseNext() {
        clearCurrentDoc();
        addCurrentLineToCurrentDoc();
        addFurtherLinesToCurrentDoc();
        return logDocument;
    }

    private void clearCurrentDoc() {
        logDocument = new LogDocument(docCounter++);
    }

    private void addCurrentLineToCurrentDoc() {
        if (currentLine != null) {
            logDocument.addLine(currentLine);
        }
    }

    private void addFurtherLinesToCurrentDoc() {
        try {
            while ((currentLine = fileReader.readLine()) != null) {
                try {
                    DateTime currentDateTime = parseDateTimeFromLine();
                    boolean timeGapAboveMax = timeGapAboveMax(currentDateTime);
                    lastDateTime = currentDateTime;
                    if (timeGapAboveMax) {
                        break;
                    }
                    addCurrentLineToCurrentDoc();
                } catch (IllegalArgumentException e) {
                    logger.error("Error parsing current line '" + currentLine + "': " + e);
                }
            }
        } catch (IOException e) {
            logger.error("Error while trying to read from file!'");
        }
    }

    private boolean timeGapAboveMax(final DateTime currentDateTime) {
        return (lastDateTime != null) && (currentDateTime.getMillis() - lastDateTime.getMillis() > maxMillisecs);
    }

    private DateTime parseDateTimeFromLine() throws IllegalArgumentException {
        return parseDateTime();
    }

    private DateTime parseDateTime() {
        for (Pattern dateTimePattern : dateTimeFormatters) {
            // Try all date time formatters
            if (currentLine.length() >= dateTimePattern.getPatternLength()) {
                String DateTimeStr = currentLine.substring(0, dateTimePattern.getPatternLength());
                currentLine = currentLine.substring(dateTimePattern.getPatternLength(), currentLine.length());
                return DateTime.parse(DateTimeStr, dateTimePattern.getDateTimeFormatter());
            }
        }
        throw new IllegalArgumentException("Line does not contain valid date time!");
    }

    /**
     * Returns a copy of the current document (if any).
     *
     * @rerurn Currently parsed document or empty string if there are no such documents.
     */
    @Override
    public LogDocument current() {
        return logDocument;
    }

    /**
     * Indicates whether there are more documents to parse.
     *
     * @return An empty document if there are no more documents to parse.
     */
    @Override
    public boolean hasNext() {
        return !logDocument.getText().isEmpty();
    }

    private SkypeLogDocExtractor() {
    }

    static {
        // Configure simple logging.
        BasicConfigurator.configure();
    }

    /** Data class used for storing the pattern together with its length. */
    static class Pattern {
        private final DateTimeFormatter dateTimeFormatter;
        private final int patternLength;

        Pattern(DateTimeFormatter dateTimeFormatter, int patternLength) {
            this.dateTimeFormatter = dateTimeFormatter;
            this.patternLength     = patternLength;
        }

        DateTimeFormatter getDateTimeFormatter() {
            return this.dateTimeFormatter;
        }

        int getPatternLength() {
            return patternLength;
        }
    }
}
