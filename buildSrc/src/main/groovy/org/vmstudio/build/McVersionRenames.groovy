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
    ].flatten() as List<Rule>

    static class Rule {
        final String since
        final String oldPattern
        final String oldText
        final String newPattern
        final String newText

        Rule(String since, String oldPattern, String oldText, String newPattern, String newText) {
            this.since = since
            this.oldPattern = oldPattern
            this.oldText = oldText
            this.newPattern = newPattern
            this.newText = newText
        }

        boolean forward(String version) {
            McVersionRange.compare(version, since) >= 0
        }

        String apply(String text, String version) {
            forward(version)
                    ? text.replaceAll(oldPattern, Matcher.quoteReplacement(newText))
                    : text.replaceAll(newPattern, Matcher.quoteReplacement(oldText))
        }
    }

    static String apply(String text, String version) {
        RULES.inject(text) { String t, Rule r -> r.apply(t, version) }
    }

    static String signature(String version) {
        RULES.collect { "${it.forward(version) ? '>' : '<'} ${it.oldText} ${it.newText}" }.join("\n")
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

    private static String slashed(String name) {
        name.replace('.', '/')
    }
}
