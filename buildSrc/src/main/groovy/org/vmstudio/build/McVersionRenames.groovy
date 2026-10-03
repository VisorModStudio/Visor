package org.vmstudio.build

import java.util.regex.Matcher
import java.util.regex.Pattern


class McVersionRenames {
    static final List<Rule> RULES = [
            type("1.21.11", "ResourceLocation", "Identifier"),
            move("1.21.11", "net.minecraft.Util", "net.minecraft.util.Util"),
            move("1.21.11", "net.minecraft.client.model.PlayerModel", "net.minecraft.client.model.player.PlayerModel"),
            move("1.21.11", "net.minecraft.client.model.PlayerCapeModel", "net.minecraft.client.model.player.PlayerCapeModel"),
            move("1.21.11", "net.minecraft.client.renderer.RenderType", "net.minecraft.client.renderer.rendertype.RenderType"),
            move("1.21.11", "net.minecraft.world.entity.animal.SnowGolem", "net.minecraft.world.entity.animal.golem.SnowGolem"),
            move("1.21.11", "net.minecraft.world.entity.animal.horse.AbstractHorse", "net.minecraft.world.entity.animal.equine.AbstractHorse"),
            move("1.21.11", "net.minecraft.world.entity.npc.AbstractVillager", "net.minecraft.world.entity.npc.villager.AbstractVillager"),
            move("1.21.11", "net.minecraft.world.entity.projectile.AbstractArrow", "net.minecraft.world.entity.projectile.arrow.AbstractArrow"),
            move("1.21.11", "net.minecraft.world.entity.projectile.ThrownTrident", "net.minecraft.world.entity.projectile.arrow.ThrownTrident"),
            move("1.21.11", "net.minecraft.world.entity.projectile.AbstractHurtingProjectile", "net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile"),
            move("1.21.11", "net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge", "net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge"),
            move("1.21.11", "net.minecraft.world.entity.projectile.ThrowableItemProjectile", "net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile"),
            move("1.21.11", "net.minecraft.world.entity.vehicle.AbstractBoat", "net.minecraft.world.entity.vehicle.boat.AbstractBoat"),
            move("1.21.11", "net.minecraft.world.entity.vehicle.Boat", "net.minecraft.world.entity.vehicle.boat.Boat"),
            move("1.21.11", "net.minecraft.world.entity.vehicle.Minecart", "net.minecraft.world.entity.vehicle.minecart.Minecart"),
            text("26.1", "accessWidener\tv1\tnamed", "accessWidener\tv1\tofficial"),
            type("26.1", "GuiGraphics", "GuiGraphicsExtractor"),
            move("26.1", "net.minecraft.client.gui.render.state.GuiRenderState", "net.minecraft.client.renderer.state.gui.GuiRenderState"),
            move("26.1", "net.minecraft.client.renderer.state.CameraRenderState", "net.minecraft.client.renderer.state.level.CameraRenderState"),
            move("26.1", "net.minecraft.client.renderer.state.LevelRenderState", "net.minecraft.client.renderer.state.level.LevelRenderState"),
            move("26.1", "net.minecraft.client.renderer.state.WeatherRenderState", "net.minecraft.client.renderer.state.level.WeatherRenderState"),
            move("26.1", "net.minecraft.client.resources.model.AtlasManager", "net.minecraft.client.resources.model.sprite.AtlasManager"),
            chain("com.mojang.blaze3d.vertex.VertexFormat.Mode",
                    "26.2", "com.mojang.blaze3d.PrimitiveTopology",
                    "26.3", "com.mojang.renderpearl.api.pipeline.PrimitiveTopology"),
            nested("26.2", "VertexFormat.Mode", "PrimitiveTopology"),
            text("26.2", "drawState().mode()", "drawState().primitiveTopology()"),
            type("26.2", "ContextualBarRenderer", "ContextualBar"),
            move("26.3", "com.mojang.blaze3d.GpuFormat", "com.mojang.renderpearl.api.GpuFormat"),
            move("26.3", "com.mojang.blaze3d.IndexType", "com.mojang.renderpearl.api.pipeline.IndexType"),
            move("26.3", "com.mojang.blaze3d.buffers.GpuBuffer", "com.mojang.renderpearl.api.buffers.GpuBuffer"),
            move("26.3", "com.mojang.blaze3d.buffers.GpuBufferSlice", "com.mojang.renderpearl.api.buffers.GpuBufferSlice"),
            move("26.3", "com.mojang.blaze3d.buffers.GpuFence", "com.mojang.renderpearl.api.commands.GpuFence"),
            move("26.3", "com.mojang.blaze3d.opengl.DirectStateAccess", "com.mojang.renderpearl.backend.opengl.DirectStateAccess"),
            move("26.3", "com.mojang.blaze3d.opengl.FrameBufferAttachment", "com.mojang.renderpearl.backend.opengl.FrameBufferAttachment"),
            move("26.3", "com.mojang.blaze3d.opengl.FrameBufferCache", "com.mojang.renderpearl.backend.opengl.FrameBufferCache"),
            move("26.3", "com.mojang.blaze3d.opengl.GlCommandEncoder", "com.mojang.renderpearl.backend.opengl.GlCommandEncoder"),
            move("26.3", "com.mojang.blaze3d.opengl.GlConst", "com.mojang.renderpearl.backend.opengl.GlConst"),
            move("26.3", "com.mojang.blaze3d.opengl.GlDevice", "com.mojang.renderpearl.backend.opengl.GlDevice"),
            move("26.3", "com.mojang.blaze3d.opengl.GlProgram", "com.mojang.renderpearl.backend.opengl.GlProgram"),
            move("26.3", "com.mojang.blaze3d.opengl.GlRenderPass", "com.mojang.renderpearl.backend.opengl.GlRenderPass"),
            move("26.3", "com.mojang.blaze3d.opengl.GlStateManager", "com.mojang.renderpearl.backend.opengl.GlStateManager"),
            move("26.3", "com.mojang.blaze3d.opengl.GlTexture", "com.mojang.renderpearl.backend.opengl.GlTexture"),
            move("26.3", "com.mojang.blaze3d.opengl.GlTextureView", "com.mojang.renderpearl.backend.opengl.GlTextureView"),
            move("26.3", "com.mojang.blaze3d.pipeline.BindGroupLayout", "com.mojang.renderpearl.api.pipeline.BindGroupLayout"),
            move("26.3", "com.mojang.blaze3d.pipeline.BlendEquation", "com.mojang.renderpearl.api.pipeline.BlendEquation"),
            move("26.3", "com.mojang.blaze3d.pipeline.BlendFunction", "com.mojang.renderpearl.api.pipeline.BlendFunction"),
            move("26.3", "com.mojang.blaze3d.pipeline.ColorTargetState", "com.mojang.renderpearl.api.pipeline.ColorTargetState"),
            move("26.3", "com.mojang.blaze3d.pipeline.CompiledRenderPipeline", "com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline"),
            move("26.3", "com.mojang.blaze3d.pipeline.DepthStencilState", "com.mojang.renderpearl.api.pipeline.DepthStencilState"),
            move("26.3", "com.mojang.blaze3d.pipeline.RenderPipeline", "com.mojang.renderpearl.api.pipeline.RenderPipeline"),
            move("26.3", "com.mojang.blaze3d.platform.BlendFactor", "com.mojang.renderpearl.api.pipeline.BlendFactor"),
            move("26.3", "com.mojang.blaze3d.platform.BlendOp", "com.mojang.renderpearl.api.pipeline.BlendOp"),
            move("26.3", "com.mojang.blaze3d.platform.CompareOp", "com.mojang.renderpearl.api.pipeline.CompareOp"),
            move("26.3", "com.mojang.blaze3d.platform.PolygonMode", "com.mojang.renderpearl.api.pipeline.PolygonMode"),
            move("26.3", "com.mojang.blaze3d.shaders.ShaderType", "com.mojang.renderpearl.api.pipeline.ShaderType"),
            move("26.3", "com.mojang.blaze3d.shaders.UniformType", "com.mojang.renderpearl.api.pipeline.UniformType"),
            move("26.3", "com.mojang.blaze3d.systems.CommandEncoder", "com.mojang.renderpearl.api.commands.CommandEncoder"),
            move("26.3", "com.mojang.blaze3d.systems.DeviceInfo", "com.mojang.renderpearl.api.device.DeviceInfo"),
            move("26.3", "com.mojang.blaze3d.systems.DeviceLimits", "com.mojang.renderpearl.api.device.DeviceLimits"),
            move("26.3", "com.mojang.blaze3d.systems.GpuBackend", "com.mojang.renderpearl.api.device.GpuBackend"),
            move("26.3", "com.mojang.blaze3d.systems.GpuDevice", "com.mojang.renderpearl.api.device.GpuDevice"),
            move("26.3", "com.mojang.blaze3d.systems.GpuDeviceBackend", "com.mojang.renderpearl.backend.api.GpuDeviceBackend"),
            move("26.3", "com.mojang.blaze3d.systems.GpuSurface", "com.mojang.renderpearl.api.device.GpuSurface"),
            move("26.3", "com.mojang.blaze3d.systems.RenderPass", "com.mojang.renderpearl.api.commands.RenderPass"),
            move("26.3", "com.mojang.blaze3d.systems.RenderPassDescriptor", "com.mojang.renderpearl.api.commands.RenderPassDescriptor"),
            move("26.3", "com.mojang.blaze3d.systems.TransientMemory", "com.mojang.renderpearl.api.buffers.TransientMemory"),
            move("26.3", "com.mojang.blaze3d.textures.AddressMode", "com.mojang.renderpearl.api.textures.AddressMode"),
            move("26.3", "com.mojang.blaze3d.textures.FilterMode", "com.mojang.renderpearl.api.textures.FilterMode"),
            move("26.3", "com.mojang.blaze3d.textures.GpuSampler", "com.mojang.renderpearl.api.textures.GpuSampler"),
            move("26.3", "com.mojang.blaze3d.textures.GpuTexture", "com.mojang.renderpearl.api.textures.GpuTexture"),
            move("26.3", "com.mojang.blaze3d.textures.GpuTextureView", "com.mojang.renderpearl.api.textures.GpuTextureView"),
            move("26.3", "com.mojang.blaze3d.vertex.VertexFormat", "com.mojang.renderpearl.api.vertex.VertexFormat"),
            move("26.3", "com.mojang.blaze3d.vertex.VertexFormatElement", "com.mojang.renderpearl.api.vertex.VertexFormatElement"),
            type("26.3", "EnderMan", "Enderman"),
            text("26.3", "event.scancode()", "event.keycode()"),
            text("26.3", "SwingSource.CLIENT", "SwingSource.PREDICTED"),
            type("26.3", "ItemInHandRenderer", "FirstPersonHandsAndItemsRenderer"),
            text("26.3", "gameRenderer.itemInHandRenderer", "gameRenderer.firstPersonHandsAndItemsRenderer"),
    ].flatten() as List<Rule>

