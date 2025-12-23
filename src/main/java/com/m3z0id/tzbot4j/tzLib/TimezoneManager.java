package com.m3z0id.tzbot4j.tzLib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimezoneManager {
    public static Set<String> ZONES = ZoneId.getAvailableZoneIds();
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("<t:(\\d{1,13})(?::([fFdDtTrR]))?>");
    public static Set<Character> VALID_FORMATS = Set.of('f', 'F', 'd', 'D', 't', 'T', 'R');
    private final Map<UUID, String> timezones;

    public TimezoneManager() { this.timezones = new HashMap<>(); }
    public void addTimezone(UUID uuid, String timezone) throws IllegalArgumentException {
        if(!ZONES.contains(timezone)) throw new IllegalArgumentException("Timezone not found");
        timezones.put(uuid, timezone);
    }

    public void removeTimezone(UUID uuid) {
        timezones.remove(uuid);
    }

    public @Nullable ZoneId getTimezone(UUID uuid) {
        if(!timezones.containsKey(uuid)) return null;
        return ZoneId.of(timezones.get(uuid));
    }

    private @NotNull String formatRelativeTime(Instant timestamp) {
        if(timestamp.getEpochSecond() == System.currentTimeMillis() / 1000) return "now";

        Instant now = Instant.now();
        Duration duration = Duration.between(timestamp, now);

        boolean isFuture = duration.isNegative();

        duration = duration.abs();

        int years = (int) (duration.toDays() / 365.25);
        int days = (int) (duration.toDays() % 365.25);
        int hours = (int) duration.toHours() % 24;
        int minutes = (int) duration.toMinutes() % 60;
        int seconds = (int) duration.toSeconds() % 60;

        StringBuilder result = new StringBuilder();
        if (years > 0) result.append(years).append(" year").append(years > 1 ? "s" : "").append(", ");
        if (days > 0) result.append(days).append(" day").append(days > 1 ? "s" : "").append(", ");
        if (hours > 0) result.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(", ");
        if (minutes > 0) result.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(", ");
        if (seconds > 0) result.append(seconds).append(" second").append(seconds > 1 ? "s" : "");

        return result.toString().replaceAll(", $", "") + (isFuture ? " from now" : " ago");
    }

    public @Nullable String getTime(UUID player, long timestamp, char mode) {
        if(!VALID_FORMATS.contains(mode)) return null;
        ZoneId zone = this.getTimezone(player);
        if(zone == null) return null;

        Instant requestedInstant = Instant.ofEpochSecond(timestamp);
        return switch (mode) {
            case 'f' -> DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm").format(requestedInstant.atZone(zone));
            case 'F' -> DateTimeFormatter.ofPattern("EEE, MMMM dd, yyyy 'at' HH:mm").format(requestedInstant.atZone(zone));
            case 'd' -> DateTimeFormatter.ofPattern("dd.MM.yyyy").format(requestedInstant.atZone(zone));
            case 'D' -> DateTimeFormatter.ofPattern("MMMM dd, yyyy").format(requestedInstant.atZone(zone));
            case 't' -> DateTimeFormatter.ofPattern("HH:mm").format(requestedInstant.atZone(zone));
            case 'T' -> DateTimeFormatter.ofPattern("HH:mm:ss").format(requestedInstant.atZone(zone));
            case 'R' -> formatRelativeTime(requestedInstant);
            default -> "";
        };
    }

    public @Nullable Component adjustForPlayer(Component component, UUID playerId) {
        ZoneId id = this.getTimezone(playerId);
        if(id == null) return component;

        String stringRepr = PlainTextComponentSerializer.plainText().serialize(component);
        Matcher matcher = TIMESTAMP_PATTERN.matcher(stringRepr);

        while (matcher.find()) {
            long timestamp;
            try {
                timestamp = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }

            if(timestamp > 8640000000000L) continue;

            char mode = 'f';
            if (matcher.groupCount() > 1 && matcher.group(2) != null) mode = matcher.group(2).charAt(0);
            if (!VALID_FORMATS.contains(mode)) continue;

            String replacement = this.getTime(playerId, timestamp, mode);
            assert replacement != null; // Never false, mode is always valid

            TextReplacementConfig config = TextReplacementConfig.builder()
                    .matchLiteral(matcher.group(0))
                    .replacement(replacement)
                    .build();

            component = component.replaceText(config);
        }

        return component;
    }
}
