package com.shatteredpixel.shatteredpixeldungeon.custom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;

public class AnnouncementStore {

    public static final String FILE = "announcements.dat";
    private static final String TEXT = "text";
    private static final String TIME = "time";

    public static class Entry {
        public String title;
        public String body;
        public long time;
    }

    public static final int IDLE = 0, LOADING = 1, OK = 2, FAILED = 3;

    public static final String DEFAULT_URL = "https://infpixeldungeon.blogspot.com/feeds/posts/default?max-results=10";
    private static final String SEEN = "seen";
    private static final long CHECK_DELAY = 1000L * 60 * 60;

    private static boolean loaded = false;
    private static long seen = 0;
    private static long lastCheck = 0;
    private static String url = DEFAULT_URL;
    private static String text = "";
    private static long time = 0;

    public static volatile int state = IDLE;
    public static volatile int version = 0;

    private static void load() {
        if (loaded) return;
        loaded = true;
        try {
            Bundle b = FileUtils.bundleFromFile(FILE);
            seen = b.contains(SEEN) ? Long.parseLong(b.getString(SEEN)) : 0;
            text = b.contains(TEXT) ? b.getString(TEXT) : "";
            time = b.contains(TIME) ? Long.parseLong(b.getString(TIME)) : 0;
        } catch (IOException | RuntimeException e) {
        }
    }

    private static void save() {
        Bundle b = new Bundle();
        b.put(SEEN, String.valueOf(seen));
        b.put(TEXT, text);
        b.put(TIME, String.valueOf(time));
        try {
            FileUtils.bundleToFile(FILE, b);
        } catch (IOException e) {
            ShatteredPixelDungeon.reportException(e);
        }
    }

    public static synchronized String url() {
        load();
        return url;
    }

    private static long versionOf(String t) {
        if (t == null) return 0;
        if (FeedParser.looksLikeFeed(t)) {
            long best = 0;
            for (Entry e : FeedParser.parse(t)) best = Math.max(best, e.time);
            return best;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?m)^\\s*version\\s*[=:]\\s*(\\d+)\\s*$").matcher(t);
        try {
            return m.find() ? Long.parseLong(m.group(1)) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static synchronized long remoteVersion() {
        load();
        return versionOf(text);
    }

    public static synchronized boolean hasNew() {
        load();
        return versionOf(text) > seen;
    }

    public static synchronized void markSeen() {
        load();
        long v = versionOf(text);
        if (v > seen) {
            seen = v;
            save();
        }
    }

    public static void check() {
        load();
        long now = System.currentTimeMillis();
        if (state == LOADING || (lastCheck != 0 && now - lastCheck < CHECK_DELAY)) return;
        lastCheck = now;
        refresh();
    }

    public static synchronized String text() {
        load();
        return text;
    }

    public static synchronized long time() {
        load();
        return time;
    }

    public static ArrayList<Entry> entries() {
        String t = text();
        ArrayList<Entry> list = new ArrayList<>();
        if (t == null || t.trim().isEmpty()) return list;
        if (FeedParser.looksLikeFeed(t)) return FeedParser.parse(t);
        t = t.replace("\r", "").replaceFirst("(?m)^\\s*version\\s*[=:]\\s*\\d+\\s*$", "");
        for (String block : t.split("(?m)^---\\s*$")) {
            String s = block.trim();
            if (s.isEmpty()) continue;
            int nl = s.indexOf('\n');
            Entry e = new Entry();
            String head = nl < 0 ? s : s.substring(0, nl);
            e.title = head.replaceFirst("^#+\\s*", "").trim();
            e.body = nl < 0 ? "" : s.substring(nl + 1).trim();
            list.add(e);
        }
        return list;
    }

    public static void refresh() {
        final String u = url();
        if (u.isEmpty()) {
            state = FAILED;
            version++;
            return;
        }
        state = LOADING;
        String full = u + (u.contains("?") ? "&" : "?") + "t=" + System.currentTimeMillis();
        try {
            Net.HttpRequest req = new HttpRequestBuilder().newRequest()
                    .method(Net.HttpMethods.GET)
                    .url(full)
                    .timeout(10000)
                    .build();
            Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse response) {
                    int code = response.getStatus().getStatusCode();
                    String body = response.getResultAsString();
                    if (code >= 200 && code < 300 && body != null) {
                        synchronized (AnnouncementStore.class) {
                            text = body;
                            time = System.currentTimeMillis();
                            save();
                        }
                        state = OK;
                    } else {
                        state = FAILED;
                        lastCheck = 0;
                    }
                    version++;
                }

                @Override
                public void failed(Throwable t) {
                    state = FAILED;
                    lastCheck = 0;
                    version++;
                }

                @Override
                public void cancelled() {
                    state = FAILED;
                    version++;
                }
            });
        } catch (Throwable t) {
            state = FAILED;
            version++;
        }
    }
}
