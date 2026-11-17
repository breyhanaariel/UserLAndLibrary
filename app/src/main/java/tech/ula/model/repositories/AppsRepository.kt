package tech.ula.model.repositories

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.* // ktlint-disable no-wildcard-imports
import tech.ula.BuildConfig
import tech.ula.model.daos.AppsDao
import tech.ula.model.entities.App
import tech.ula.model.remote.GithubAppsFetcher
import tech.ula.utils.*
import tech.ula.utils.preferences.AppsPreferences
import java.util.Locale

class AppsRepository(
    private val appsDao: AppsDao,
    private val remoteAppsSource: GithubAppsFetcher,
    private val appsPreferences: AppsPreferences,
    private val sharedPreferences: SharedPreferences,
    private val logger: Logger = SentryLogger()
) {
    private val className = "AppsRepository"

    private val refreshStatus = MutableLiveData<RefreshStatus>()

    fun getAllApps(): LiveData<List<App>> {
        return appsDao.getAllApps()
    }

    fun getActiveApps(): LiveData<List<App>> {
        return appsDao.getActiveApps()
    }

    fun getRefreshStatus(): LiveData<RefreshStatus> {
        return refreshStatus
    }

    suspend fun refreshData(scope: CoroutineScope) {
        refreshStatus.postValue(RefreshStatus.ACTIVE)
        val distributionsList = mutableSetOf<String>()
        try {
            // Validate the entire catalog and download assets before changing database entries.
            val apps = remoteAppsSource.fetchAppsList()
            for (app in apps) {
                remoteAppsSource.fetchAppIcon(app)
                remoteAppsSource.fetchAppDescription(app)
                remoteAppsSource.fetchAppScript(app)
                if (app.category.lowercase(Locale.ENGLISH) == "distribution") {
                    distributionsList.add(app.name)
                }
            }
            // Remove only cached app catalog records, not filesystems, sessions or user files.
            // This replaces stale GIMP metadata (filesystemRequired=gimp) with Debian metadata.
            appsDao.deleteAllApps()
            for (app in apps) {
                appsDao.insertApp(app)
            }
            appsPreferences.setDistributionsList(distributionsList)
            refreshStatus.postValue(RefreshStatus.FINISHED)
        } catch (err: Exception) {
            refreshStatus.postValue(RefreshStatus.FAILED)
            logger.addExceptionBreadcrumb(err)
            logger.addBreadcrumb(UlaBreadcrumb(className, BreadcrumbType.RuntimeError, err.toString()))
            logger.sendEvent("App Refresh Failed")
        }
    }
}

enum class RefreshStatus {
    ACTIVE, FINISHED, FAILED, INACTIVE
}