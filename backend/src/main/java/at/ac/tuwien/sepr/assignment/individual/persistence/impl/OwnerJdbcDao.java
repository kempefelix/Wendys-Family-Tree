package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.OwnerDao;

/**
 * JDBC implementation of {@link OwnerDao} for interacting with the database.
 */
@Repository
public class OwnerJdbcDao implements OwnerDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "owner";
  private static final String SQL_SELECT_BY_ID =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE id = :id";

  private static final String SQL_SELECT_ALL =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE id IN (:ids)";

  private static final String SQL_SELECT_SEARCH =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE UPPER(first_name || ' ' || last_name) LIKE UPPER('%%' || COALESCE(:name, '') || '%%')";

  private static final String SQL_SELECT_SEARCH_LIMIT_CLAUSE = " LIMIT :limit";


  private final JdbcClient jdbcClient;

  @Autowired
  public OwnerJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public Owner getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Owner> owners = jdbcClient
        .sql(SQL_SELECT_BY_ID)
        .param("id", id)
        .query(this::mapRow)
        .list();
    if (owners.isEmpty()) {
      throw new NotFoundException("Owner with ID %d not found".formatted(id));
    }
    if (owners.size() > 1) {
      // If this happens, something is wrong with either the DB or the select
      throw new FatalException("Found more than one owner with ID %d".formatted(id));
    }
    return owners.getFirst();
  }


  @Override
  public Collection<Owner> getAllById(Collection<Long> ids) {
    LOG.trace("getAllById({})", ids);
    return jdbcClient
        .sql(SQL_SELECT_ALL)
        .param("ids", ids)
        .query(this::mapRow)
        .list();
  }

  @Override
  public Collection<Owner> search(OwnerSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    var query = SQL_SELECT_SEARCH;

    Map<String, Object> params = new HashMap<>();
    params.put("name", searchParameters.name());

    var maxAmount = searchParameters.maxAmount();
    if (maxAmount != null) {
      query += SQL_SELECT_SEARCH_LIMIT_CLAUSE;
      params.put("limit", maxAmount);
    }

    return jdbcClient
        .sql(query)
        .params(params)
        .query(this::mapRow)
        .list();
  }

  private Owner mapRow(ResultSet resultSet, int i) throws SQLException {
    return new Owner(
        resultSet.getLong("id"),
        resultSet.getString("first_name"),
        resultSet.getString("last_name"),
        resultSet.getString("email"),
        resultSet.getString("description")
        );
  }

  private static final String SQL_INSERT =
      "INSERT INTO " + TABLE_NAME + " (first_name, last_name, email, description) " 
      +
      "VALUES (:firstName, :lastName, :email, :description)";

  @Override
  public Owner create(OwnerCreateDto dto) {
    int updated = jdbcClient
        .sql(SQL_INSERT)
        .param("firstName", dto.firstName())
        .param("lastName", dto.lastName())
        .param("email", dto.email())
        .param("description", dto.description())
        .update();
    if (updated == 0) {
      throw new RuntimeException("Failed to create owner");
    }
    long generatedId = 0; 
    return new Owner(generatedId, dto.firstName(), dto.lastName(), dto.email(), dto.description());
  }

  private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE id = :id";

  @Override
  public void delete(long id) throws NotFoundException {
    int affected = jdbcClient
        .sql(SQL_DELETE)
        .param("id", id)
        .update();
    if (affected == 0) {
      throw new NotFoundException("Owner with ID " + id + " not found for deletion.");
    }
  }  
}
