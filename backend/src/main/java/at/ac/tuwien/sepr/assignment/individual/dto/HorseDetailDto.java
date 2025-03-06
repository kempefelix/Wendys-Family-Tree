package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

/**
 * Represents a Data Transfer Object (DTO) for detailed horse information.
 * This record provides all necessary details about a horse.
 */
public record HorseDetailDto(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    String image,
    OwnerDto owner
) {
}