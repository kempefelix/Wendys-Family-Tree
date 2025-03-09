package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;

/**
 * JDBC implementation of {@link HorseDao} for interacting with the database.
 */
@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";

  private static final String SQL_SELECT_ALL =
      "SELECT * FROM " + TABLE_NAME;

  private static final String SQL_SELECT_BY_ID =
      "SELECT * FROM " + TABLE_NAME + " WHERE id = :id";

  private static final String SQL_UPDATE =
      "UPDATE " + TABLE_NAME + " " 
      + 
      """
      SET name = :name,
          description = :description,
          date_of_birth = :date_of_birth,
          sex = :sex,
          image = :image,
          owner_id = :owner_id,
          parent_female_id = :parent_female_id,
          parent_male_id = :parent_male_id
      WHERE id = :id
      """;
  

  private final JdbcClient jdbcClient;

  @Autowired
  public HorseJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    return jdbcClient
        .sql(SQL_SELECT_ALL)
        .query(this::mapRow)
        .list();
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses = jdbcClient
        .sql(SQL_SELECT_BY_ID)
        .param("id", id)
        .query(this::mapRow)
        .list();

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.getFirst();
  }

  @Override
  public Horse update(HorseUpdateDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcClient
        .sql(SQL_UPDATE)
        .param("id", horse.id())
        .param("name", horse.name())
        .param("description", horse.description())
        .param("date_of_birth", horse.dateOfBirth())
        .param("sex", horse.sex().toString())
        .param("image", horse.image())
        .param("owner_id", horse.ownerId())
        .param("parent_female_id", horse.parentFemaleId())
        .param("parent_male_id", horse.parentMaleId())
        .update();

    if (updated == 0) {
      throw new NotFoundException(
          "Could not update horse with ID " + horse.id() + ", because it does not exist"
      );
    }

    return new Horse(
        horse.id(),
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex(),
        horse.image(),
        horse.ownerId(),
        horse.parentFemaleId(),
        horse.parentMaleId()
    );
  }

  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    return new Horse(
        result.getLong("id"),
        result.getString("name"),
        result.getString("description"),
        result.getDate("date_of_birth").toLocalDate(),
        Sex.valueOf(result.getString("sex")),
        result.getString("image"),
        result.getObject("owner_id", Long.class),
        result.getObject("parent_female_id", Long.class),
        result.getObject("parent_male_id", Long.class)
    );
  }

  @Override
  public Horse create(HorseCreateDto horseCreateDto) throws NotFoundException {
    String sqlInsert = "INSERT INTO " + TABLE_NAME 
                       +
                       " (name, description, date_of_birth, sex, image, owner_id, parent_female_id, parent_male_id) " 
                       +
                       "VALUES (:name, :description, :date_of_birth, :sex, :image, :owner_id, :parent_female_id, :parent_male_id)";
    
    int updated = jdbcClient
        .sql(sqlInsert)
        .param("name", horseCreateDto.name())
        .param("description", horseCreateDto.description())
        .param("date_of_birth", horseCreateDto.dateOfBirth())
        .param("sex", horseCreateDto.sex().toString())
        .param("image", horseCreateDto.image())
        .param("owner_id", horseCreateDto.ownerId())
        .param("parent_female_id", horseCreateDto.parentFemaleId())
        .param("parent_male_id", horseCreateDto.parentMaleId())
        .update();
    
    if (updated == 0) {
      throw new NotFoundException("Failed to create horse. No rows affected.");
    }
    
    long generatedId = 0; // Dummy Value
    
    return new Horse(
        generatedId,
        horseCreateDto.name(),
        horseCreateDto.description(),
        horseCreateDto.dateOfBirth(),
        horseCreateDto.sex(),
        horseCreateDto.image(),
        horseCreateDto.ownerId(),
        horseCreateDto.parentFemaleId(),
        horseCreateDto.parentMaleId()
    );
  }

  @Override
  public void delete(long id) throws NotFoundException {
    String sqlDelete = "DELETE FROM " + TABLE_NAME + " WHERE id = :id";
    int affectedRows = jdbcClient
        .sql(sqlDelete)
        .param("id", id)
        .update();
    if (affectedRows == 0) {
      throw new NotFoundException("No horse with ID " + id + " found for deletion.");
    }
  }

  @Override
  public List<Horse> search(HorseSearchDto criteria) {
    StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE 1=1");
    Map<String, Object> params = new HashMap<>();

    if (criteria.name() != null && !criteria.name().isBlank()) {
      sql.append(" AND LOWER(name) LIKE :name");
      params.put("name", "%" + criteria.name().toLowerCase() + "%");
    }
    if (criteria.description() != null && !criteria.description().isBlank()) {
      sql.append(" AND LOWER(description) LIKE :description");
      params.put("description", "%" + criteria.description().toLowerCase() + "%");
    }
    if (criteria.bornBefore() != null) {
      sql.append(" AND date_of_birth < :bornBefore");
      params.put("bornBefore", criteria.bornBefore());
    }
    if (criteria.sex() != null) {
      sql.append(" AND sex = :sex");
      params.put("sex", criteria.sex().toString());
    }
    if (criteria.ownerName() != null && !criteria.ownerName().isBlank()) {
      sql.append(" AND owner_id IN (SELECT id FROM owner WHERE LOWER(first_name || ' ' || last_name) LIKE :ownerName)");
      params.put("ownerName", "%" + criteria.ownerName().toLowerCase() + "%");
    }
    if (criteria.limit() != null) {
      sql.append(" LIMIT :limit");
      params.put("limit", criteria.limit());
    }

    LOG.debug("Executing search query: {} with params: {}", sql, params);
    return jdbcClient
        .sql(sql.toString())
        .params(params)
        .query(this::mapRow)
        .list();
  }
}