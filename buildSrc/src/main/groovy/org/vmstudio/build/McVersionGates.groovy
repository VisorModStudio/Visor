package org.vmstudio.build

import java.util.regex.MatchResult
import java.util.regex.Pattern


// Stonecutter 0.9.8 gate rendering (SlashStarCommentStrategy with flatten, StarRemoveCommentStrategy) for range files
// outside of src, which Stonecutter never processes: the copies parked under mcversion/ and their per-node builds
class McVersionGates {
    private static final String SUPERSCRIPTS = "⁰¹²³⁴⁵⁶⁷⁸⁹"
    private static final Pattern MARKER = ~/\/\/\?[^\n]*/
    static final Pattern HASH_MARKER = ~/(?m)^#\?[^\n]*/
    private static final Pattern TERM = ~/^\s*(>=|<=|!=|==|>|<|=)?\s*([\d.]+)\s*$/

    static boolean hasMarkers(String text) {
        text.contains("//?")
    }

    static String render(String text, String version) {
        def found = chains(text)
        List<MatchResult> marks = found.marks
        def out = new StringBuilder()
        int pos = 0
        for (List<Integer> chain : found.chains) {
            out.append(text, pos, marks[chain[0]].start())
            boolean taken = false
            for (int i = 0; i < chain.size() - 1; i++) {
                MatchResult head = marks[chain[i]]
                MatchResult next = marks[chain[i + 1]]
                def marker = parseMarker(head.group())
                boolean active = !taken && (marker.kind == "else" || evaluate(marker.cond, version))
                taken = taken || active
                String scope = text.substring(head.end(), next.start())
                int k = leadingWhitespace(scope)
                String body = scope.substring(k)
                out.append(head.group())
                if (body.length() >= 4 && body.startsWith("/*") && body.endsWith("*/")) {
                    // an already commented branch keeps its '/*' where it is
                    String inner = render(bump(body.substring(2, body.length() - 2), -1), version)
                    out.append(scope, 0, k).append(active ? inner : "/*" + bump(inner, 1) + "*/")
                } else {
                    String inner = render(scope, version)
                    out.append(active || inner.trim().isEmpty() ? inner : comment(inner))
                }
            }
            MatchResult end = marks[chain[chain.size() - 1]]
            out.append(end.group())
            pos = end.end()
        }
        out.append(text, pos, text.length())
        out.toString()
    }

    // Stonecutter renames commented branches too: each branch goes back to the names of the newest node taking it
    static String nativeForm(String text, String version, List<String> nodes, Pattern marker = MARKER) {
        def found = chains(text, marker)
        List<MatchResult> marks = found.marks
        def out = new StringBuilder()
        int pos = 0
        for (List<Integer> chain : found.chains) {
            out.append(renamed(text.substring(pos, marks[chain[0]].start()), version))
            def markers = chain.collect { parseMarker(marks[it].group()) }
            for (int i = 0; i < chain.size() - 1; i++) {
                MatchResult head = marks[chain[i]]
                String scope = text.substring(head.end(), marks[chain[i + 1]].start())
                List<String> mine = nodes.findAll { pick(markers, it) == i }
                String own = version in mine ? version : mine.max { a, b -> McVersionRange.compare(a, b) }
                out.append(head.group()).append(nativeForm(scope, own, mine, marker))
            }
            MatchResult end = marks[chain[chain.size() - 1]]
            out.append(end.group())
            pos = end.end()
        }
        out.append(renamed(text.substring(pos), version))
        out.toString()
    }

    private static String renamed(String text, String version) {
        version == null ? text : McVersionRenames.apply(text, version)
    }

    // the markers of every chain whose branch choice is the same on all the given versions
    static List<String> constantChains(String text, Collection<String> versions) {
        def problems = []
        def found = chains(text)
        List<MatchResult> marks = found.marks
        for (List<Integer> chain : found.chains) {
            def picks = versions.collect { v -> pick(chain.collect { parseMarker(marks[it].group()) }, v) } as Set
            if (picks.size() <= 1) {
                problems << marks[chain[0]].group().trim()
            }
            for (int i = 0; i < chain.size() - 1; i++) {
                String scope = text.substring(marks[chain[i]].end(), marks[chain[i + 1]].start())
                int k = leadingWhitespace(scope)
                String body = scope.substring(k)
                String inner = body.length() >= 4 && body.startsWith("/*") && body.endsWith("*/")
                        ? bump(body.substring(2, body.length() - 2), -1) : scope
                problems.addAll(constantChains(inner, versions))
            }
        }
        problems
    }

    private static int pick(List<Map> markers, String version) {
        for (int i = 0; i < markers.size() - 1; i++) {
            if (markers[i].kind == "else" || evaluate(markers[i].cond as String, version)) {
                return i
            }
        }
        -1
    }

    static boolean evaluate(String cond, String version) {
        cond.split("&&").every { String term ->
            def m = TERM.matcher(term)
            if (!m.matches()) {
                throw new IllegalArgumentException("unsupported Stonecutter condition '${cond}'")
            }
            int c = McVersionRange.compare(version, m.group(2))
            switch (m.group(1) ?: "=") {
                case ">=": return c >= 0
                case "<=": return c <= 0
                case ">": return c > 0
                case "<": return c < 0
                case "!=": return c != 0
                default: return c == 0
            }
        }
    }

