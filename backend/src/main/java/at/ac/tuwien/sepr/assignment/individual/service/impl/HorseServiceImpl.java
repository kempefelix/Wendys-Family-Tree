package at.ac.tuwien.sepr.assignment.individual.service.impl;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
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
 * Implementation of {@link HorseService} for handling image storage and retrieval.
 */
@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;

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

  @Override
  public Stream<HorseListDto> allHorses() {
    LOG.trace("allHorses()");
    var horses = dao.getAll();
    var ownerIds = horses.stream()
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

  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    return mapper.entityToDetailDto(
        horse,
        ownerMapForSingleId(horse.ownerId())
    );
  }

  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    try {
      return ownerId == null
          ? null
          : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

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

  @Override
  public void delete(long id) throws NotFoundException {
    dao.delete(id);
  }
}