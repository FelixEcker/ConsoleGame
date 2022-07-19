package de.feckert.congame.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Logger {
    protected String name;
    protected PrintStream printStream;
    protected FileWriter logFileWriter;
    protected PrintWriter logWriter;

    private Logger(String name) {
        this.name = name;
        this.printStream = System.out;
        this.logWriter = new PrintWriter(new NullWriter());
    }

    /**
     * Writes a message of type Information.
     * @param msg The message
     * */
    public void info(String msg) {
        printStream.printf("%s [%s]:[INFO] %s\n",getDateTimeString(), name, msg);
        logWriter.printf("%s [%s]:[INFO] %s\n", getDateTimeString(), name, msg);
        Logger.UNIVERSAL_LOGFILE_WRITER.printf("%s [%s]:[INFO] %s\n", getDateTimeString(), name, msg);
    }

    /**
     * Writes a message of type Information with formatting.
     * @param msg The message
     * @param objects Objects for formatting
     * */
    public void infof(String msg, Object... objects) {
        printStream.printf("%s [%s]:[INFO] %s", getDateTimeString(), name, String.format(msg, objects));
        logWriter.printf("%s [%s]:[INFO] %s", getDateTimeString(), name, String.format(msg, objects));
        Logger.UNIVERSAL_LOGFILE_WRITER.printf("%s [%s]:[INFO] %s", getDateTimeString(), name, String.format(msg, objects));
    }

    /**
     * Writes a message of type Warning.
     * @param msg The message
     * */
    public void warn(String msg) {
        printStream.printf("%s [%s]:[WARN] %s\n", getDateTimeString(), name, msg);
        logWriter.printf("%s [%s]:[WARN] %s\n", getDateTimeString(), name, msg);
        Logger.UNIVERSAL_LOGFILE_WRITER.printf("%s [%s]:[WARN] %s\n", getDateTimeString(), name, msg);
    }

    /**
     * Writes a message of type Warning with formatting.
     * @param msg The message
     * @param objects Objects for formatting
     * */
    public void warnf(String msg, Object... objects) {
        printStream.printf("%s [%s]:[WARN] %s", getDateTimeString(), name, String.format(msg, objects));
        logWriter.printf("%s [%s]:[WARN] %s", getDateTimeString(), name, String.format(msg, objects));
        Logger.UNIVERSAL_LOGFILE_WRITER.printf("%s [%s]:[WARN] %s", getDateTimeString(), name, String.format(msg, objects));
    }

    /**
     * Writes a message of type Error.
     * @param msg The message
     * */
    public void err(String msg) {
        printStream.printf("%s [%s]:[ERROR] %s\n", getDateTimeString(), name, msg);
        logWriter.printf("%s [%s]:[ERROR] %s\n", getDateTimeString(), name, msg);
        Logger.UNIVERSAL_LOGFILE_WRITER.printf("%s [%s]:[ERROR] %s\n", getDateTimeString(), name, msg);
    }

    /**
     * Writes a message of type Error with formatting.
     * @param msg The message
     * @param objects Objects for formatting
     * */
    public void errf(String msg, Object... objects) {
        printStream.printf("%s [%s]:[ERROR] %s", getDateTimeString(), name, String.format(msg, objects));
        logWriter.printf("%s [%s]:[ERROR] %s", getDateTimeString(), name, String.format(msg, objects));
        Logger.UNIVERSAL_LOGFILE_WRITER.printf("%s [%s]:[ERROR] %s", getDateTimeString(), name, String.format(msg, objects));
    }

    /**
     * Sets the Loggers print-stream
     *
     * @param stream The New PrintStream
     */
    public void setPrintStream(PrintStream stream) {
        this.printStream = stream;
    }

    /**
     * Sets the file the log should be saved to
     * @param file File to save to
     * */
    public void setLogFile(File file) {
        try {
            this.logFileWriter = new FileWriter(file);
            this.logWriter = new PrintWriter(logFileWriter, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Used as standard log-file writer
    private static class NullWriter extends Writer {
        @Override public void write(char[] cbuf, int off, int len) throws IOException { }
        @Override public void flush() throws IOException { }
        @Override public void close() throws IOException { }
    }

    ///////////////////////////////////

    public static final ArrayList<String> TAKEN_NAMES = new ArrayList<>();
    public static final Logger STANDARD_LOGGER;
    public static PrintWriter UNIVERSAL_LOGFILE_WRITER = new PrintWriter(new NullWriter());

    static {
        STANDARD_LOGGER = new Logger("STD_LOG");
        TAKEN_NAMES.add("STD_LOG");
    }

    /**
     * Creates a new Logger with a unique name.
     * @param name Name for the logger
     * @return Newly created Logger
     * */
    public static Logger create(String name) {
        if (!TAKEN_NAMES.contains(name)) {
            TAKEN_NAMES.add(name);
            return new Logger(name);
        } else {
            System.err.printf("LOGGER %s COULD NOT BE CREATED; LOGGER NAME ALREADY PRESENT!\n", name);
            return STANDARD_LOGGER;
        }
    }

    /**
     * Sets up the LogManager
     * */
    public static void setup() {
        Runtime.getRuntime().addShutdownHook(new Logger.ShutdownHook());
    }

    /**
     * Resets the LogManager
     * */
    public static void reset() {
        TAKEN_NAMES.clear();
        TAKEN_NAMES.add("STD_LOG");
    }

    public static String getDateTimeString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static class ShutdownHook extends Thread {
        @Override
        public void run() {
            UNIVERSAL_LOGFILE_WRITER.close();
        }
    }
}