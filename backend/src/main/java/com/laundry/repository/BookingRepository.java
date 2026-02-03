package com.laundry.repository;

import com.laundry.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT b FROM Booking b WHERE b.startTime >= :start AND b.endTime <= :end")
    List<Booking> findBookingsInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT b FROM Booking b WHERE b.startTime >= :now AND b.startTime < :tomorrow")
    List<Booking> findTodayBookings(@Param("now") LocalDateTime now, @Param("tomorrow") LocalDateTime tomorrow);
    
    @Query("SELECT b FROM Booking b WHERE b.startTime >= :weekStart AND b.startTime < :weekEnd")
    List<Booking> findWeekBookings(@Param("weekStart") LocalDateTime weekStart, @Param("weekEnd") LocalDateTime weekEnd);
    
    @Query("SELECT b FROM Booking b WHERE " +
           "(b.startTime < :endTime AND b.endTime > :startTime)")
    List<Booking> findOverlappingBookings(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
}

