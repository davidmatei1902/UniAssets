package io.github.davidmatei1902.uni_assets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean checkLogin(String username, String password) {
        String sql = "SELECT COUNT(*) FROM Utilizatori WHERE username = ? AND parola = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username, password);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> getTableData(String tableName) {
        if (!isValidTable(tableName)) throw new IllegalArgumentException("Acces interzis!");
        return jdbcTemplate.queryForList("SELECT * FROM " + tableName);
    }

    // Dotari per Facultate (JOIN + Parametru)
    public List<Map<String, Object>> getAssetsByFaculty(String facultyCode) {
        String sql = "SELECT f.NumeFacultate, d.NumeDepartament, s.NumeSala, dot.NumeDotare, sd.Cantitate " +
                "FROM Facultati f " +
                "JOIN Departament d ON f.FacultateID = d.FacultateID " +
                "JOIN SalaDepartament sdep ON d.DepartamentID = sdep.DepartamentID " +
                "JOIN Sali s ON sdep.SalaID = s.SalaID " +
                "JOIN SalaDotari sd ON s.SalaID = sd.SalaID " +
                "JOIN Dotari dot ON sd.DotareID = dot.DotareID " +
                "WHERE f.CodFacultate = ?";
        return jdbcTemplate.queryForList(sql, facultyCode);
    }

    // Sali peste Medie (SUBQUERY)
    public List<Map<String, Object>> getRoomsAboveAverageCapacity() {
        String sql = "SELECT NumeSala, Capacitate, TipSala FROM Sali " +
                "WHERE Capacitate > (SELECT AVG(Capacitate) FROM Sali)";
        return jdbcTemplate.queryForList(sql);
    }

    // Top Departamente (GROUP BY + HAVING)
    public List<Map<String, Object>> getTopEquippedDepartments() {
        String sql = "SELECT d.NumeDepartament, COUNT(sd.DotareID) as NrDotariDistinte, SUM(sd.Cantitate) as TotalObiecte " +
                "FROM Departament d " +
                "JOIN SalaDepartament sdep ON d.DepartamentID = sdep.DepartamentID " +
                "JOIN SalaDotari sd ON sdep.SalaID = sd.SalaID " +
                "GROUP BY d.NumeDepartament " +
                "HAVING SUM(sd.Cantitate) > 5";
        return jdbcTemplate.queryForList(sql);
    }

    // Locatie Dotari (LIKE + Parametru)
    public List<Map<String, Object>> getAssetLocationByName(String assetName) {
        String sql = "SELECT dot.NumeDotare, s.NumeSala, s.Etaj, sd.Cantitate " +
                "FROM Dotari dot " +
                "JOIN SalaDotari sd ON dot.DotareID = sd.DotareID " +
                "JOIN Sali s ON sd.SalaID = s.SalaID " +
                "WHERE dot.NumeDotare LIKE ?";
        return jdbcTemplate.queryForList(sql, "%" + assetName + "%");
    }

    // Analiza Stare Inventar (GROUP BY coloane multiple)
    public List<Map<String, Object>> getInventoryStatusAnalysis() {
        String sql = "SELECT TipDotare, Stare, COUNT(*) as NumarUnitati " +
                "FROM Dotari " +
                "GROUP BY TipDotare, Stare " +
                "ORDER BY TipDotare";
        return jdbcTemplate.queryForList(sql);
    }

    public List<String> getTableColumns(String tableName) {
        if (!isValidTable(tableName)) return new ArrayList<>();
        return jdbcTemplate.queryForList("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?", String.class, tableName);
    }

    public void insertRecord(String tableName, Map<String, String> formData) {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (entry.getKey().toLowerCase().contains("id") || entry.getKey().equals("tableName")) continue;
            columns.add(entry.getKey());
            values.add("?");
            params.add(entry.getValue());
        }
        jdbcTemplate.update("INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES (" + String.join(", ", values) + ")", params.toArray());
    }

    public void deleteByBusinessName(String tableName, String identifierValue) {
        String nameColumn;
        switch (tableName.toLowerCase()) {
            case "facultati": nameColumn = "NumeFacultate"; break;
            case "departament": nameColumn = "NumeDepartament"; break;
            case "sali": nameColumn = "NumeSala"; break;
            case "dotari": nameColumn = "NumeDotare"; break;
            default: nameColumn = "Nume";
        }
        jdbcTemplate.update("DELETE FROM " + tableName + " WHERE " + nameColumn + " = ?", identifierValue);
    }

    private boolean isValidTable(String tableName) {
        return Arrays.asList("Dotari", "Sali", "Facultati", "Departament", "Caracteristici", "SalaDotari").stream()
                .anyMatch(t -> t.equalsIgnoreCase(tableName));
    }
}