/** Controller care gestioneaza rutele si validarea datelor
 * @author David Matei
 * @version 10 Ianuarie 2026
 */
package io.github.davidmatei1902.uni_assets.controller;

import io.github.davidmatei1902.uni_assets.model.Asset;
import io.github.davidmatei1902.uni_assets.service.DatabaseService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
public class AppController {

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/")
    public String showIndex(Model model) {
        boolean isDatabaseUp = false;
        try {
            isDatabaseUp = databaseService.checkDatabaseStatus();
            model.addAttribute("systemStatus", isDatabaseUp ? "Online" : "Maintenance");
            model.addAttribute("statusColor", isDatabaseUp ? "#28a745" : "#ffc107");
        } catch (Exception e) {
            model.addAttribute("systemStatus", "Offline");
            model.addAttribute("statusColor", "#dc3545");
        }
        return "index";
    }

    @GetMapping("/login")
    public String showLogin(HttpSession session) {
        return session.getAttribute("user") != null ? "redirect:/dashboard" : "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        if (databaseService.checkLogin(username, password)) {
            session.setAttribute("user", username);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Utilizator sau parolă incorectă!");
        return "login";
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model,
                                @RequestParam(required = false) String selectedTable,
                                @RequestParam(required = false) String filterParam,
                                @RequestParam(required = false, defaultValue = "false") boolean showSecret) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        model.addAttribute("tables", databaseService.getAllowedTables(showSecret));
        model.addAttribute("selectedTable", selectedTable);
        model.addAttribute("showSecret", showSecret);

