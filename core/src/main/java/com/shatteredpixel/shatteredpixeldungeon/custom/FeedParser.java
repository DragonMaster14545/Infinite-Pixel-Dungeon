package com.shatteredpixel.shatteredpixeldungeon.custom;

import com.badlogic.gdx.utils.XmlReader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FeedParser {

    public static boolean looksLikeFeed(String text) {
        if (text == null) return false;
        String t = text.trim();
        return t.startsWith("<") && (t.contains("<feed") || t.contains("<rss"));
    }

    public static ArrayList<AnnouncementStore.Entry> parse(String xml) {
        ArrayList<AnnouncementStore.Entry> list = new ArrayList<>();
        try {
            XmlReader.Element root = new XmlReader().parse(xml);
            if (root == null) return list;
            if (root.getName().equals("feed")) {
                for (XmlReader.Element e : root.getChildrenByName("entry")) {
                    add(list, text(e, "title"), body(e, "content", "summary"), atomTime(text(e, "updated"), text(e, "published")));
                }
            } else {
                XmlReader.Element channel = root.getChildByName("channel");
                if (channel != null) {
                    for (XmlReader.Element e : channel.getChildrenByName("item")) {
                        add(list, text(e, "title"), body(e, "description", "content:encoded"), rssTime(text(e, "pubDate")));
                    }
                }
            }
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
        return list;
    }

    private static void add(ArrayList<AnnouncementStore.Entry> list, String title, String body, long time) {
        AnnouncementStore.Entry e = new AnnouncementStore.Entry();
        e.title = title.isEmpty() ? "-" : title;
        e.body = body;
        e.time = time;
        list.add(e);
    }

    private static String text(XmlReader.Element e, String name) {
        XmlReader.Element c = e.getChildByName(name);
        if (c == null || c.getText() == null) return "";
        return plain(c.getText());
    }

    private static String body(XmlReader.Element e, String first, String second) {
        XmlReader.Element c = e.getChildByName(first);
        if (c == null || c.getText() == null || c.getText().trim().isEmpty()) c = e.getChildByName(second);
        if (c == null || c.getText() == null) return "";
        return plain(c.getText());
    }

    public static String plain(String html) {
        String s = html;
        s = s.replaceAll("(?i)<br\\s*/?>", "\n");
        s = s.replaceAll("(?i)</(p|div|h[1-6]|li|tr|blockquote)>", "\n");
        s = s.replaceAll("(?i)<li[^>]*>", "- ");
        s = s.replaceAll("(?s)<script.*?</script>", "");
        s = s.replaceAll("(?s)<style.*?</style>", "");
        s = s.replaceAll("<[^>]+>", "");
        s = decode(s);
        s = s.replaceAll("[ \\t\\u00A0]+\\n", "\n");
        s = s.replaceAll("\\n{3,}", "\n\n");
        return s.trim();
    }

    private static String decode(String s) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("&#(x?)([0-9a-fA-F]+);").matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String rep;
            try {
                int cp = Integer.parseInt(m.group(2), m.group(1).isEmpty() ? 10 : 16);
                rep = new String(Character.toChars(cp));
            } catch (RuntimeException ex) {
                rep = "";
            }
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        s = sb.toString();
        return s.replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&apos;", "'").replace("&hellip;", "...")
                .replace("&ndash;", "-").replace("&mdash;", "-").replace("&amp;", "&");
    }

    private static long atomTime(String updated, String published) {
        long t = iso(updated);
        return t != 0 ? t : iso(published);
    }

    private static long iso(String s) {
        if (s == null || s.length() < 19) return 0;
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            c.clear();
            c.set(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(5, 7)) - 1,
                    Integer.parseInt(s.substring(8, 10)), Integer.parseInt(s.substring(11, 13)),
                    Integer.parseInt(s.substring(14, 16)), Integer.parseInt(s.substring(17, 19)));
            long t = c.getTimeInMillis();
            String rest = s.substring(19);
            if (rest.startsWith(".")) {
                int i = 1;
                while (i < rest.length() && Character.isDigit(rest.charAt(i))) i++;
                String frac = (rest.substring(1, i) + "000").substring(0, 3);
                t += Integer.parseInt(frac);
                rest = rest.substring(i);
            }
            if (rest.length() >= 6 && (rest.charAt(0) == '+' || rest.charAt(0) == '-')) {
                int sign = rest.charAt(0) == '-' ? -1 : 1;
                int hh = Integer.parseInt(rest.substring(1, 3));
                int mm = Integer.parseInt(rest.substring(4, 6));
                t -= sign * (hh * 60L + mm) * 60000L;
            }
            return t;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private static long rssTime(String s) {
        try {
            Date d = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).parse(s);
            return d == null ? 0 : d.getTime();
        } catch (Exception e) {
            return 0;
        }
    }
}
