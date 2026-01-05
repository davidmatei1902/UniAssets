/** Clasa Service pentru operatiile de manipulare a datelor si logica de procesare
 * @author David Matei
 * @version 5 Ianuarie 2026
 */
package io.github.davidmatei1902.uni_assets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean checkLogin(String username, String password) {
        String sql = "SELECT COUNT(*) FROM Utilizatori WHERE username = ? AND parola = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username, password);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> getSortedTableData(String tableName) {
        if (!isValidTable(tableName)) throw new IllegalArgumentException("Acces interzis!");
        List<Map<String, Object>> data = jdbcTemplate.queryForList("SELECT * FROM " + tableName);

        String nameCol = getNameColumn(tableName);

        return data.stream()
                .sorted((m1, m2) -> {
                    String v1 = m1.get(nameCol) != null ? m1.get(nameCol).toString() : "";
                    String v2 = m2.get(nameCol) != null ? m2.get(nameCol).toString() : "";
                    return v1.compareToIgnoreCase(v2);
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAssetsByFaculty(String facultyCode) {
        String sql = "SELECT f.NumeFacultate, d.NumeDepartament, s.NumeSala, dot.NumeDotare, sd.Cantitate " +
                "FROM Facultati f JOIN Departament d ON f.FacultateID = d.FacultateID " +
                "JOIN SalaDepartament sdep ON d.DepartamentID = sdep.DepartamentID " +
                "JOIN Sali s ON sdep.SalaID = s.SalaID " +
                "JOIN SalaDotari sd ON s.SalaID = sd.SalaID " +
                "JOIN Dotari dot ON sd.DotareID = dot.DotareID " +
                "WHERE f.FacultateID = (SELECT FacultateID FROM Facultati WHERE CodFacultate = ?)";
        return jdbcTemplate.queryForList(sql, facultyCode);
    }

    public List<Map<String, Object>> getRoomsAboveAverageCapacity() {
        String sql = "SELECT NumeSala, Capacitate, TipSala FROM Sali " +
                "WHERE Capacitate > (SELECT AVG(Capacitate) FROM Sali)";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getTopEquippedDepartments() {
        String sql = "SELECT NumeDepartament FROM Departament " +
                "WHERE DepartamentID IN (SELECT DepartamentID FROM SalaDepartament WHERE SalaID IN " +
                "(SELECT SalaID FROM SalaDotari GROUP BY SalaID HAVING SUM(Cantitate) > 5))";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getAssetLocationByName(String assetName) {
        String sql = "SELECT s.NumeSala, s.Etaj, sd.Cantitate " +
                "FROM Sali s JOIN SalaDotari sd ON s.SalaID = sd.SalaID " +
                "WHERE sd.DotareID IN (SELECT DotareID FROM Dotari WHERE NumeDotare LIKE ?)";
        return jdbcTemplate.queryForList(sql, "%" + assetName + "%");
    }

    public List<Map<String, Object>> getInventoryStatusAnalysis() {
        String sql = "SELECT TipDotare, Stare, COUNT(*) as NumarUnitati " +
                "FROM Dotari GROUP BY TipDotare, Stare ORDER BY TipDotare";
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

    public void updateRecord(String tableName, String targetName, Map<String, String> formData) {
        String nameColumn = getNameColumn(tableName);
        List<String> sets = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (entry.getKey().toLowerCase().contains("id") || entry.getKey().equals("tableName") || entry.getKey().equals("targetName")) continue;
            sets.add(entry.getKey() + " = ?");
            params.add(entry.getValue());
        }
        params.add(targetName);
        jdbcTemplate.update("UPDATE " + tableName + " SET " + String.join(", ", sets) + " WHERE " + nameColumn + " = ?", params.toArray());
    }

    public void deleteByBusinessName(String tableName, String identifierValue) {
        String nameColumn = getNameColumn(tableName);
        jdbcTemplate.update("DELETE FROM " + tableName + " WHERE " + nameColumn + " = ?", identifierValue);
    }

    private String getNameColumn(String tableName) {
        switch (tableName.toLowerCase()) {
            case "facultati": return "NumeFacultate";
            case "departament": return "NumeDepartament";
            case "sali": return "NumeSala";
            case "dotari": return "NumeDotare";
            default: return "Nume";
        }
    }

    private boolean isValidTable(String tableName) {
        return Arrays.asList("Dotari", "Sali", "Facultati", "Departament", "Utilizatori", "SalaDotari").stream()
                .anyMatch(t -> t.equalsIgnoreCase(tableName));
    }
}