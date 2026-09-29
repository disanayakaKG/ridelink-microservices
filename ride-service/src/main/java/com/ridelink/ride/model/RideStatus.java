package com.ridelink.ride.model;

/**
 * Allowed ride lifecycle states.
 *
 * <pre>
 * REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED
 *     │           │          │            │
 *     └───────────┴──────────┴────────────┴──→ CANCELLED
 * </pre>
 */
public enum RideStatus {
	REQUESTED,
	ASSIGNED,
	ACCEPTED,
	IN_PROGRESS,
	COMPLETED,
	CANCELLED
}
