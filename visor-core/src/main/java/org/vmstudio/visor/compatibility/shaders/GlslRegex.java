package org.vmstudio.visor.compatibility.shaders;

import java.util.regex.Pattern;

final class GlslRegex {
    private GlslRegex() {}

    static Pattern compile(String spaced) {
        return Pattern.compile(padWhitespace(expandNumbers(expandSwizzles(spaced))), Pattern.CASE_INSENSITIVE);
    }

    /** {@code \.<swizzle>} -> {@code \.} followed by each component's alias character class */
    private static String expandSwizzles(String s) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == '\\' && i + 1 < s.length() && s.charAt(i + 1) == '.') {
                int j = i + 2;
                while (j < s.length() && j - (i + 2) < 4 && "xyzw".indexOf(s.charAt(j)) >= 0) {
                    j++;
                }
                boolean followedByWord = j < s.length() && isWord(s.charAt(j));
                if (j > i + 2 && !followedByWord) {
                    out.append("\\.");
                    for (int k = i + 2; k < j; k++) {
                        out.append(alias(s.charAt(k)));
                    }
                    i = j;
                    continue;
                }
            }
            out.append(s.charAt(i++));
        }
        return out.toString();
    }

    /** {@code <digits>\.0} -> {@code (?:<digits>|<digits>\.|<digits>\.0)} */
    private static String expandNumbers(String s) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            if (Character.isDigit(s.charAt(i))) {
                int j = i;
                while (j < s.length() && Character.isDigit(s.charAt(j))) {
                    j++;
                }
                if (j + 2 < s.length() && s.charAt(j) == '\\' && s.charAt(j + 1) == '.' && s.charAt(j + 2) == '0') {
                    String n = s.substring(i, j);
                    out.append("(?:").append(n).append('|').append(n).append("\\.|").append(n).append("\\.0)");
                    i = j + 3;
                    continue;
                }
            }
            out.append(s.charAt(i++));
        }
        return out.toString();
    }

    /** Inserts {@code \s*} before each token (never at index 0) and turns literal spaces into {@code \s*} */
    private static String padWhitespace(String s) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int len = i == 0 ? 0 : tokenLength(s, i);
            if (len > 0) {
                out.append("\\s*").append(s, i, i + len);
                i += len;
            } else {
                out.append(s.charAt(i++));
            }
        }
        return out.toString().replace(" ", "\\s*");
    }

    private static int tokenLength(String s, int i) {
        char c = s.charAt(i);
        char prev = i > 0 ? s.charAt(i - 1) : '\0';
        if (c == '\\' && i + 1 < s.length()) {
            char next = s.charAt(i + 1);
            if ("[]().+*?".indexOf(next) >= 0) {
                return 2; // escaped literal: \[ \] \( \) \. \+ \* \?
            }
            if (Character.isDigit(next)) {
                int j = i + 1;
                while (j < s.length() && Character.isDigit(s.charAt(j))) {
                    j++;
                }
                return j - i; // back-reference \1, \2, ...
            }
            return 0; // \w, \s, ... are not tokens
        }
        if (c == '[') {
            return prev != ']' ? 1 : 0; // class opener, but not one closing right after another
        }
        if (c == '(') {
            return prev != '(' && prev != '?' && prev != '\\' ? 1 : 0;
        }
        if (c == ':') {
            return prev != '?' ? 1 : 0; // a real ':' token, not the ':' of "(?:"
        }
        if ("-;=/,".indexOf(c) >= 0) {
            return 1;
        }
        if (isWord(c)) {
            if (prev == '\\' || prev == '|' || isWord(prev)) {
                return 0;
            }
            if ((prev == '[' || prev == '(') && (i < 2 || s.charAt(i - 2) != '\\')) {
                return 0; // first atom inside an unescaped group/class
            }
            if (prev == ':' && i >= 2 && s.charAt(i - 2) == '?') {
                return 0; // first atom inside "(?:"
            }
            int j = i;
            while (j < s.length() && isWord(s.charAt(j))) {
                j++;
            }
            return j - i;
        }
        return 0;
    }

    private static String alias(char component) {
        return switch (component) {
            case 'x' -> "[xrs]";
            case 'y' -> "[ygt]";
            case 'z' -> "[zbp]";
            case 'w' -> "[waq]";
            default -> String.valueOf(component);
        };
    }

    // hate this
    private static boolean isWord(char c) {
        return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }
}
