package at.ac.tuwien.sepr.assignment.individual.entity;

import java.time.LocalDate;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

/**
 * Represents a horse in the persistent data store.
 */
public record Horse(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    String image,
    Long ownerId
) {
}