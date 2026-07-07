package org.project.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.carsharingapp.dto.rental.RentalRequestDto;
import org.project.carsharingapp.dto.rental.RentalResponseDto;
import org.project.carsharingapp.security.annotation.CustomerOnly;
import org.project.carsharingapp.security.annotation.ManagerOrCustomer;
import org.project.carsharingapp.service.RentalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users' car rentals management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;

    @Operation(summary = "Create a new rental")
    @ResponseStatus(HttpStatus.CREATED)
    @CustomerOnly
    @PostMapping
    public RentalResponseDto createRental(@Valid @RequestBody RentalRequestDto requestDto) {
        return rentalService.createRental(requestDto);
    }

    @Operation(summary = "Get rentals by user id and active status")
    @ManagerOrCustomer
    @GetMapping
    public Page<RentalResponseDto> getAll(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "is_active", required = false) Boolean isActive,
            Pageable pageable
    ) {
        return rentalService.findAll(userId, isActive, pageable);
    }

    @Operation(summary = "Get a rental by id")
    @ManagerOrCustomer
    @GetMapping("/{id}")
    public RentalResponseDto getById(@PathVariable Long id) {
        return rentalService.findById(id);
    }

    @Operation(summary = "Return a rental")
    @ManagerOrCustomer
    @PostMapping("/{id}/return")
    public RentalResponseDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }

}
