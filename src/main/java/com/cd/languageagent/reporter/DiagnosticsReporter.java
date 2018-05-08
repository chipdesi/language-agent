package com.cd.languageagent.reporter;

/*
Simple reporter that logs diagnostics to standard out
 */
public class DiagnosticsReporter {

    private static DiagnosticsReporter instance = new DiagnosticsReporter();

    private static final String LOG_FORMAT = "%s => Request: [%s], String count: [%d], time (ms): [%d], memory: [%d]";

    private DiagnosticsReporter() {}

    public static DiagnosticsReporter getInstance() {
        return instance;
    }

    public void report(String id, String requestUrl, long stringCount, long timeMs, long memory) {
        System.out.println(String.format(LOG_FORMAT, id, requestUrl, stringCount, timeMs, memory));
    }
}
