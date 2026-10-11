package dev.goetynouveau.network;
public record ChantStatePayload(java.util.UUID token, int duration, int remaining, boolean channeling) {
    public ChantStatePayload(java.util.UUID token, int duration, int remaining) { this(token, duration, remaining, false); }
}
