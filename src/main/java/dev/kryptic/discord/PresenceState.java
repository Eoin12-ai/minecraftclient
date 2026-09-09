package dev.kryptic.discord;

import java.util.Objects;

/**
 * One immutable snapshot of what the presence should say.
 *
 * The game thread builds these and hands them to
 * {@link DiscordPresenceService#publish}; the service thread compares each new
 * snapshot against the last one it sent and skips the write when nothing
 * changed, which is what keeps the update rate under Discord's limit.
 *
 * {@code startEpochMillis} deliberately stays constant for the life of a
 * session so the elapsed timer counts up smoothly instead of resetting on every
 * update; it is 0 when the timer is switched off.
 */
public record PresenceState(String details,
                            String state,
                            long startEpochMillis,
                            String largeImage,
                            String largeText,
                            String smallImage,
                            String smallText) {

    public static final PresenceState EMPTY =
            new PresenceState("", "", 0L, "", "", "", "");

    public PresenceState {
        details    = trim(details, 128);
        state      = trim(state, 128);
        largeImage = trim(largeImage, 64);
        largeText  = trim(largeText, 128);
        smallImage = trim(smallImage, 64);
        smallText  = trim(smallText, 128);
    }

    /**
     * Discord rejects an activity field of one or two characters, so anything
     * that short is dropped rather than sent and refused.
     */
    private static String trim(String s, int max) {
        if (s == null) return "";
        String t = s.strip();
        if (t.length() > max) t = t.substring(0, max);
        return t.length() < 2 ? "" : t;
    }

    public boolean isBlank() {
        return details.isEmpty() && state.isEmpty();
    }

    /** Field-by-field equality, which is what the send-on-change check needs. */
    public boolean sameAs(PresenceState other) {
        return other != null
                && Objects.equals(details, other.details)
                && Objects.equals(state, other.state)
                && startEpochMillis == other.startEpochMillis
                && Objects.equals(largeImage, other.largeImage)
                && Objects.equals(largeText, other.largeText)
                && Objects.equals(smallImage, other.smallImage)
                && Objects.equals(smallText, other.smallText);
    }
}
