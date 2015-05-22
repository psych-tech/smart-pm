package com.emolance.repository;

import com.emolance.domain.Report;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Report entity.
 */
public interface ReportRepository extends JpaRepository<Report,Long> {

    @Query("select report from Report report where report.userId.login = ?#{principal.username}")
    List<Report> findAllForCurrentUser();

}
