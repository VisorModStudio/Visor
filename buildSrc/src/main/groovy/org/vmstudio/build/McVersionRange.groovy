package org.vmstudio.build

import java.util.regex.Pattern


class McVersionRange {
    static final String HEADER = "// #!MC-VERSION::"
    private static final Pattern NAME = ~/^\[?\s*(\d+(?:\.\d+)+)(?:-(\d+(?:\.\d+)+)|(\+))?\s*]?$/

    final String from
    final String to

    private McVersionRange(String from, String to) {
        this.from = from
        this.to = to
    }

    static McVersionRange parse(String text) {
        def m = NAME.matcher(text.trim())
        if (!m.matches()) {
            throw new IllegalArgumentException("'${text.trim()}' is not a version range: expected <from>-<to> or <from>+")
        }
        new McVersionRange(m.group(1), m.group(3) ? null : (m.group(2) ?: m.group(1)))
    }

    static McVersionRange fromHeader(File file) {
        file.withReader("UTF-8") { reader ->
            for (int i = 0; i < 20; i++) {
                String line = reader.readLine()
                if (line == null) {
                    break
                }
                String s = line.trim()
                if (s.startsWith(HEADER)) {
                    return parse(s.substring(HEADER.length()))
                }
                if (s.startsWith("package ")) {
                    break
                }
            }
            return null
        }
    }

    String getName() {
        (to == null ? "${from}+" : (to == from ? from : "${from}-${to}")).toString()
    }

    List<String> getBounds() {
        to == null ? [from] : [from, to]
    }

    boolean contains(String version) {
        compare(version, from) >= 0 && (to == null || compare(version, to) <= 0)
    }

    static int compare(String a, String b) {
        List<Integer> x = a.tokenize(".")*.toInteger()
        List<Integer> y = b.tokenize(".")*.toInteger()
        for (int i = 0; i < Math.max(x.size(), y.size()); i++) {
            int d = (i < x.size() ? x[i] : 0) <=> (i < y.size() ? y[i] : 0)
            if (d != 0) {
                return d
            }
        }
        return 0
    }

    @Override
    boolean equals(Object o) {
        o instanceof McVersionRange && o.from == from && o.to == to
    }

    @Override
    int hashCode() {
        Objects.hash(from, to)
    }

    @Override
    String toString() {
        name
    }
}
