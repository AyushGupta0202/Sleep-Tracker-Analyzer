package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private var tonight = MutableLiveData<SleepNight?>()

    val nights = database.getAllNights()


    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

    val navigateToSleepQuality : LiveData<SleepNight>
            get() = _navigateToSleepQuality

    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    val startButtonVisible = Transformations.map(tonight) {
        it == null
    }

    val stopButtonVisible = Transformations.map(tonight) {
        it != null
    }

    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }
    }

    fun onStartTracking() {
         uiScope.launch {
             val newNight = SleepNight()

             insert(newNight)

             tonight.value = getTonightFromDatabase()
         }
    }

    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    /**
     * Pattern for coroutines task
     * from the scope launch coroutine and
     * switch to the IO thread
     * pass in the suspend function
     */
/*
    fun someWorkNeedsToBeDone() {
        uiScope.launch {
            suspendFunction()
        }
    }

    suspend fun suspendFunction() {
        withContext(Dispatchers.IO) {
            longrunningwork()
        }
    }

 */

    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)

            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
            _showSnackbarEvent.value = true

        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()

        }
    }

    private val _navigateToSleepDataQuality = MutableLiveData<Long>()
    val navigateToSleepDataQuality
        get() = _navigateToSleepDataQuality

    fun onSleepNightClicked(id: Long) {
        _navigateToSleepDataQuality.value = id
    }

    fun onSleepDataQualityNavigated() {
        _navigateToSleepDataQuality.value = null
    }

//    private var tonight = MutableLiveData<SleepNight?>()
//
//    private val nights = database.getAllNights()
//
//    /**
//     * Converted nights to Spanned for displaying.
//     */
//    val nightsString = Transformations.map(nights) { nights ->
//        formatNights(nights, application.resources)
//    }
//
//    /**
//     * If tonight has not been set, then the START button should be visible.
//     */
//    val startButtonVisible = Transformations.map(tonight) {
//        null == it
//    }
//
//    /**
//     * If tonight has been set, then the STOP button should be visible.
//     */
//    val stopButtonVisible = Transformations.map(tonight) {
//        null != it
//    }
//
//    /**
//     * If there are any nights in the database, show the CLEAR button.
//     */
//    val clearButtonVisible = Transformations.map(nights) {
//        it?.isNotEmpty()
//    }
//
//    /**
//     * Request a toast by setting this value to true.
//     *
//     * This is private because we don't want to expose setting this value to the Fragment.
//     */
//    private var _showSnackbarEvent = MutableLiveData<Boolean>()
//
//    /**
//     * If this is true, immediately `show()` a toast and call `doneShowingSnackbar()`.
//     */
//    val showSnackBarEvent: LiveData<Boolean>
//        get() = _showSnackbarEvent
//
//    /**
//     * Variable that tells the Fragment to navigate to a specific SleepQualityFragment]
//     *
//     * This is private because we don't want to expose setting this value to the Fragment.
//     */
//
//    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
//    /**
//     * Call this immediately after calling `show()` on a toast.
//     *
//     * It will clear the toast request, so if the user rotates their phone it won't show a duplicate
//     * toast.
//     */
//
//    fun doneShowingSnackbar() {
//        _showSnackbarEvent.value = false
//    }
//    /**
//     * If this is non-null, immediately navigate to SleepQualityFragment] and call [doneNavigating]
//     */
//    val navigateToSleepQuality: LiveData<SleepNight>
//        get() = _navigateToSleepQuality
//
//    /**
//     * Call this immediately after navigating to SleepQualityFragment]
//     *
//     * It will clear the navigation request, so if the user rotates their phone it won't navigate
//     * twice.
//     */
//    fun doneNavigating() {
//        _navigateToSleepQuality.value = null
//    }
//
//    init {
//        initializeTonight()
//    }
//
//    private fun initializeTonight() {
//        viewModelScope.launch {
//            tonight.value = getTonightFromDatabase()
//        }
//    }
//
//    /**
//     *  Handling the case of the stopped app or forgotten recording,
//     *  the start and end times will be the same.j
//     *
//     *  If the start time and end time are not the same, then we do not have an unfinished
//     *  recording.
//     */
//    private suspend fun getTonightFromDatabase(): SleepNight? {
//        var night = database.getTonight()
//        if (night?.endTimeMilli != night?.startTimeMilli) {
//            night = null
//        }
//        return night
//    }
//
//
//    private suspend fun clear() {
//        database.clear()
//    }
//
//    private suspend fun update(night: SleepNight) {
//        database.update(night)
//    }
//
//    private suspend fun insert(night: SleepNight) {
//        database.insert(night)
//    }
//
//    /**
//     * Executes when the START button is clicked.
//     */
//    fun onStartTracking() {
//        viewModelScope.launch {
//            // Create a new night, which captures the current time,
//            // and insert it into the database.
//            val newNight = SleepNight()
//
//            insert(newNight)
//
//            tonight.value = getTonightFromDatabase()
//        }
//    }
//
//    /**
//     * Executes when the STOP button is clicked.
//     */
//    fun onStopTracking() {
//        viewModelScope.launch {
//            // In Kotlin, the return@label syntax is used for specifying which function among
//            // several nested ones this statement returns from.
//            // In this case, we are specifying to return from launch(),
//            // not the lambda.
//            val oldNight = tonight.value ?: return@launch
//
//            // Update the night in the database to add the end time.
//            oldNight.endTimeMilli = System.currentTimeMillis()
//
//            update(oldNight)
//
//            // Set state to navigate to the SleepQualityFragment.
//            _navigateToSleepQuality.value = oldNight
//        }
//    }
//
//    /**
//     * Executes when the CLEAR button is clicked.
//     */
//    fun onClear() {
//        viewModelScope.launch {
//            // Clear the database table.
//            clear()
//
//            // And clear tonight since it's no longer in the database
//            tonight.value = null
//        }
//
//        // Show a snackbar message, because it's friendly.
//        _showSnackbarEvent.value = true
//    }

}

