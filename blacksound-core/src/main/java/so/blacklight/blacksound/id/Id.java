package so.blacklight.blacksound.id;

/**
 * A tag value that is used to uniquely identify an object of a given type.
 *
 * @param <I> the type of the data type to be identified with this ID, eg. Thread, User, Session, etc. In this interface
 *           it is just a phantom type (not used locally) but implementations should rely on it in order to prevent
 *           cross-type assignments.
 * @param <T> the value type of the ID, eg. Long, Integer, String, UUID, etc
 */
public interface Id<I, T> {

    T value();
}
