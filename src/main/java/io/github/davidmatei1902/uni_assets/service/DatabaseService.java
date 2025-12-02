package io.github.davidmatei1902.uni_assets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Test database connectivity
    public boolean testConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Verify user credentials against the database
    public boolean checkLogin(String username, String password) {
        String sql = "SELECT COUNT(*) FROM Utilizatori WHERE username = ? AND parola = ?";
        // Spring handles parameter replacement to prevent SQL Injection
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username, password);
        return count != null && count > 0;
    }

    // Fetch all rows from a specific table
    public List<Map<String, Object>> getTable(String tableName) {
        // Validate table name to prevent SQL Injection
        if (!isValidTable(tableName)) throw new IllegalArgumentException("Invalid table!");

        String sql = "SELECT * FROM " + tableName;
        return jdbcTemplate.queryForList(sql);
    }

    // Retrieve column names for a specific table
    public List<String> getTableColumns(String tableName) {
        if (!isValidTable(tableName)) throw new IllegalArgumentException("Invalid table!");

        // Query metadata to get column names
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?";
        return jdbcTemplate.queryForList(sql, String.class, tableName);
    }

    // Insert a new row dynamically
    public void insertRow(String tableName, Map<String, String> formData) {
        if (!isValidTable(tableName)) throw new IllegalArgumentException("Invalid table!");

        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String colName = entry.getKey();
            String val = entry.getValue();

            // Skip ID columns or empty values
            if (colName.equalsIgnoreCase("id") || colName.toLowerCase().endsWith("id")) continue;

            columns.add(colName);
            values.add("?"); // Placeholder for PreparedStatement
            params.add(val);
        }

        String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") " +
                "VALUES (" + String.join(", ", values) + ")";

        jdbcTemplate.update(sql, params.toArray());
    }

    // Delete a row by ID
    public int deleteRow(String tableName, int id) {
        // Validate table name
        List<String> allowedTables = Arrays.asList("Dotari", "Sali", "Facultati", "Departament", "Caracteristici", "SalaDotari");
        if (allowedTables.stream().noneMatch(t -> t.equalsIgnoreCase(tableName))) {
            throw new IllegalArgumentException("Invalid table!");
        }

        // Determine the ID column name based on table conventions
        String idColumn = tableName + "ID";
        if (tableName.equalsIgnoreCase("Dotari")) idColumn = "DotareID";
        else if (tableName.equalsIgnoreCase("Sali")) idColumn = "SalaID";
        else if (tableName.equalsIgnoreCase("Facultati")) idColumn = "FacultateID";
        // Add other exceptions here if necessary

        // Execute deletion
        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);

        // Check if the operation was successful
        if (rowsAffected == 0) {
            throw new RuntimeException("No record found with ID " + id + " in table " + tableName + "!");
        }

        return rowsAffected;
    }

    // Whitelist allowed tables for security reasons
    private boolean isValidTable(String tableName) {
        List<String> allowed = Arrays.asList("Dotari", "Sali", "Facultati", "Departament", "Caracteristici", "Utilizatori");
        return allowed.stream().anyMatch(t -> t.equalsIgnoreCase(tableName));
    }
}