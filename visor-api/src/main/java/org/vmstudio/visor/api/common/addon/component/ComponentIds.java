package org.vmstudio.visor.api.common.addon.component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;


/**
 * Component ID validation utility.
 * <p>
 * A valid component ID must:
 * <ul>
 *     <li>Be between {@value MIN_LENGTH} and {@value MAX_LENGTH} characters long</li>
 *     <li>Start with a lowercase letter {@code [a-z]}</li>
 *     <li>Contain only lowercase alphanumeric characters, hyphens, or underscores {@code [a-z0-9_-]}</li>
 * </ul>
 * Examples: {@code "my_overlay"}, {@code "hud-compass"}, {@code "custom_preset_01"}
 * </p>
 */
public final class ComponentIds {

    /**
     * Minimum allowed ID length.
     */
    public static final int MIN_LENGTH = 2;

    /**
     * Maximum allowed ID length.
     */
    public static final int MAX_LENGTH = 64;

    /**
     * Regex pattern.
     */
    public static final String REGEX = "[a-z][a-z0-9_-]{" + (MIN_LENGTH - 1) + "," + (MAX_LENGTH - 1) + "}";

    /**
     * Compiled pattern.
     */
    public static final Pattern PATTERN = Pattern.compile(REGEX);


    /**
     * If the given ID is a valid component ID.
     *
     * @param id the id to validate
     * @return true if the ID is valid
     */
    public static boolean isValid(@Nullable String id) {
        return id != null && PATTERN.matcher(id).matches();
    }

    /**
     * Validate the given ID and return an error reason, or null if valid.
     *
     * @param id the id to validate
     * @return error reason or null if valid
     */
    @Nullable
    public static String validate(@Nullable String id) {
        if (id == null || id.isEmpty()) {
            return "ID must not be empty";
        }
        if (id.length() < MIN_LENGTH) {
            return "ID must be at least " + MIN_LENGTH + " characters long";
        }
        if (id.length() > MAX_LENGTH) {
            return "ID must be at most " + MAX_LENGTH + " characters long";
        }
        char first = id.charAt(0);
        if (first < 'a' || first > 'z') {
            return "ID must start with a lowercase letter [a-z], found: '" + first + "'";
        }
        for (int i = 1; i < id.length(); i++) {
            char c = id.charAt(i);
            if (!((c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '_'
                    || c == '-')) {
                return "ID contains invalid character '" + c
                        + "' at index " + i
                        + ". Only [a-z0-9_-] are allowed";
            }
        }
        return null;
    }

    /**
     * Validate the given ID and throw an exception if invalid.
     *
     * @param id the id to validate
     * @throws IllegalArgumentException if the ID is invalid
     */
    public static void requireValid(@NotNull String id) {
        String error = validate(id);
        if (error != null) {
            throw new IllegalArgumentException("Invalid component ID '" + id + "': " + error);
        }
    }


    private ComponentIds() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

}