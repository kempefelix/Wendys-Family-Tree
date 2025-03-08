package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

/**
 * Represents a Data Transfer Object (DTO) for creating a new horse entry.
 * This record encapsulates all necessary details for registering a horse.
 */
public record HorseCreateDto(
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    String image,
    Long ownerId,
    Long parentFemaleId,
    Long parentMaleId
) {
}