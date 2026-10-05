package com.ridelink.ride.model;

/**
 * Allowed ride lifecycle states and state transition flow.
 *
 * <pre>
 * REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED
 *     │           │          │            │
 *     └───────────┴──────────┴────────────┴──→ CANCELLED
 * </pre>
 */
public enum RideStatus {
	// Standard progression states (REQUESTED to IN_PROGRESS) and terminal states (COMPLETED, CANCELLED)
	REQUESTED,
	ASSIGNED,
	ACCEPTED,
	IN_PROGRESS,
	COMPLETED,
	CANCELLED
}
