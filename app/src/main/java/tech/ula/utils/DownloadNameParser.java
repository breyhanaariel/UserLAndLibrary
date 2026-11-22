package tech.ula.utils;

/**
 * Parses UserLAnd's distribution-asset-version download names.
 * Kept free of Android dependencies so isolation can be tested with javac.
 */
public final class DownloadNameParser {
    private DownloadNameParser() {}

    public static Parsed parse(String downloadName, String assetName) {
        if (downloadName == null || assetName == null || assetName.isEmpty()) {
            throw new IllegalArgumentException("Missing download or asset name");
        }
        String separator = "-" + assetName + "-";
        int boundary = downloadName.lastIndexOf(separator);
        if (boundary <= 0) {
            throw new IllegalArgumentException("Invalid download name: " + downloadName);
        }
        String distribution = downloadName.substring(0, boundary);
        String version = downloadName.substring(boundary + separator.length());
        if (version.trim().isEmpty() || distribution.contains("/") || version.contains("/")
                || distribution.contains("\\") || version.contains("\\")) {
            throw new IllegalArgumentException("Invalid download name: " + downloadName);
        }
        return new Parsed(distribution, version);
    }

    public static final class Parsed {
        public final String distribution;
        public final String version;

        private Parsed(String distribution, String version) {
            this.distribution = distribution;
            this.version = version;
        }
    }
}
