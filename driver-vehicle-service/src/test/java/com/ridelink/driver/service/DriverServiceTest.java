package com.ridelink.driver.service;

import com.ridelink.driver.dto.DriverProfileRequest;
import com.ridelink.driver.dto.LocationUpdateRequest;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DuplicateDriverProfileException;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.DriverStatus;
import com.ridelink.driver.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver existingDriver;

    @BeforeEach
    void setUp() {
        existingDriver = Driver.builder()
                .id("driver-1")
                .accountId("acc-1")
                .licenseNumber("LIC123")
                .vehicleMake("Toyota")
                .vehicleModel("Corolla")
                .vehiclePlateNumber("CAB-1234")
                .status(DriverStatus.UNAVAILABLE)
                .serviceArea("Colombo")
                .build();
    }

    @Test
    void registerProfile_savesNewDriver_whenAccountHasNoExistingProfile() {
        DriverProfileRequest request = new DriverProfileRequest();
        request.setAccountId("acc-1");
        request.setLicenseNumber("LIC123");
        request.setVehicleMake("Toyota");
        request.setVehicleModel("Corolla");
        request.setVehiclePlateNumber("CAB-1234");
        request.setServiceArea("Colombo");

        when(driverRepository.findByAccountId("acc-1")).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenReturn(existingDriver);

        Driver result = driverService.registerProfile(request);

        assertThat(result.getAccountId()).isEqualTo("acc-1");
        assertThat(result.getStatus()).isEqualTo(DriverStatus.UNAVAILABLE);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void registerProfile_throwsDuplicateException_whenAccountAlreadyHasProfile() {
        DriverProfileRequest request = new DriverProfileRequest();
        request.setAccountId("acc-1");

        when(driverRepository.findByAccountId("acc-1")).thenReturn(Optional.of(existingDriver));

        assertThatThrownBy(() -> driverService.registerProfile(request))
                .isInstanceOf(DuplicateDriverProfileException.class)
                .hasMessageContaining("already exists");

        verify(driverRepository, never()).save(any());
    }

    @Test
    void getById_returnsDriver_whenFound() {
        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(existingDriver));

        Driver result = driverService.getById("driver-1");

        assertThat(result.getId()).isEqualTo("driver-1");
    }

    @Test
    void getById_throwsNotFoundException_whenMissing() {
        when(driverRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.getById("missing-id"))
                .isInstanceOf(DriverNotFoundException.class)
                .hasMessageContaining("missing-id");
    }

    @Test
    void updateAvailability_changesStatus_andSaves() {
        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(existingDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        Driver result = driverService.updateAvailability("driver-1", DriverStatus.AVAILABLE);

        assertThat(result.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
        verify(driverRepository).save(existingDriver);
    }

    @Test
    void updateLocation_setsLatLong_andSaves() {
        LocationUpdateRequest request = new LocationUpdateRequest();
        request.setLatitude(6.9271);
        request.setLongitude(79.8612);

        when(driverRepository.findById("driver-1")).thenReturn(Optional.of(existingDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        Driver result = driverService.updateLocation("driver-1", request);

        assertThat(result.getCurrentLatitude()).isEqualTo(6.9271);
        assertThat(result.getCurrentLongitude()).isEqualTo(79.8612);
    }

    @Test
    void getEligibleAvailableDrivers_filtersByAreaWhenProvided() {
        when(driverRepository.findByStatusAndServiceArea(DriverStatus.AVAILABLE, "Colombo"))
                .thenReturn(List.of(existingDriver));

        List<Driver> result = driverService.getEligibleAvailableDrivers("Colombo");

        assertThat(result).hasSize(1);
        verify(driverRepository).findByStatusAndServiceArea(DriverStatus.AVAILABLE, "Colombo");
        verify(driverRepository, never()).findByStatus(any());
    }

    @Test
    void getEligibleAvailableDrivers_returnsAllAvailable_whenNoAreaGiven() {
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of(existingDriver));

        List<Driver> result = driverService.getEligibleAvailableDrivers(null);

        assertThat(result).hasSize(1);
        verify(driverRepository).findByStatus(DriverStatus.AVAILABLE);
    }
}