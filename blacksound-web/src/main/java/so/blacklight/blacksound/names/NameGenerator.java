package so.blacklight.blacksound.names;

public class NameGenerator {

    private static final String[] NAME_POOL = new String[] {
            "Axl Rose",
            "Bob Dylan",
            "Dave Mustaine",
            "David Bowie",
            "Eric Clapton",
            "Kirk Hammett",
            "Kurt Cobain",
            "James Hetfield",
            "James LaBrie",
            "Jimi Hendrix",
            "John Lennon",
            "John Myung",
            "John Petrucci",
            "Jon Lord",
            "Lars Ulrich",
            "Marko Paasikoski",
            "Markus Grosskopf",
            "Michael Jackson",
            "Mike Portnoy",
            "Ozzy Osbourne",
            "Paul McCartney",
            "Rob Halford",
            "Robert Trujillo",
            "Steve Vai",
            "Tommy Portimo"
    };

    public String generate(final String input) {
        return NAME_POOL[Math.abs(input.hashCode()) % NAME_POOL.length];
    }
}
