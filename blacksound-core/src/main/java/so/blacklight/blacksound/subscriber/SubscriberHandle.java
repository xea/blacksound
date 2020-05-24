package so.blacklight.blacksound.subscriber;

public class SubscriberHandle {

    private final String id;

    private final String accessToken;

    private final String refreshToken;

    private final long expires;

    SubscriberHandle(final String id, final String accessToken, final String refreshToken, final long expires) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expires = expires;
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
}
