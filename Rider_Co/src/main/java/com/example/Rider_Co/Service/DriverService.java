package com.example.Rider_Co.Service;

import com.example.Rider_Co.Model.Driver;
import com.example.Rider_Co.Model.Ride;
import com.example.Rider_Co.Repository.DriverRepository;
import com.example.Rider_Co.Repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DriverService {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RideRepository rideRepository;

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    /**
     * Retrieves all drivers from the database.
     * @return List of all drivers.
     */
    public List<Driver> GetAllDrivers() {
        logger.info("Fetching all drivers from the database");
        return driverRepository.findAll();
    }

    /**
     * Retrieves a driver by their ID.
     * @param driverId ID of the driver.
     * @return Driver object or an empty Driver if not found.
     */
    public Driver GetDriverByID(int driverId) {
        if (driverRepository.existsById(driverId)) {
            logger.info("Fetching driver with ID: {}", driverId);
            return driverRepository.findById(driverId).orElse(new Driver());
        } else {
            logger.warn("Driver with ID: {} does not exist", driverId);
            return new Driver();
        }
    }

    /**
     * Adds a new driver to the system.
     * @param driver Driver object to be added.
     * @return Success message.
     */
    public String AddDriver(Driver driver) {
        driverRepository.save(driver);
        logger.info("Driver with ID: {} added successfully", driver.getDriverId());
        return "Successfully added the driver with ID: " + driver.getDriverId();
    }

    /**
     * Updates an existing driver's details.
     * @param driver Driver object with updated details.
     * @return Success or failure message.
     */
    public String UpdateDriver(Driver driver) {
        if (driverRepository.existsById(driver.getDriverId())) {
            driverRepository.save(driver);
            logger.info("Driver with ID: {} updated successfully", driver.getDriverId());
            return "Successfully updated the driver with ID: " + driver.getDriverId();
        } else {
            logger.warn("Driver with ID: {} does not exist and cannot be updated", driver.getDriverId());
            return "Driver with ID: " + driver.getDriverId() + " does not exist.";
        }
    }

    /**
     * Deletes a driver by their ID.
     * @param driverId ID of the driver to be deleted.
     * @return Success or failure message.
     */
    public String DeleteDriver(int driverId) {
        if (driverRepository.existsById(driverId)) {
            driverRepository.deleteById(driverId);
            logger.info("Driver with ID: {} deleted successfully", driverId);
            return "Successfully deleted the driver with ID: " + driverId;
        } else {
            logger.warn("Driver with ID: {} does not exist and cannot be deleted", driverId);
            return "Driver with ID: " + driverId + " does not exist.";
        }
    }

    /**
     * Assigns a ride to a driver.
     * @param driverId ID of the driver.
     * @param rideId ID of the ride.
     * @return Status message of ride acceptance.
     */
    public String AcceptRide(int driverId, int rideId) {
        Ride selectedRide = rideRepository.findById(rideId).orElse(null);

        if (selectedRide == null) {
            logger.warn("Ride with ID: {} is already assigned or does not exist", rideId);
            return "Ride is already assigned to someone else.";
        }

        selectedRide.setDriverId(driverId);
        rideRepository.save(selectedRide);
        logger.info("Ride with ID: {} assigned to driver ID: {}", rideId, driverId);

        // Mark driver as unavailable after accepting a ride
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver != null) {
            driver.setAvailable(false);
            driverRepository.save(driver);
            logger.info("Driver with ID: {} is now unavailable", driverId);
        }

        return "RIDE_ACCEPTED " + rideId;
    }

    /**
     * Retrieves a list of available rides for a specific driver, sorted by distance.
     * @param driverId ID of the driver.
     * @return List of available rides sorted by distance.
     */
    public List<Ride> getAvailableRidesForDriver(int driverId) {
        Driver driver = driverRepository.findById(driverId).orElse(null);

        if (driver == null) {
            logger.warn("Driver with ID: {} does not exist", driverId);
            return new ArrayList<>();
        }

        if (!driver.isAvailable()) {
            logger.info("Driver with ID: {} is not available for new rides", driverId);
            return new ArrayList<>();
        }

        double driverX = driver.getX();
        double driverY = driver.getY();

        List<Ride> availableRides = rideRepository.findAllUnassignedRide();

        // Sort rides based on the distance from the driver
        availableRides.sort((r1, r2) -> {
            double d1 = calculateDistance(driverX, driverY, r1.getStartX(), r1.getStartY());
            double d2 = calculateDistance(driverX, driverY, r2.getStartX(), r2.getStartY());
            return Double.compare(d1, d2);
        });

        logger.info("Returning sorted available rides for driver ID: {}", driverId);
        return availableRides;
    }

    /**
     * Calculates the Euclidean distance between two points.
     * @param x1 X-coordinate of the first point.
     * @param y1 Y-coordinate of the first point.
     * @param x2 X-coordinate of the second point.
     * @param y2 Y-coordinate of the second point.
     * @return Distance between the two points.
     */
    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
