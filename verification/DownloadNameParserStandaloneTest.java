import tech.ula.utils.DownloadNameParser;

/** No Android SDK, Gradle, bVNC, or third-party dependencies. */
public final class DownloadNameParserStandaloneTest {
    private static int checks = 0;

    private static void check(String name, String asset, String expectedDist, String expectedVersion) {
        DownloadNameParser.Parsed parsed = DownloadNameParser.parse(name, asset);
        if (!expectedDist.equals(parsed.distribution) || !expectedVersion.equals(parsed.version)) {
            throw new AssertionError(name + ": got " + parsed.distribution + "/" + parsed.version);
        }
        checks++;
    }

    private static void reject(String name, String asset) {
        try {
            DownloadNameParser.parse(name, asset);
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError("Accepted invalid download name: " + name);
    }

    public static void main(String[] args) {
        check("sakura-gimp3-rootfs.tar.gz-v0.0.1", "rootfs.tar.gz", "sakura-gimp3", "v0.0.1");
        check("sakura-gimp3-assets.tar.gz-v0.0.1", "assets.tar.gz", "sakura-gimp3", "v0.0.1");
        check("gimp-rootfs.tar.gz-v0.0.1", "rootfs.tar.gz", "gimp", "v0.0.1");
        check("gimp-assets.tar.gz-v0.0.1", "assets.tar.gz", "gimp", "v0.0.1");
        check("sakura-gimp3-rootfs.tar.gz-v0.0.1-rc1", "rootfs.tar.gz", "sakura-gimp3", "v0.0.1-rc1");
        reject("sakura-gimp3-rootfs.tar.gz-", "rootfs.tar.gz");
        reject("-rootfs.tar.gz-v1", "rootfs.tar.gz");
        reject("sakura-gimp3-v1", "rootfs.tar.gz");
        reject("sakura-gimp3-rootfs.tar.gz-../escape", "rootfs.tar.gz");
        reject("sakura-gimp3/rootfs.tar.gz-v1", "rootfs.tar.gz");
        reject("sakura-gimp3-rootfs.tar.gz-v1\\bad", "rootfs.tar.gz");
        System.out.println("PASS: " + checks + " standalone production-parser checks");
    }
}
