package com.mapbox.maps.plugin.viewport.state

import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.plugin.animation.Cancelable
import com.mapbox.maps.plugin.viewport.ViewportPlugin
import com.mapbox.maps.plugin.viewport.transition.ViewportTransition

/**
 * [ViewportState] is used to observe the data source associated with the state and query the camera
 * to keep updating according to current viewport data. [ViewportState] is also used by [ViewportPlugin]
 * to orchestrate transitions to and from different states.
 *
 * The [observeDataSource] allows [ViewportTransition] to consume a stream of camera
 * updates from a target state while executing a transition.
 *
 * The [startUpdatingCamera] and [stopUpdatingCamera] are invoked to tell the state that it should
 * assume or relinquish control of the map's camera. These are typically used by [ViewportPlugin] itself
 * after a successful transition into a state and when exiting a state, respectively.
 *
 * Mapbox provides implementations of [ViewportState] that can be created and configured via methods
 * on [ViewportPlugin]. Applications may also define their own implementations to handle advanced use
 * cases not covered by the provided implementations.
 *
 * [ViewportState] should generally pre-warm their data sources as soon as they are created to minimise
 * delays when they become current. For this reason, only states that are current(or soon-to-be) needed
 * should be kept alive so that unneeded resources(e.g. location services) can be released.
 *
 * Note: Users are responsible to create the viewport states and keep a reference to these states for
 * future operations.
 *
 * @see [FollowPuckViewportState]
 * @see [OverviewViewportState]
 */
@MapboxExperimental
interface ViewportState {
  /**
   * Registers an observer to receive the cameras being generated by this [ViewportState].
   *
   * This method is commonly used by [ViewportTransition] implementations to obtain the target camera
   * for transition animations. Transitions typically cannot start their animations until after the
   * observer is invoked for the first time, so it's a good idea for [ViewportState] to invoke observer
   * with the current camera if it's not too stale rather than waiting for the next camera change to
   * occur. To increase the likelihood that a valid camera exists when an observer is registered, design
   * [ViewportState] implementations so that they start updating their internal state prior to when they
   * are passed to [ViewportPlugin.transitionTo].
   *
   * The caller may either cancel on the returned [Cancelable] or return false from [ViewportStateDataObserver]
   * to indicate that it wishes to stop receiving updates. Following either of these events, implementations
   * must no longer invoke [ViewportStateDataObserver] and must release all strong references to it.
   *
   * @param viewportStateDataObserver observer that observe new viewport data.
   * @return a handle that cancels current observation.
   */
  fun observeDataSource(viewportStateDataObserver: ViewportStateDataObserver): Cancelable

  /**
   * Indicates the current [ViewportState] is now responsible for updating the camera.
   *
   * [ViewportPlugin] calls this method at the end of a successful transition into this state.
   */
  fun startUpdatingCamera()

  /**
   * Indicates the current [ViewportState] is no longer responsible for updating the camera.
   *
   * [ViewportPlugin] calls this method at the beginning of the transition out of this sate.
   *
   * Implementations must stop updating the camera immediately and should typically cancel any ongoing
   * animations that they started when this method is invoked.
   */
  fun stopUpdatingCamera()
}