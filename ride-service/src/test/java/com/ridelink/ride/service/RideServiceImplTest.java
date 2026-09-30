package com.ridelink.ride.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.PaymentServiceClient;
import com.ridelink.ride.dto.AssignDriverRequest;
import com.ridelink.ride.dto.CancelRideRequest;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.exception.InvalidRideStateException;
import com.ridelink.ride.exception.NoAvailableDriverException;
import com.ridelink.ride.exception.ResourceNotFoundException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.service.impl.RideServiceImpl;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {

	@Mock
	private RideRepository rideRepository;

	@Mock
	private DriverServiceClient driverServiceClient;

	@Mock
	private PaymentServiceClient paymentServiceClient;

	@InjectMocks
	private RideServiceImpl rideService;

	private Ride requestedRide;

	@BeforeEach
	void setUp() {
		requestedRide = Ride.builder()
				.id("mongo1")
				.rideId("RIDE001")
				.passengerId("PASS001")
				.pickupLocation("Colombo Fort")
				.destinationLocation("Kandy")
				.distanceKm(10.0)
				.status(RideStatus.REQUESTED)
				.estimatedFare(950.0)
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();
	}

	@Test
	void createRide_success() {
		CreateRideRequest request = CreateRideRequest.builder()
				.passengerId("PASS001")
				.pickupLocation("Colombo Fort")
				.destinationLocation("Kandy")
				.distanceKm(10.0)
				.build();

		when(paymentServiceClient.estimateFare(10.0)).thenReturn(950.0);
		when(rideRepository.existsByRideId(anyString())).thenReturn(false);
		when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
			Ride r = inv.getArgument(0);
			r.setId("mongo1");
			return r;
		});

		RideResponse response = rideService.createRide(request);

		assertThat(response.getPassengerId()).isEqualTo("PASS001");
		assertThat(response.getStatus()).isEqualTo(RideStatus.REQUESTED);
		assertThat(response.getEstimatedFare()).isEqualTo(950.0);
		assertThat(response.getRideId()).startsWith("RIDE");
		verify(rideRepository).save(any(Ride.class));
	}

	@Test
	void getByRideId_notFound() {
		when(rideRepository.findByRideId("MISSING")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> rideService.getByRideId("MISSING"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("MISSING");
	}

	@Test
	void assignDriver_autoSelect_success() {
		when(rideRepository.findByRideId("RIDE001")).thenReturn(Optional.of(requestedRide));
		when(driverServiceClient.getAvailableDriverIds()).thenReturn(List.of("DRV001", "DRV002"));
		when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

		RideResponse response = rideService.assignDriver("RIDE001", new AssignDriverRequest());

		assertThat(response.getStatus()).isEqualTo(RideStatus.ASSIGNED);
		assertThat(response.getDriverId()).isEqualTo("DRV001");
		assertThat(response.getAssignedAt()).isNotNull();
	}

	@Test
	void assignDriver_noAvailableDriver() {
		when(rideRepository.findByRideId("RIDE001")).thenReturn(Optional.of(requestedRide));
		when(driverServiceClient.getAvailableDriverIds()).thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> rideService.assignDriver("RIDE001", new AssignDriverRequest()))
				.isInstanceOf(NoAvailableDriverException.class);
	}

	@Test
	void assignDriver_invalidStatus() {
		requestedRide.setStatus(RideStatus.COMPLETED);
		when(rideRepository.findByRideId("RIDE001")).thenReturn(Optional.of(requestedRide));

		assertThatThrownBy(() -> rideService.assignDriver("RIDE001", new AssignDriverRequest()))
				.isInstanceOf(InvalidRideStateException.class)
				.hasMessageContaining("COMPLETED");
	}

	@Test
	void acceptRide_success() {
		requestedRide.setStatus(RideStatus.ASSIGNED);
		requestedRide.setDriverId("DRV001");
		when(rideRepository.findByRideId("RIDE001")).thenReturn(Optional.of(requestedRide));
		when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

		RideResponse response = rideService.acceptRide("RIDE001");

		assertThat(response.getStatus()).isEqualTo(RideStatus.ACCEPTED);
		assertThat(response.getAcceptedAt()).isNotNull();
	}

	@Test
	void startRide_success() {
		requestedRide.setStatus(RideStatus.ACCEPTED);
		requestedRide.setDriverId("DRV001");
		when(rideRepository.findByRideId("RIDE001")).thenReturn(Optional.of(requestedRide));
		when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

		RideResponse response = rideService.startRide("RIDE001");

		assertThat(response.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);
		assertThat(response.getStartedAt()).isNotNull();
	}

	@Test
	void completeRide_success() {
		requestedRide.setStatus(RideStatus.IN_PROGRESS);
		requestedRide.setDriverId("DRV001");
		when(rideRepository.findByRideId("RIDE001")).thenReturn(Optional.of(requestedRide));
		when(paymentServiceClient.estimateFare(10.0)).thenReturn(950.0);
		when(paymentServiceClient.createPayment(eq("RIDE001"), eq("PASS001"), eq(950.0)))
				.thenReturn("pay123");
		when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

		RideResponse response = rideService.completeRide("RIDE001");

		assertThat(response.getStatus()).isEqualTo(RideStatus.COMPLETED);
		assertThat(response.getFinalFare()).isEqualTo(950.0);
		assertThat(response.getPaymentId()).isEqualTo("pay123");
		assertThat(response.getCompletedAt()).isNotNull();
	}

	@Test
	void cancelRide_fromRequested_success() {
		when(rideRepository.findByRideId("RIDE001")).thenReturn(Optional.of(requestedRide));
		when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

		CancelRideRequest cancel = CancelRideRequest.builder().reason("Changed plans").build();
		RideResponse response = rideService.cancelRide("RIDE001", cancel);

		assertThat(response.getStatus()).isEqualTo(RideStatus.CANCELLED);
		assertThat(response.getCancellationReason()).isEqualTo("Changed plans");
		assertThat(response.getCancelledAt()).isNotNull();
	}

	@Test
	void cancelRide_fromCompleted_fails() {
		requestedRide.setStatus(RideStatus.COMPLETED);
		when(rideRepository.findByRideId("RIDE001")).thenReturn(Optional.of(requestedRide));

		CancelRideRequest cancel = CancelRideRequest.builder().reason("Too late").build();

		assertThatThrownBy(() -> rideService.cancelRide("RIDE001", cancel))
				.isInstanceOf(InvalidRideStateException.class)
				.hasMessageContaining("COMPLETED");
		verify(rideRepository, never()).save(any());
	}

	@Test
	void assignDriver_explicitDriver_success() {
		when(rideRepository.findByRideId("RIDE001")).thenReturn(Optional.of(requestedRide));
		when(driverServiceClient.isDriverAvailable("DRV099")).thenReturn(true);
		when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

		AssignDriverRequest req = AssignDriverRequest.builder().driverId("DRV099").build();
		RideResponse response = rideService.assignDriver("RIDE001", req);

		assertThat(response.getDriverId()).isEqualTo("DRV099");
		assertThat(response.getStatus()).isEqualTo(RideStatus.ASSIGNED);

		ArgumentCaptor<Ride> captor = ArgumentCaptor.forClass(Ride.class);
		verify(rideRepository).save(captor.capture());
		assertThat(captor.getValue().getDriverId()).isEqualTo("DRV099");
	}
}
