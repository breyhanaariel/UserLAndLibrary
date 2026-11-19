package tech.ula.model.repositories

/**
 * Reserved filesystem identity for an opt-in GIMP 3 migration.
 *
 * The current GIMP 2 filesystem uses distributionType "gimp". This new identity
 * MUST NOT be aliased to "gimp", even as a fallback: UserLAnd keys cached assets
 * and filesystem selection by distributionType.
 *
 * Fail closed until an independently packaged ARM64 release has been verified
 * for UserLAnd support files, Python 3 plug-ins, and archive integrity.
 */
internal object SakuraGimp3Filesystem {
    const val DISTRIBUTION_TYPE = "sakura-gimp3"
    const val LEGACY_DISTRIBUTION_TYPE = "gimp"

    fun requirePublishedRelease(distributionType: String): Unit {
        if (distributionType == DISTRIBUTION_TYPE) {
            throw IllegalStateException(
                "Sakura GIMP 3 filesystem is reserved but no verified ARM64 " +
                    "rootfs/assets release is configured. Existing GIMP 2 data is untouched."
            )
        }
    }
}
