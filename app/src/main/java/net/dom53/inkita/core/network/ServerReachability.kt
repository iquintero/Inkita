package net.dom53.inkita.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Remembers that the Kavita server could not be connected to, e.g. a home server while on mobile data.
 *
 * The phone then still reports "online", so without this every server call would wait for its full timeout.
 * While the server is marked unreachable the reader skips server calls and uses downloaded pages straight away.
 * The mark expires after [COOLDOWN_MS], is cleared by any successful call, and is reset when the connection
 * changes (e.g. back on home wifi).
 */
object ServerReachability {
    const val COOLDOWN_MS = 60_000L

    @Volatile
    private var failedAtMillis = NEVER

    @Volatile
    private var clock: () -> Long = System::currentTimeMillis

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val unreachableState = MutableStateFlow(false)

    /** Observable form of [isUnreachable] for UI such as the "reading offline" label. */
    val unreachable: StateFlow<Boolean> = unreachableState

    fun markUnreachable() {
        failedAtMillis = clock()
        unreachableState.value = true
        // Let observers know when the cooldown is over, unless another failure has extended it.
        scope.launch {
            delay(COOLDOWN_MS)
            unreachableState.value = isUnreachable()
        }
    }

    fun markReachable() {
        failedAtMillis = NEVER
        unreachableState.value = false
    }

    /** Forget a previous failure, e.g. because the device switched networks. */
    fun reset() {
        failedAtMillis = NEVER
        unreachableState.value = false
    }

    fun isUnreachable(): Boolean {
        val failedAt = failedAtMillis
        if (failedAt == NEVER) return false
        return clock() - failedAt < COOLDOWN_MS
    }

    /**
     * True for failures that mean "could not reach the server" rather than a slow response:
     * refused or unroutable connections, unresolvable host names and connect timeouts.
     * Read timeouts are not counted because the server was reachable.
     */
    fun isConnectFailure(error: IOException): Boolean =
        when (error) {
            is UnknownHostException, is NoRouteToHostException -> true
            is SocketTimeoutException -> error.message.orEmpty().contains("connect", ignoreCase = true)
            is ConnectException -> true
            else -> false
        }

    internal fun setClockForTesting(source: () -> Long) {
        clock = source
        failedAtMillis = NEVER
        unreachableState.value = false
    }

    private const val NEVER = Long.MIN_VALUE
}
