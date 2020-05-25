package so.blacklight.blacksound.subscriber;

public class SubscriberHandle {

    private final String id;

    private final String accessToken;

    private final String refreshToken;

    private final long expires;

    private final boolean enabled;

    SubscriberHandle(final String id, final String accessToken, final String refreshToken, final long expires, final boolean enabled) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expires = expires;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpires() {
        return expires;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
