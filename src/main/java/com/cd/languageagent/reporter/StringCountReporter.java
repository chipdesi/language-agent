package com.cd.languageagent.reporter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class StringCountReporter {

    // maintains a mapping of thread IDs to a non-blocking queue
    private Map<Long, ConcurrentLinkedQueue<Long>> timestampMap = new ConcurrentHashMap<>();
    private Thread fileReaderThread;
    private AtomicBoolean stopped = new AtomicBoolean(false);
    private AtomicBoolean started = new AtomicBoolean(false);

    private static StringCountReporter instance = new StringCountReporter();

    private static final String STRING_LOG_FORMAT = "%s => String object count: %d";

    private StringCountReporter() {
        fileReaderThread = new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream("string-log.bin");
                byte[] b = new byte[16];
                while (!stopped.get()) {
                    int bytesRead = fis.read(b);
                    if (bytesRead != -1) {
                        ByteBuffer bb = ByteBuffer.wrap(b);
                        long timestamp = bb.getLong();
                        long id = bb.getLong();
                        getInstance().add(id, timestamp);
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException iex) {
                            iex.printStackTrace();
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static StringCountReporter getInstance() {
        return instance;
    }

    public void init() {
        if (stopped.get()) {
            throw new IllegalArgumentException("StringCountReporter can not be re-initialized once started.");
        } else if (!started.get()) {
            started.set(true);
            fileReaderThread.start();
        }
    }

    public void stop() {
        stopped.set(true);
        try {
            fileReaderThread.join();
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
    }

    public void add(long threadId, long timestamp) {
        timestampMap.putIfAbsent(threadId, new ConcurrentLinkedQueue<>());
        timestampMap.get(threadId).add(timestamp);
    }

    public void report(String id, long threadId, long startTimestamp, long stopTimestamp) {
        ConcurrentLinkedQueue<Long> queue = timestampMap.get(threadId);
        int stringCount = 0;
        if (queue != null) {
            while (!queue.isEmpty() && queue.peek() <= stopTimestamp) {
                long timestamp = queue.poll();
                if (timestamp >= startTimestamp) {
                    stringCount++;
                }
            }
            System.out.println(String.format(STRING_LOG_FORMAT, id, stringCount));
        }
    }
}