    static class Rule {
        final String since
        final String oldPattern
        final String oldText
        final String newPattern
        final String newText
        final String oldFrom
        final String oldUntil
        final String newUntil

        Rule(String since, String oldPattern, String oldText, String newPattern, String newText,
             String oldFrom = null, String oldUntil = since, String newUntil = null) {
            this.since = since
            this.oldPattern = oldPattern
            this.oldText = oldText
            this.newPattern = newPattern
            this.newText = newText
            this.oldFrom = oldFrom
            this.oldUntil = oldUntil
            this.newUntil = newUntil
        }

        boolean forward(String version) {
            McVersionRange.compare(version, since) >= 0
        }

        boolean appliesTo(String version) {
            forward(version)
                    ? newUntil == null || McVersionRange.compare(version, newUntil) < 0
                    : (oldFrom == null || McVersionRange.compare(version, oldFrom) >= 0)
                        && McVersionRange.compare(version, oldUntil) < 0
        }

        String apply(String text, String version) {
            forward(version)
                    ? text.replaceAll(oldPattern, Matcher.quoteReplacement(newText))
                    : text.replaceAll(newPattern, Matcher.quoteReplacement(oldText))
        }
    }

    static List<Rule> forVersion(String version) {
        RULES.findAll { it.appliesTo(version) }
    }

