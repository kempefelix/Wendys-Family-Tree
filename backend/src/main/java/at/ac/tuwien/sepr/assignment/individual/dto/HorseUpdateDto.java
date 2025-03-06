package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

/**
 * Represents a Data Transfer Object (DTO) for updating horse details.
 * This record encapsulates all necessary fields for updating a horse entry.
 */
public record HorseUpdateDto(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    String image,
    Long ownerId
) {
}