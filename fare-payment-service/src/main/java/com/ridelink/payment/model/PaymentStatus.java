package com.ridelink.payment.model;

/**
 * Stored simulated-payment outcomes; new payments begin PENDING before an explicit status
 * update.
 */
public enum PaymentStatus {
    PENDING, SUCCESS, FAILED
}
