package org.vmstudio.visor.compatibility.shaders;

import java.util.regex.Pattern;

public final class ShaderPatch {
    private final String name;
    private final String sample;
    private final String replacement;
    private final Pattern[] matchers;

    private ShaderPatch(String name, String sample, String replacement, Pattern[] matchers) {
        this.name = name;
        this.sample = sample;
        this.replacement = replacement;
        this.matchers = matchers;
    }

    public static ShaderPatch of(String name, String sample, String replacement, String... matchers) {
        Pattern[] compiled = new Pattern[matchers.length];
        for (int i = 0; i < matchers.length; i++) {
            compiled[i] = GlslRegex.compile(matchers[i]);
        }
        return new ShaderPatch(name, sample, replacement, compiled);
    }

    public String applyTo(String source) {
        for (Pattern matcher : matchers) {
            source = matcher.matcher(source).replaceAll(replacement);
        }
        return source;
    }

    public String name() {
        return name;
    }

    public String sample() {
        return sample;
    }

    public Pattern[] matchers() {
        return matchers;
    }
}