    static List<Rule> ordered(String version) {
        List<Rule> rules = forVersion(version)
        rules.findAll { !it.forward(version) }.reverse() + rules.findAll { it.forward(version) }
    }

    static String apply(String text, String version) {
        ordered(version).inject(text) { String t, Rule r -> r.apply(t, version) }
    }

    static String signature(String version) {
        forVersion(version).collect { "${it.forward(version) ? '>' : '<'} ${it.oldText} ${it.newText}" }.join("\n")
    }

    // a class renamed in place: every whole-word use
    private static Rule type(String since, String oldName, String newName) {
        new Rule(since, "\\b${oldName}\\b", oldName, "\\b${newName}\\b", newName)
    }

    // a class moved to another package: dotted names and JVM descriptors, nested classes follow
    private static List<Rule> move(String since, String oldName, String newName) {
        [
                new Rule(since, "${Pattern.quote(oldName)}\\b", oldName, "${Pattern.quote(newName)}\\b", newName),
                new Rule(since, "${Pattern.quote(slashed(oldName))}\\b", slashed(oldName), "${Pattern.quote(slashed(newName))}\\b", slashed(newName)),
        ]
    }

    // a class moved more than once: a move per pair of its forms, used only by the versions of those two forms
    private static List<Rule> chain(String oldest, String... later) {
        List<String> names = [oldest]
        List<String> starts = [null]
        for (int i = 0; i < later.length; i += 2) {
            starts << later[i]
            names << later[i + 1]
        }
        List<Rule> rules = []
        for (int i = 0; i < names.size(); i++) {
            for (int j = i + 1; j < names.size(); j++) {
                String newUntil = j + 1 < names.size() ? starts[j + 1] : null
                [[names[i], names[j]], [slashed(names[i]), slashed(names[j])]].each { String o, String n ->
                    rules << new Rule(starts[j], "${Pattern.quote(o)}\\b", o, "${Pattern.quote(n)}\\b", n,
                            starts[i], starts[i + 1], newUntil)
                }
            }
        }
        rules
    }

    // a nested class that became a top-level one: every use by its simple name, qualified names are a move
    private static Rule nested(String since, String oldName, String newName) {
        new Rule(since, "(?<![\\w.])${Pattern.quote(oldName)}\\b", oldName, "(?<![\\w.])${Pattern.quote(newName)}\\b", newName)
    }

    // a literal that differs between the versions, outside of any class name
    private static Rule text(String since, String oldText, String newText) {
        new Rule(since, Pattern.quote(oldText), oldText, Pattern.quote(newText), newText)
    }

    private static String slashed(String name) {
        name.replace('.', '/')
    }
}
