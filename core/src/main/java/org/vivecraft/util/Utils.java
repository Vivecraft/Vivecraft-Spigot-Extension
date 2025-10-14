package org.vivecraft.util;

import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private final static Pattern VERSION_PATTERN = Pattern.compile("\\d{1,2}\\.\\d{1,2}(\\.\\d{1,2})?");

    /**
     * create a map from a list of strings, reimplements Map.of(), since that is not part of java 8
     *
     * @param args key value pairs of strings
     * @return map with the given strings
     */
    public static Map<String, String> MapOf(String... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Map entries need to be an even number of arguments");
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put(args[i], args[i + 1]);
        }
        return map;
    }

    /**
     * Collection.containsAll, but ignores null values
     */
    public static boolean containsAll(Collection<?> a, Collection<?> b) {
        for (Object o : a) {
            if (o != null && !b.contains(o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * extract the clients mc version from the version string
     *
     * @param viveVersion version String that the client sent
     * @return parsed MCVersion
     */
    public static MCVersion getMCVersion(String viveVersion) {
        Matcher m = VERSION_PATTERN.matcher(viveVersion);
        if (m.find()) {
            return MCVersion.parse(m.group(), false);
        } else {
            return MCVersion.INVALID;
        }
    }

    /**
     * pads an object with gold chat color for printing in chat
     *
     * @param o object to pad
     * @return the padded object string
     */
    public static String gold(Object o) {
        return padColor(o, ChatColor.GOLD);
    }

    /**
     * pads an object with red chat color for printing in chat
     *
     * @param o object to pad
     * @return the padded object string
     */
    public static String red(Object o) {
        return padColor(o, ChatColor.RED);
    }

    /**
     * pads an object with gren chat color for printing in chat
     *
     * @param o object to pad
     * @return the padded object string
     */
    public static String green(Object o) {
        return padColor(o, ChatColor.GREEN);
    }

    /**
     * pads an object with the given chat color for printing in chat
     *
     * @param o     object to pad
     * @param color color to pad with
     * @return the padded object string
     */
    public static String padColor(Object o, ChatColor color) {
        return color + o.toString() + ChatColor.RESET;
    }
}
