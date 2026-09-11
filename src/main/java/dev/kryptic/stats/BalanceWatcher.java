package dev.kryptic.stats;

import dev.kryptic.util.Amounts;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Your balance on the server, read out of chat.
 *
 * There is no packet for money. Balance is a plugin's idea, and the only way a
 * client learns it is by reading what the server prints when something asks —
 * so this watches chat for a line that names a balance and remembers the
 * number. That makes the figure honest but second-hand: it is whatever the
 * server last said, which is why the card shows how long ago that was rather
 * than presenting a stale number as current.
 *
 * Every scope keyword here is one that means <em>your</em> balance. "sent",
 * "received" and "paid" are deliberately not matched: a payment line carries a
 * number too, and treating it as a balance would overwrite the real one with
 * the size of the last transaction.
 */
public final class BalanceWatcher {

    /**
     * A balance line: a keyword, then a number, with at most a few words of
     * server formatting between them.
     */
    private static final Pattern BALANCE = Pattern.compile(
            "(?:your\\s+)?(?:balance|bal|money|coins|funds|wallet)\\b[^0-9$]{0,20}"
                    + "\\$?\\s*([0-9][0-9,.]*\\s*[kmbt]?)",
            Pattern.CASE_INSENSITIVE);

    /** Lines that carry a figure but are not a balance. */
    private static final Pattern TRANSFER = Pattern.compile(
            "\\b(sent|paid|received|withdrew|deposited|charged|refunded)\\b",
            Pattern.CASE_INSENSITIVE);

    private double value = -1.0;
    private long seenAt;

    /**
     * Reads a chat line, returning whether it held a balance.
     *
     * @param plain the message with formatting codes already stripped
     */
    public boolean onChat(String plain) {
        if (plain == null || plain.isEmpty()) return false;
        if (TRANSFER.matcher(plain).find()) return false;

        Matcher m = BALANCE.matcher(plain);
        if (!m.find()) return false;

        double parsed = Amounts.parse(m.group(1).replace(" ", ""));
        if (Double.isNaN(parsed) || parsed < 0.0) return false;

        this.value = parsed;
        this.seenAt = System.currentTimeMillis();
        return true;
    }

    public boolean known() {
        return this.seenAt > 0L && this.value >= 0.0;
    }

    /** The balance, short form, or null when the server has never said. */
    public String formatted() {
        return known() ? "$" + Amounts.shortForm(this.value) : null;
    }

    /** How long ago the server last said, or null when it never has. */
    public String age() {
        if (!known()) return null;
        long seconds = (System.currentTimeMillis() - this.seenAt) / 1000L;
        if (seconds < 10L) return "just now";
        if (seconds < 60L) return seconds + "s ago";
        if (seconds < 3600L) return (seconds / 60L) + "m ago";
        if (seconds < 86400L) return (seconds / 3600L) + "h ago";
        return String.format(Locale.ROOT, "%dd ago", seconds / 86400L);
    }

    public void clear() {
        this.value = -1.0;
        this.seenAt = 0L;
    }
}
