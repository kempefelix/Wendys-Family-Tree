package at.ac.tuwien.sepr.assignment.individual.service.impl;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;

/**
 * Implementation of {@link HorseService} that handles operations for managing horses.
 * This implementation supports listing, searching, retrieving, creating, updating, and deleting horses.
 */
@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;

  /**
   * Constructs a new HorseServiceImpl.
   *
   * @param dao the DAO for horse persistence operations
   * @param mapper the mapper for converting between entities and DTOs
   * @param validator the validator for horse update operations
   * @param ownerService the service for handling owner-related operations
   */
  @Autowired
  public HorseServiceImpl(HorseDao dao,
                          HorseMapper mapper,
                          HorseValidator validator,
                          OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  /**
   * Lists all horses stored in the system.
   *
   * @return a stream of {@link HorseListDto} for all horses
   */
  @Override
  public Stream<HorseListDto> allHorses() {
    LOG.trace("allHorses()");
    var horses = dao.getAll();
    Set<Long> ownerIds = horses.stream()
        .map(Horse::ownerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }
    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }

  /**
   * Updates an existing horse with the given update data.
   *
   * @param horse the update DTO containing the new data for the horse
   * @return the updated horse as a detailed DTO
   * @throws NotFoundException if the horse or a referenced entity is not found
   * @throws ValidationException if the provided update data is invalid
   * @throws ConflictException if the update data conflicts with existing system data
   */
  @Override
  public HorseDetailDto update(HorseUpdateDto horse) throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", horse);
    validator.validateForUpdate(horse);

    if (horse.parentFemaleId() != null) {
      Horse parentFemale = dao.getById(horse.parentFemaleId());
      if (!parentFemale.sex().equals(Sex.FEMALE)) {
        throw new ValidationException("The specified parent female must be of gender FEMALE.", Collections.emptyList());
      }
    }
    if (horse.parentMaleId() != null) {
      Horse parentMale = dao.getById(horse.parentMaleId());
      if (!parentMale.sex().equals(Sex.MALE)) {
        throw new ValidationException("The specified parent male must be of gender MALE.", Collections.emptyList());
      }
    }

    var updatedHorse = dao.update(horse);
    return mapper.entityToDetailDto(
        updatedHorse,
        ownerMapForSingleId(updatedHorse.ownerId())
    );
  }

  /**
   * Retrieves a detailed horse by its ID.
   *
   * @param id the ID of the horse to retrieve
   * @return the horse details as a {@link HorseDetailDto}
   * @throws NotFoundException if no horse with the given ID exists
   */
  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    return mapper.entityToDetailDto(
        horse,
        ownerMapForSingleId(horse.ownerId())
    );
  }

  /**
   * Helper method to obtain a singleton owner map for a given owner ID.
   *
   * @param ownerId the owner ID
   * @return a map with the owner ID as key and its DTO as value, or null if ownerId is null
   * @throws FatalException if the owner referenced by the horse is not found
   */
  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    try {
      return ownerId == null
          ? null
          : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

  /**
   * Creates a new horse based on the provided creation data.
   *
   * @param horseCreateDto the DTO containing horse creation data
   * @return the created horse as a detailed DTO
   * @throws ValidationException if the provided data is invalid
   * @throws ConflictException if there is a conflict (e.g., a referenced entity is missing)
   * @throws NotFoundException if a referenced entity (e.g., owner) is not found
   */
  @Override
  public HorseDetailDto create(HorseCreateDto horseCreateDto)
      throws ValidationException, ConflictException, NotFoundException {
    LOG.trace("create({})", horseCreateDto);
    
    if (horseCreateDto.parentFemaleId() != null) {
      Horse parentFemale = dao.getById(horseCreateDto.parentFemaleId());
      if (!parentFemale.sex().equals(Sex.FEMALE)) {
        throw new ValidationException("The specified parent female must be of gender FEMALE.", Collections.emptyList());
      }
    }
    if (horseCreateDto.parentMaleId() != null) {
      Horse parentMale = dao.getById(horseCreateDto.parentMaleId());
      if (!parentMale.sex().equals(Sex.MALE)) {
        throw new ValidationException("The specified parent male must be of gender MALE.", Collections.emptyList());
      }
    }
    
    Horse createdHorse = dao.create(horseCreateDto);
    
    return mapper.entityToDetailDto(
        createdHorse,
        ownerMapForSingleId(createdHorse.ownerId())
    );
  }

  /**
   * Deletes the horse with the given ID from the persistent data store.
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if no horse with the given ID exists in the persistent data store
   */
  @Override
  public void delete(long id) throws NotFoundException {
    dao.delete(id);
  }

  /**
   * Searches for horses that match the provided search criteria.
   *
   * <p>
   * The search criteria may include filters for name, description, birth date (older than a given date),
   * sex, and owner. When multiple criteria are provided, only horses matching all criteria are returned.
   * If no criteria are provided, all horses are listed.
   * </p>
   *
   * @param criteria the {@link HorseSearchDto} encapsulating the search parameters
   * @return a stream of {@link HorseListDto} objects representing the horses that match the criteria
   * @throws NotFoundException if no horses match the criteria or if a referenced entity is missing
   */
  @Override
  public Stream<HorseListDto> search(HorseSearchDto criteria) throws NotFoundException {
    List<Horse> horses = dao.search(criteria);
    Set<Long> ownerIds = horses.stream()
        .map(Horse::ownerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<Long, OwnerDto> ownerMap = ownerService.getAllById(ownerIds);
    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }  
}