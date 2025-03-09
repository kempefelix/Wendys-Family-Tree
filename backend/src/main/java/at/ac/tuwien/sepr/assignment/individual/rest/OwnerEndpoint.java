package at.ac.tuwien.sepr.assignment.individual.rest;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;

/**
 * REST controller for managing owner-related operations.
 * Provides endpoints for searching and creating owners.
 */
@RestController
@RequestMapping(OwnerEndpoint.BASE_PATH)
public class OwnerEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/owners";

  private final OwnerService service;

  public OwnerEndpoint(OwnerService service) {
    this.service = service;
  }

  /**
   * Searches for owners based on the given search parameters.
   *
   * @param searchParameters the parameters to filter the owner search
   * @return a stream of {@link OwnerDto} matching the search criteria
   */
  @GetMapping
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH + " query parameters: {}", searchParameters);
    return service.search(searchParameters);
  }

  /**
   * Creates a new owner in the system.
   *
   * @param dto the DTO containing the new owner's data
   * @return the created owner as a {@link OwnerDto}
   * @throws ResponseStatusException if owner creation fails due to invalid input or other errors
   */
  @PostMapping
  public OwnerDto createOwner(@RequestBody OwnerCreateDto dto) {
    LOG.info("POST " + BASE_PATH);
    try {
      return service.create(dto);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Deletes the owner with the specified ID from the system.
   *
   * @param id the ID of the owner to delete
   * @throws ResponseStatusException if no owner with the given ID exists
   */
  @DeleteMapping("{id}")
  public void deleteOwner(@PathVariable("id") long id) {
    try {
      service.delete(id);
    } catch (NotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }
}
