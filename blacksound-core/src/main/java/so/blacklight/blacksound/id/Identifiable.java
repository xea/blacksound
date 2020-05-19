package so.blacklight.blacksound.id;

/**
 * Indicates that the implementing class is uniquely identified by an Id-type class
 *
 * @param <T> Identifier type
 */
public interface Identifiable<T extends Id<?, ?>> {

    T getId();
}
