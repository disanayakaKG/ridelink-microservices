package com.ridelink.ride.model;

/**
 * Ride lifecycle states: REQUESTED, ASSIGNED, ACCEPTED, IN_PROGRESS and COMPLETED. Cancellation
 * is allowed only from REQUESTED, ASSIGNED or ACCEPTED; RideServiceImpl enforces these
 * transitions.
 */
public enum RideStatus {
	REQUESTED,
	ASSIGNED,
	ACCEPTED,
	IN_PROGRESS,
	COMPLETED,
	CANCELLED
}
