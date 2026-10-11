package minijava.web;

import java.util.List;
import java.util.Map;

/**
 * Lightweight, dependency-free JSON builder and serializer for the embedded web visualizer.
 */
public class Json {

    public static String escape(String text) {
        if (text == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String string(String key, String value) {
        return "\"" + escape(key) + "\":\"" + escape(value) + "\"";
    }

    public static String number(String key, Number value) {
        return "\"" + escape(key) + "\":" + value;
    }

    public static String bool(String key, boolean value) {
        return "\"" + escape(key) + "\":" + value;
    }

    public static String stringList(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(escape(list.get(i))).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
