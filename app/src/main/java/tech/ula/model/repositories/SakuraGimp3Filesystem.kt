package tech.ula.model.repositories

/**
 * Separate filesystem namespace for GIMP 3. Existing "gimp" is never reused.
 *
 * This is a fail-closed release gate: the ARM64 archive and support assets
 * must be published and independently verified before enabling downloads.
 */
internal object SakuraGimp3Filesystem {
    const val DISTRIBUTION_TYPE = "sakura-gimp3"
    const val LEGACY_DISTRIBUTION_TYPE = "gimp"
    const val ARCHITECTURE = "arm64"

    // Deliberately false until the release archive, support bundle, and
    // extraction/startup compatibility have been verified.
    private const val RELEASE_VERIFIED = false

    fun requirePublishedRelease(distributionType: String, architecture: String) {
        if (distributionType != DISTRIBUTION_TYPE) return
        require(architecture == ARCHITECTURE) {
            "Sakura GIMP 3 is only packaged for ARM64."
        }
        check(RELEASE_VERIFIED) {
            "Sakura GIMP 3 release is not verified; legacy GIMP 2 is untouched."
        }
    }
}
