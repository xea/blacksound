package so.blacklight.blacksound.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Session ID")
public class SessionIdTest {

    @RepeatedTest(1000)
    @DisplayName("No-argument constructor should generate new instances with a random UUID")
    void noArgConstructorGeneratesRandomId() {
        final var id = new SessionId();

        assertNotNull(id);
        assertNotNull(id.value());
    }

    @RepeatedTest(1000)
    @DisplayName("No two instances of SessionID are generated with the same UUID")
    void noArgUUIDsAreUnique() {
        // Note: this test is obviously simplistic, at some point it'll be worth investing in writing a better one
        final var id1 = new SessionId();
        final var id2 = new SessionId();

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("If a null value is passed to the single argument constructor, a random UUID is generated")
    void nullParameterGeneratesRandomUUID() {
        final var id = new SessionId(null);

        assertNotNull(id.value());
    }

    @Test
    @DisplayName("Two session IDs are considered equal if and only if their contained UUID values are equal")
    void equalityDependsOnInternalUUID() {
        // UUIDs 1 and 2 are the same and should lead to equality
        final var uuid1 = UUID.randomUUID();
        final var uuid2 = UUID.fromString(uuid1.toString());
        // UUID 3 is a different one and should not be equal with the above two
        final var uuid3 = UUID.randomUUID();

        final var id1 = new SessionId(uuid1);
        final var id2 = new SessionId(uuid2);
        final var id3 = new SessionId(uuid3);

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertNotEquals(id2, id3);
    }

    @Test
    @DisplayName("Session ID equality should be symmetric, reflexive and transitive")
    void equalityProperties() {
        final var uuid1 = UUID.randomUUID();

        // Positive cases
        final var id1 = new SessionId(uuid1);
        final var id2 = new SessionId(uuid1);
        final var id3 = new SessionId(uuid1);

        // Reflexive
        assertEquals(id1, id1);
        // Symmetric
        assertEquals(id1, id2);
        assertEquals(id2, id1);
        // Transitive
        assertEquals(id2, id3);
        assertEquals(id1, id3);

        // Negative cases
        final var uuid2 = UUID.randomUUID();
        final var uuid3 = UUID.randomUUID();
        final var id4 = new SessionId(uuid2);
        final var id5 = new SessionId(uuid3);

        // The reflexive property is not tested here

        // Symmetric
        assertNotEquals(id1, id4);
        assertNotEquals(id4, id1);
        // Transitive
        assertNotEquals(id4, id5);
        assertNotEquals(id1, id5);
    }
}
