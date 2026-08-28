package org.vmstudio.visor.compatibility.shaders;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class GlslPattern {
    private static final String COMPONENTS = "xyzw";
    private static final String[] ALIASES = {"xrs", "ygt", "zbp", "waq"};

    private final StringBuilder regex = new StringBuilder();
    private boolean prevWasIdent;
    private boolean glueNext;

    GlslPattern glsl(String source) {
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (identStart(c)) {
                int to = i;
                while (to < source.length() && identPart(source.charAt(to))) {
                    to++;
                }
                gap(true);
                regex.append(Pattern.quote(source.substring(i, to)));
                prevWasIdent = true;
                i = to;
                continue;
            }

            if (Character.isDigit(c)) {
                int to = i;
                while (to < source.length() && (Character.isDigit(source.charAt(to)) || source.charAt(to) == '.')) {
                    to++;
                }
                gap(false);
                regex.append(floatSpellings(source.substring(i, to)));
                prevWasIdent = true;
                i = to;
                continue;
            }

            if (c == '.') {
                int to = i + 1;
                while (to < source.length() && to - i <= 4 && isComponent(source.charAt(to))) {
                    to++;
                }
                boolean bareSwizzle = to > i + 1 && (to >= source.length() || !identPart(source.charAt(to)));
                if (bareSwizzle) {
                    gap(false);
                    regex.append("\\.");
                    for (int k = i + 1; k < to; k++) {
                        regex.append(aliasesOf(source.charAt(k)));
                    }
                    prevWasIdent = true;
                    i = to;
                    continue;
                }
            }

            gap(false);
            regex.append(Pattern.quote(String.valueOf(c)));
            prevWasIdent = false;
            i++;
        }
        return this;
    }

    GlslPattern capture(String name) {
        return token("(?<" + name + ">\\w+)");
    }

    GlslPattern same(String name) {
        return token("\\k<" + name + ">");
    }

    GlslPattern anyName() {
        return token("\\w+");
    }

    GlslPattern either(String... spellings) {
        StringBuilder alt = new StringBuilder("(?:");
        for (int i = 0; i < spellings.length; i++) {
            if (i > 0) {
                alt.append('|');
            }
            alt.append(Pattern.quote(spellings[i]));
        }
        return token(alt.append(')').toString());
    }

    GlslPattern capturing(String name, Consumer<GlslPattern> body) {
        gap(false);
        regex.append("(?<").append(name).append('>');
        glueNext = true;
        boolean outerWasIdent = prevWasIdent;
        prevWasIdent = false;
        body.accept(this);
        regex.append(')');
        glueNext = false;
        prevWasIdent = true;
        return this;
    }

    GlslPattern optional(Consumer<GlslPattern> body) {
        gap(false);
        regex.append("(?:");
        boolean outerWasIdent = prevWasIdent;
        prevWasIdent = false;
        body.accept(this);
        regex.append(")?");
        prevWasIdent = outerWasIdent;
        return this;
    }

    @SafeVarargs
    final GlslPattern anyOf(Consumer<GlslPattern>... branches) {
        gap(false);
        regex.append("(?:");
        boolean outerWasIdent = prevWasIdent;
        for (int i = 0; i < branches.length; i++) {
            if (i > 0) {
                regex.append('|');
            }
            prevWasIdent = false;
            branches[i].accept(this);
        }
        regex.append(')');
        prevWasIdent = outerWasIdent;
        return this;
    }

    GlslPattern space() {
        return loose("\\s*");
    }

    GlslPattern anything() {
        return loose("[\\s\\S]*?");
    }

    GlslPattern raw(String fragment) {
        return loose(fragment);
    }

    GlslPattern tight() {
        glueNext = true;
        return this;
    }

    Pattern compile() {
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
    }

    private GlslPattern token(String fragment) {
        gap(true);
        regex.append(fragment);
        prevWasIdent = true;
        return this;
    }

    private GlslPattern loose(String fragment) {
        regex.append(fragment);
        prevWasIdent = false;
        return this;
    }

    private void gap(boolean nextIsIdent) {
        if (glueNext) {
            glueNext = false;
            return;
        }
        if (regex.isEmpty() || atGroupStart()) {
            return;
        }
        regex.append(prevWasIdent && nextIsIdent ? "\\s+" : "\\s*");
    }

    private boolean atGroupStart() {
        char last = regex.charAt(regex.length() - 1);
        return last == '|' || last == '(' || last == ':';
    }

    private static String floatSpellings(String literal) {
        if (!literal.contains(".")) {
            return Pattern.quote(literal);
        }
        String digits = literal.endsWith(".0") ? literal.substring(0, literal.length() - 2)
                : literal.substring(0, literal.length() - 1);
        if (digits.isEmpty() || digits.contains(".")) {
            return Pattern.quote(literal);
        }
        String q = Pattern.quote(digits);
        return "(?:" + q + "\\.0|" + q + "\\.|" + q + ")";
    }

    private static String aliasesOf(char component) {
        int i = COMPONENTS.indexOf(component);
        return i < 0 ? Pattern.quote(String.valueOf(component)) : "[" + ALIASES[i] + "]";
    }

    private static boolean isComponent(char c) {
        return COMPONENTS.indexOf(c) >= 0;
    }

    private static boolean identStart(char c) {
        return c == '_' || Character.isLetter(c);
    }

    private static boolean identPart(char c) {
        return c == '_' || Character.isLetterOrDigit(c);
    }
}