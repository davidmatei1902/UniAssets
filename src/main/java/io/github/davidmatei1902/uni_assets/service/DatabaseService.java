/** Clasa Service pentru operatiile de manipulare a datelor si logica de procesare
 * @author David Matei
 * @version 8 Ianuarie 2026
 */
package io.github.davidmatei1902.uni_assets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final List<String> PUBLIC_TABLES = List.of(
            "Dotari", "Sali", "Facultati", "Departament", "Caracteristici"
    );

    private static final List<String> SECRET_TABLES = List.of(
            "SalaDotari", "DotariCaracteristici", "SalaDepartament", "Utilizatori"
    );

    private void validateFormData(Map<String, String> formData) {
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.equals("tableName") || key.equals("targetName") || key.toLowerCase().contains("id")) continue;

            // validare lungime nume
            if (key.toLowerCase().contains("nume") || key.toLowerCase().contains("username")) {
                if (value == null || value.trim().length() < 3) {
                    throw new IllegalArgumentException("Câmpul '" + key + "' trebuie să aibă cel puțin 3 caractere!");
                }
            }

            // validare valori numerice (Capacitate, Cantitate, Etaj)
            if (key.toLowerCase().contains("cantitate") ||
                    key.toLowerCase().contains("capacitate") ||
                    key.toLowerCase().contains("etaj")) {
                try {
                    int val = Integer.parseInt(value);
                    if (val < 0) {
                        throw new IllegalArgumentException("Valoarea pentru '" + key + "' nu poate fi negativă!");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Câmpul '" + key + "' trebuie să fie un număr valid!");
                }
            }
        }
    }

    public List<String> getAllowedTables(boolean showSecret) {
        if (showSecret) {
            return Stream.concat(PUBLIC_TABLES.stream(), SECRET_TABLES.stream())
                    .collect(Collectors.toList());
        }
        return PUBLIC_TABLES;
    }

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

    public Double getAverageCapacity() {
        String sql = "SELECT AVG(CAST(Capacitate AS FLOAT)) FROM Sali";
        return jdbcTemplate.queryForObject(sql, Double.class);
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

    // UNUSED
    public List<Map<String, Object>> getInventoryStatusAnalysis() {
        String sql = "SELECT TipDotare, Stare, COUNT(*) as NumarUnitati " +
                "FROM Dotari GROUP BY TipDotare, Stare ORDER BY TipDotare";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getLocationAuditReport() {
        String sql = "SELECT s.NumeSala, s.Etaj, s.Capacitate, " +
                "SUM(sd.Cantitate) as TotalObiecte, " +
                "COUNT(CASE WHEN d.Stare != 'Functional' THEN 1 END) as Defecte_Mentenanta " +
                "FROM Sali s " +
                "JOIN SalaDotari sd ON s.SalaID = sd.SalaID " +
                "JOIN Dotari d ON sd.DotareID = d.DotareID " +
                "GROUP BY s.NumeSala, s.Etaj, s.Capacitate " +
                "ORDER BY Defecte_Mentenanta DESC, TotalObiecte DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<String> getTableColumns(String tableName) {
        if (!isValidTable(tableName)) return new ArrayList<>();
        return jdbcTemplate.queryForList("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?", String.class, tableName);
    }

    public void insertRecord(String tableName, Map<String, String> formData) {
        validateFormData(formData);
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
        validateFormData(formData);
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
            case "caracteristici": return "NumeCaracteristica";
            case "utilizatori": return "username";
            default: return "ID";
        }
    }

    private boolean isValidTable(String tableName) {
        return Stream.concat(PUBLIC_TABLES.stream(), SECRET_TABLES.stream())
                .anyMatch(t -> t.equalsIgnoreCase(tableName));
    }

    public List<String> getSecretTables() { return SECRET_TABLES; }
}