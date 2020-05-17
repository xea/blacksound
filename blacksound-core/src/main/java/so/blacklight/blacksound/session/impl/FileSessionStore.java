package so.blacklight.blacksound.session.impl;

import so.blacklight.blacksound.session.Session;
import so.blacklight.blacksound.session.SessionId;
import so.blacklight.blacksound.session.SessionStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class FileSessionStore implements SessionStore {

    private final File sessionDir;

    public FileSessionStore(final boolean createDir, final Path sessionDirPath) throws IOException {
        sessionDir = Optional.ofNullable(sessionDirPath).orElse(Path.of("file://sessions")).toFile();

        if (!sessionDir.exists() && createDir) {
            if (!sessionDir.mkdir()) {
                throw new IOException("Could not create session directory");
            }
        }
    }

    @Override
    public Optional<Session> getSession(SessionId sessionId) {
        return Optional.empty();
    }

    @Override
    public Session getOrCreateSession(SessionId sessionId) {
        return null;
    }

}
