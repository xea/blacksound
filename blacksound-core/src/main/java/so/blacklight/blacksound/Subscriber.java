package so.blacklight.blacksound;

public class Subscriber {

    private final String accessToken;

    private final String refreshToken;

    public Subscriber(final String accessToken, final String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
