package org.vmstudio.visor.api.common.addon.component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;
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


    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");
    private static final Pattern NOT_ID_CHARS = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_UNDERSCORES = Pattern.compile("^_+|_+$");
    private static final int ID_BASE_MAX_LENGTH = MAX_LENGTH - 6;


    public static String generateId(@NotNull String componentName,
                                    @Nullable String idPrefix,
                                    @NotNull Predicate<String> idUsedValidation){
        String slug = slugify(componentName);
        if(idPrefix != null) {
            requireValid(idPrefix);
        }else{
            //if slug is a valid id - use it, otherwise add id as a fixed prefix
            idPrefix = PATTERN.matcher(slug).lookingAt() ? slug : "id";
        }
        String base;
        if(slug.isEmpty()){
            base = idPrefix;
        }else if(slug.equals(idPrefix) || slug.startsWith(idPrefix + "_")){
            base = slug;
        }else{
            base = idPrefix + "_" + slug;
        }
        if(base.length() > ID_BASE_MAX_LENGTH){
            base = trimUnderscores(base.substring(0, ID_BASE_MAX_LENGTH));
        }

        String id = base;
        for(int i = 2; idUsedValidation.test(id); i++){
            id = base + "_" + i;
        }
        return id;
    }

    private static String slugify(@NotNull String text){
        String noAccents = DIACRITICS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("");
        return trimUnderscores(NOT_ID_CHARS.matcher(noAccents.toLowerCase(Locale.ROOT)).replaceAll("_"));
    }

    private static String trimUnderscores(@NotNull String text){
        return EDGE_UNDERSCORES.matcher(text).replaceAll("");
    }


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