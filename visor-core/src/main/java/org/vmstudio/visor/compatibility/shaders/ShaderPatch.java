package org.vmstudio.visor.compatibility.shaders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class ShaderPatch {
    private final List<Rewrite> rewrites = new ArrayList<>();

    public ShaderPatch rewrite(String replacement, Consumer<GlslPattern> shape) {
        GlslPattern shaped = new GlslPattern();
        shape.accept(shaped);
        rewrites.add(new Rewrite(shaped.compile(), replacement));
        return this;
    }

    public String applyTo(String source) {
        for (Rewrite rewrite : rewrites) {
            source = rewrite.match().matcher(source).replaceAll(rewrite.replacement());
        }
        return source;
    }

    private record Rewrite(Pattern match, String replacement) {}
}
