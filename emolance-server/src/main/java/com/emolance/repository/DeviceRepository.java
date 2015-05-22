package com.emolance.repository;

import com.emolance.domain.Device;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Device entity.
 */
public interface DeviceRepository extends JpaRepository<Device,Long> {

    @Query("select device from Device device where device.owner.login = ?#{principal.username}")
    List<Device> findAllForCurrentUser();

    List<Device> findBySn(String sn);
}
