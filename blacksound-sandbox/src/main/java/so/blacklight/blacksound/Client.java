package so.blacklight.blacksound;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;

public class Client {

    public static void main(String[] args) throws ParseException, SpotifyWebApiException, IOException {

        final URI redirectUri = SpotifyHttpManager.makeUri("http://blacklight.so/spotify-redirect");

        final SpotifyApi api = new SpotifyApi.Builder()
                .setClientId("ce2bfbfbe41241adbe8463a0d3403e61")
                .setClientSecret("ae79f6f69e1b45b5b0d95d469c1241a0")
                .setRedirectUri(redirectUri)
                .build();

        final var authRequest = api.authorizationCodeUri()
                .scope("user-read-playback-state streaming playlist-read-collaborative user-modify-playback-state user-read-currently-playing")
                .build();
        final var authResponse = authRequest.execute();

        /*
        final var code = "AQC7u9J9y5hqX0R32WbDDvwgGwkNV-iCz55x7qIr2NRRMauK32AJiJymPlO_QgJUjwXUrXJNiIOgAyN4kEzy64NulVJ18nN_hx8sWBdPUqLR7Z3noWo01lnFmiA7Kw_wdjFsccj_Lh4-fC_WqvprfqbpiGg4IOScxvW156UQ26ytPblHd3fkxTCEYfZbxdlr3iti0wYO6szmHYZKCh9hJqTO4z4J8ZO6kYdbo5sosJslZnGjUNsljR6HkXqlnqELRjNanOCi2Fhb7GBVsKIGsxI08VPI_827JA2akPn1m1RSnb_cJlZn99wPIHgyl4YMDxdXBWzwck04yqP2HIR2BvGGdeZHLozuCwwWqD7RTXvHFHUh2QM";
        final var credentials = api.authorizationCode(code).build().execute();

        final String accessToken = credentials.getAccessToken();
        final String refreshtoken = credentials.getRefreshToken();
         */

        final String accessToken = "BQCZjMkupg36MpQ4Jf0cI43IflK3AtSkfbuw95qqK572Krq_cHNmjRhayzJjgMy6KWVjJz9LNZcouQyYeU4E6TGCe7pqeHsK_5rhBXV59Z6YmWeXYKNzzXtKEQPD-LJ4Fl-PZHSFilWct__CKWa_JQkpEVUCIj5kng";
        final String refreshtoken = "AQBRzgBtBsJGHbMX0LMKUuAKUK6a0bL77vUgaeCj4bDxcC0feD15eJZxaC-lqmVYpOs6_5pBHnVeSvN6HkwA2xLi6IxiL7fE3h1WbxS7d_aw_apecuDeEx3gZm6QHITRDBg";

        api.setAccessToken(accessToken);
        api.setRefreshToken(refreshtoken);

        final var request = api.skipUsersPlaybackToNextTrack().build();

        final var response = request.execute();

        System.out.println("Response: " + response);
    }
}