    private static Map chains(String text, Pattern marker = MARKER) {
        List<MatchResult> marks = []
        def m = marker.matcher(text)
        while (m.find()) {
            marks << m.toMatchResult()
        }
        List<List<Integer>> chains = []
        int i = 0
        while (i < marks.size()) {
            if (parseMarker(marks[i].group()).kind != "if") {
                throw new IllegalArgumentException("unbalanced Stonecutter marker '${marks[i].group().trim()}'")
            }
            List<Integer> chain = [i]
            int depth = 0
            int j = i + 1
            while (true) {
                if (j >= marks.size()) {
                    throw new IllegalArgumentException("unclosed Stonecutter marker '${marks[i].group().trim()}'")
                }
                String kind = parseMarker(marks[j].group()).kind
                if (kind == "if") {
                    depth++
                } else if (kind == "end") {
                    if (depth == 0) {
                        chain << j
                        break
                    }
                    depth--
                } else if (depth == 0) {
                    chain << j
                }
                j++
            }
            chains << chain
            i = chain[chain.size() - 1] + 1
        }
        [chains: chains, marks: marks]
    }

    private static Map parseMarker(String marker) {
        String body = marker.substring(marker.startsWith("#") ? 2 : 3).trim()
        if (body == "}") {
            return [kind: "end"]
        }
        if (body == "} else {") {
            return [kind: "else"]
        }
        def m = body =~ /^}\s*(?:elif|else if)\s+(.*?)\s*\{$/
        if (m.matches()) {
            return [kind: "elif", cond: m.group(1)]
        }
        m = body =~ /^if\s+(.*?)\s*\{$/
        if (m.matches()) {
            return [kind: "if", cond: m.group(1)]
        }
        throw new IllegalArgumentException("unsupported Stonecutter marker '${marker.trim()}'")
    }

    private static int leadingWhitespace(String s) {
        int k = 0
        while (k < s.length() && " \t\r\n".indexOf((int) s.charAt(k)) >= 0) {
            k++
        }
        k
    }

    private static String comment(String scope) {
        String s = bump(scope, 1)
        int k = leadingWhitespace(s)
        s.substring(0, k) + "/*" + s.substring(k) + "*/"
    }

    // nested block comments one level deeper (delta 1) or shallower (-1): /* */ -> /^ ^/ -> /^¹ ¹^/ ...
    private static String bump(String s, int delta) {
        def out = new StringBuilder()
        int last = 0
        places(s).each { int[] p ->
            int marker = p[0], from = p[1], to = p[2]
            String digits = s.substring(from, to)
            int level = s.charAt(marker) == ('*' as char) ? 0 : 1 + (digits ? superscriptToInt(digits) : 0)
            int next = Math.max(level + delta, 0)
            boolean opener = marker < from
            String suffix = next <= 1 ? "" : intToSuperscript(next - 1)
            String token = next == 0 ? "*" : (opener ? "^" + suffix : suffix + "^")
            out.append(s, last, Math.min(marker, from)).append(token)
            last = Math.max(marker + 1, to)
        }
        out.append(s, last, s.length())
        out.toString()
    }

    // [index of '*' or '^', start, end of its superscript digits]: openers '/*' '/^n', closers '*/' 'n^/'
    private static List<int[]> places(String s) {
        List<int[]> out = []
        int i = 0
        int n = s.length()
        while (i < n) {
            char c = s.charAt(i)
            if (c == ('/' as char) && i + 1 < n && (s.charAt(i + 1) == ('*' as char) || s.charAt(i + 1) == ('^' as char))) {
                int j = i + 2
                if (s.charAt(i + 1) == ('^' as char)) {
                    while (j < n && SUPERSCRIPTS.indexOf((int) s.charAt(j)) >= 0) {
                        j++
                    }
                }
                out << ([i + 1, i + 2, j] as int[])
                i = j
                continue
            }
            if (c == ('*' as char) && i + 1 < n && s.charAt(i + 1) == ('/' as char)) {
                out << ([i, i, i] as int[])
                i += 2
                continue
            }
            if (c == ('^' as char) || SUPERSCRIPTS.indexOf((int) c) >= 0) {
                int j = i
                while (j < n && SUPERSCRIPTS.indexOf((int) s.charAt(j)) >= 0) {
                    j++
                }
                if (j + 1 < n && s.charAt(j) == ('^' as char) && s.charAt(j + 1) == ('/' as char)) {
                    out << ([j, i, j] as int[])
                    i = j + 2
                    continue
                }
            }
            i++
        }
        out
    }

    private static int superscriptToInt(String digits) {
        digits.inject(0) { int acc, String d -> acc * 10 + SUPERSCRIPTS.indexOf(d) } as int
    }

    private static String intToSuperscript(int value) {
        value.toString().collect { SUPERSCRIPTS.charAt(Integer.parseInt(it)) }.join()
    }
}