        if (selectedTable != null) {
            List<Map<String, Object>> resultData;
            String filterValue = (filterParam == null || filterParam.isEmpty()) ? "" : filterParam;
            String desc = "";

            switch (selectedTable) {
                case "COMPLEX_REPORT":
                    resultData = databaseService.getAssetsByFaculty(filterValue.isEmpty() ? "AC" : filterValue);
                    desc = "Afișează toate dotările și locația lor pentru o anumită facultate.";
                    model.addAttribute("currentParam", filterValue.isEmpty() ? "AC" : filterValue);
                    break;
                case "ROOMS_AVG":
                    resultData = databaseService.getRoomsAboveAverageCapacity();
                    desc = "Identifică sălile care au o capacitate mai mare decât media universității.";
                    Double avg = databaseService.getAverageCapacity();
                    model.addAttribute("avgValue", (avg != null) ? String.format("%.0f", avg) : "0.00");
                    break;
                case "DEPT_STATS":
                    resultData = databaseService.getTopEquippedDepartments();
                    desc = "Afișează departamentele care dețin cele mai multe obiecte de inventar.";
                    model.addAttribute("thresholdValue", 5);
                    break;
                case "ASSET_LOC":
                    resultData = databaseService.getAssetLocationByName(filterValue.isEmpty() ? "PC" : filterValue);
                    desc = "Caută sala și etajul unde se află un anumit tip de obiect.";
                    model.addAttribute("currentParam", filterValue.isEmpty() ? "PC" : filterValue);
                    break;
                case "STATUS_ANALYSIS":
                    resultData = databaseService.getLocationAuditReport();
                    desc = "Audit critic locații: Identifică sălile cu echipamente defecte.";
                    break;
                default:
                    resultData = databaseService.getSortedTableData(selectedTable);
                    desc = "Tabel sortat alfabetic.";
                    List<String> cols = databaseService.getTableColumns(selectedTable);
                    cols.removeIf(c -> c.toLowerCase().contains("id"));
                    model.addAttribute("editColumns", cols);
                    break;
            }
            model.addAttribute("tableData", resultData);
            if (!resultData.isEmpty()) model.addAttribute("columns", resultData.get(0).keySet());
            model.addAttribute("tableDescription", desc);
        }
        return "dashboard";
    }

    @PostMapping("/insert")
    public String insertData(@RequestParam String tableName, @RequestParam Map<String, String> allParams, RedirectAttributes ra) {
        try {
            Map<String, String> dataParams = new HashMap<>(allParams);
            dataParams.remove("tableName");

            if (tableName.equalsIgnoreCase("Departament")) {
                dataParams.put("FacultateID", "5");
            }

            for (Map.Entry<String, String> entry : dataParams.entrySet()) {
                String key = entry.getKey();
                String value = (entry.getValue() != null) ? entry.getValue().trim() : "";

                if (value.isEmpty()) {
                    throw new IllegalArgumentException("Câmpul '" + key + "' nu poate fi gol!");
                }

                if (key.equalsIgnoreCase("Telefon")) {
                    if (!value.matches("^\\d{3}-\\d{3}-\\d{4}$")) {
                        throw new IllegalArgumentException("Telefonul trebuie să fie de forma XXX-XXX-XXXX!");
                    }
                }
                else if (key.equalsIgnoreCase("Website")) {
                    if (!value.matches("^https://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(/.*)?$")) {
                        throw new IllegalArgumentException("Website-ul trebuie să înceapă cu https:// și să fie valid!");
                    }
                }
                else if (key.equalsIgnoreCase("Greutate")) {
                    if (!value.matches("^\\d+(\\.\\d+)?$")) {
                        throw new IllegalArgumentException("Greutatea trebuie să fie un număr valid!");
                    }
                    if (Double.parseDouble(value) < 0) throw new IllegalArgumentException("Greutatea nu poate fi negativă!");
                }
                else if (value.matches("^-?\\d+$")) {
                    int numValue = Integer.parseInt(value);
                    if (numValue < 0) throw new IllegalArgumentException("Câmpul '" + key + "' nu poate fi negativ!");
                    if (key.equalsIgnoreCase("Capacitate") && numValue == 0) throw new IllegalArgumentException("Capacitatea minimă este 1!");
                }
                else {
                    if (key.equalsIgnoreCase("CodFacultate")) {
                        if (value.length() < 2) throw new IllegalArgumentException("CodFacultate: minim 2 caractere!");
                    } else if (!key.equalsIgnoreCase("FacultateID")) { // Ignorăm ID-ul forțat de la validarea de caractere
                        if (value.length() < 3) throw new IllegalArgumentException("Câmpul '" + key + "': minim 3 caractere!");
                    }
                }
            }

            databaseService.insertRecord(tableName, dataParams);
            ra.addFlashAttribute("successMessage", "Înregistrare adăugată cu succes!");

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            String detail = (e.getRootCause() != null) ? e.getRootCause().getMessage() : "Conflict de unicitate sau cheie externă.";
            ra.addFlashAttribute("errorMessage", "La Inserare - Eroare SQL: " + detail);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", "La Inserare - Eroare validare: " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "La Inserare - Eroare neprevăzută: " + e.getMessage());
        }

        return "redirect:/dashboard?selectedTable=" + tableName;
    }

    @PostMapping("/delete")
    public String deleteData(@RequestParam String tableName, @RequestParam String identifier, RedirectAttributes ra) {
        try {
            if (identifier == null || identifier.trim().isEmpty()) {
                throw new IllegalArgumentException("Trebuie să introduceți un nume/identificator pentru ștergere!");
            }

            databaseService.deleteRecord(tableName, identifier);
            ra.addFlashAttribute("successMessage", "Înregistrarea a fost ștearsă cu succes din " + tableName);

            return "redirect:/dashboard?selectedTable=" + tableName;
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Eroare la ștergere: " + e.getMessage());
            return "redirect:/dashboard?selectedTable=" + tableName;
        }
    }

    @PostMapping("/update")
    public String updateData(@RequestParam String tableName, @RequestParam String targetName, @RequestParam Map<String, String> allParams, RedirectAttributes ra) {
        try {
            Map<String, String> dataParams = new HashMap<>(allParams);
            dataParams.remove("tableName");
            dataParams.remove("targetName");

            dataParams.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().trim().isEmpty());

            if (dataParams.isEmpty()) {
                throw new IllegalArgumentException("Nu ați introdus nicio valoare nouă!");
            }

            for (Map.Entry<String, String> entry : dataParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().trim();

                if (key.equalsIgnoreCase("Telefon")) {
                    if (!value.matches("^\\d{3}-\\d{3}-\\d{4}$")) throw new IllegalArgumentException("Telefon invalid!");
                }
                else if (key.equalsIgnoreCase("Website")) {
                    if (!value.matches("^https://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(/.*)?$")) throw new IllegalArgumentException("Website invalid!");
                }
                else if (key.equalsIgnoreCase("Greutate")) {
                    if (!value.matches("^\\d+(\\.\\d+)?$")) throw new IllegalArgumentException("Greutate invalidă!");
                    if (Double.parseDouble(value) < 0) throw new IllegalArgumentException("Greutatea nu poate fi negativă!");
                }
                else if (value.matches("^-?\\d+$")) {
                    int numValue = Integer.parseInt(value);
                    if (numValue < 0) throw new IllegalArgumentException("Câmpul '" + key + "' nu poate fi negativ!");
                }
                else {
                    if (key.equalsIgnoreCase("CodFacultate") && value.length() < 2) throw new IllegalArgumentException("CodFacultate: min 2!");
                    else if (!key.equalsIgnoreCase("CodFacultate") && value.length() < 3) throw new IllegalArgumentException("Câmpul '" + key + "': min 3!");
                }
            }

            databaseService.updateRecord(tableName, targetName, dataParams);
            ra.addFlashAttribute("successMessage", "Actualizare realizată cu succes!");

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            ra.addFlashAttribute("errorMessage", "La Update - Conflict de date: Modificarea încalcă o regulă de unicitate.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", "La Update - Eroare validare: " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "La Update - Eroare neprevăzută: " + e.getMessage());
        }

        return "redirect:/dashboard?selectedTable=" + tableName;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}