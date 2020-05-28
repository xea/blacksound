package so.blacklight.blacksound.id;

/**
 * Marks implementing classes as types whose instances are uniquely identified by some value and
 *
 * @param <T> Identifier type
 */
public interface Identifiable<T extends Id<?, ?>> {

    T getId();

    /**
     * Tests if the current instance and a provided Identifiable instance of the same type have the same identities.
     *
     * @param other other object
     * @return {@code true} if the two instances' identifiers are equal, otherwise {@code false}
     */
    default boolean equalsId(final Identifiable<T> other) {
        if (other == null) {
            return false;
        } else {
            return getId().equals(other.getId());
        }
    }
}
