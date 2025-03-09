package at.ac.tuwien.sepr.assignment.individual.persistence;


import java.util.List;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {
  /**
   * Get all horses stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  List<Horse> getAll();


  /**
   * Update the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(HorseUpdateDto horse) throws NotFoundException;


  /**
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  
  /**
   * Creates a new horse in the persistent data store based on the given DTO.
   *
   * @param horseCreateDto the DTO with the horse creation data
   * @return the created Horse with its generated ID
   * @throws NotFoundException if a related entity (e.g., owner) is not found
   */
  Horse create(HorseCreateDto horseCreateDto) throws NotFoundException;

  /**
   * Deletes the horse with the specified ID from the persistent data store.
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if no horse with the given ID exists in the persistent data store
   */
  void delete(long id) throws NotFoundException;

  /**
   * Searches for horses that match the provided criteria.
   *
   * <p>
   * The search criteria may include filters for name, description, birth date (older than a given date),
   * sex, and owner. When multiple criteria are provided, only horses matching all criteria are returned.
   * If no criteria are provided, all horses are listed.
   * </p>
   *
   * @param criteria the {@link HorseSearchDto} encapsulating the search parameters
   * @return a list of horses matching the criteria
   * @throws NotFoundException if no horses match the criteria or if a referenced entity is missing
   */
  List<Horse> search(HorseSearchDto criteria) throws NotFoundException;
}
