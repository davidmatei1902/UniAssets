/** Clasa Service completa pentru operatiile de manipulare a datelor
 * @author David Matei
 * @version 10 Ianuarie 2026
 */
package io.github.davidmatei1902.uni_assets.service;

import io.github.davidmatei1902.uni_assets.model.Asset;
import io.github.davidmatei1902.uni_assets.repository.AssetRepository;
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

    @Autowired
    private AssetRepository assetRepository;

    private static final List<String> PUBLIC_TABLES = List.of(
            "Dotari", "Sali", "Facultati", "Departament", "Caracteristici"
    );

    private static final List<String> SECRET_TABLES = List.of(
            "SalaDotari", "DotariCaracteristici", "SalaDepartament", "Utilizatori"
    );

    // METODA NOUA: Salvare prin JPA pentru a folosi validarile @Min, @Size (Cerinta A.1)
    public void saveWithJPA(Asset asset) {
        assetRepository.save(asset);
    }

    // --- TOATE METODELE TALE ORIGINALE ---

    private void validateFormData(Map<String, String> formData) {
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.equals("tableName") || key.equals("targetName") || key.toLowerCase().contains("id")) continue;
            if (key.toLowerCase().contains("nume") || key.toLowerCase().contains("username")) {
                if (value == null || value.trim().length() < 3) {
                    throw new IllegalArgumentException("Câmpul '" + key + "' trebuie să aibă cel puțin 3 caractere!");
                }
            }
            if (key.toLowerCase().contains("cantitate") || key.toLowerCase().contains("capacitate") || key.toLowerCase().contains("etaj")) {
                try {
                    int val = Integer.parseInt(value);
                    if (val < 0) throw new IllegalArgumentException("Valoarea pentru '" + key + "' nu poate fi negativă!");
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Câmpul '" + key + "' trebuie să fie un număr valid!");
                }
            }
        }
    }

    public List<String> getAllowedTables(boolean showSecret) {
        return showSecret ? Stream.concat(PUBLIC_TABLES.stream(), SECRET_TABLES.stream()).collect(Collectors.toList()) : PUBLIC_TABLES;
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
        return data.stream().sorted((m1, m2) -> {
            String v1 = m1.get(nameCol) != null ? m1.get(nameCol).toString() : "";
            String v2 = m2.get(nameCol) != null ? m2.get(nameCol).toString() : "";
            return v1.compareToIgnoreCase(v2);
        }).collect(Collectors.toList());
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

    public List<Map<String, Object>> getLocationAuditReport() {
        String sql = "SELECT s.NumeSala, s.Etaj, s.Capacitate, SUM(sd.Cantitate) as TotalObiecte, " +
                "COUNT(CASE WHEN d.Stare != 'Functional' THEN 1 END) as Defecte_Mentenanta " +
                "FROM Sali s JOIN SalaDotari sd ON s.SalaID = sd.SalaID JOIN Dotari d ON sd.DotareID = d.DotareID " +
                "GROUP BY s.NumeSala, s.Etaj, s.Capacitate ORDER BY Defecte_Mentenanta DESC, TotalObiecte DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public List<String> getTableColumns(String tableName) {
        if (!isValidTable(tableName)) return new ArrayList<>();
        return jdbcTemplate.queryForList("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?", String.class, tableName);
    }

    public void insertRecord(String tableName, Map<String, String> params) {
        String columns = String.join(", ", params.keySet());
        String placeholders = params.keySet().stream()
                .map(k -> "?")
                .collect(Collectors.joining(", "));

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

        // 2. Extragem valorile cu suport pentru FLOAT (Greutate) și INT (Etaj/Capacitate)
        Object[] values = params.values().stream().map(val -> {
            try {
                if (val == null) return null;
                // Verificăm dacă este număr cu virgulă (ex: 2.5)
                if (val.matches("-?\\d+\\.\\d+")) return Float.parseFloat(val);
                // Verificăm dacă este număr întreg (ex: 10)
                if (val.matches("-?\\d+")) return Integer.parseInt(val);
                return val;
            } catch (Exception e) {
                return val;
            }
        }).toArray();

        jdbcTemplate.update(sql, values);
    }

    public void updateRecord(String tableName, String targetName, Map<String, String> formData) {
        String nameColumn = getNameColumn(tableName);
        List<String> sets = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();

            if (key.toLowerCase().contains("id") || key.equals("tableName") || key.equals("targetName")) continue;

            sets.add(key + " = ?");

            // Conversie robustă pentru UPDATE (asigură suportul pentru Greutate float)
            try {
                if (val != null && val.matches("-?\\d+\\.\\d+")) {
                    params.add(Float.parseFloat(val));
                } else if (val != null && val.matches("-?\\d+")) {
                    params.add(Integer.parseInt(val));
                } else {
                    params.add(val);
                }
            } catch (Exception e) {
                params.add(val);
            }
        }

        params.add(targetName);
        jdbcTemplate.update("UPDATE " + tableName + " SET " + String.join(", ", sets) + " WHERE " + nameColumn + " = ?", params.toArray());
    }

    public void deleteRecord(String tableName, String identifier) {
        // Identificăm coloana de tip "Nume" pentru tabelul respectiv
        String columnName = getNameColumn(tableName);

        String sql = "DELETE FROM " + tableName + " WHERE " + columnName + " = ?";

        int rowsAffected = jdbcTemplate.update(sql, identifier);

        if (rowsAffected == 0) {
            throw new RuntimeException("Nu s-a găsit nicio înregistrare cu numele: " + identifier);
        }
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
        return Stream.concat(PUBLIC_TABLES.stream(), SECRET_TABLES.stream()).anyMatch(t -> t.equalsIgnoreCase(tableName));
    }

    public boolean checkDatabaseStatus() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return result != null && result == 1;
        } catch (Exception e) {
            return false;
        }
    }
}