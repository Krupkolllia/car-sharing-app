package org.project.carsharingapp.service;

import org.project.carsharingapp.dto.rental.RentalRequestDto;
import org.project.carsharingapp.dto.rental.RentalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalResponseDto createRental(RentalRequestDto requestDto);

    Page<RentalResponseDto> findAll(Long userId, Boolean isActive, Pageable pageable);

    RentalResponseDto findById(Long id);

    RentalResponseDto returnRental(Long id);

}
