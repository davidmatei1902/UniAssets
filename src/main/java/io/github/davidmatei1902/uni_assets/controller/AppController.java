/** Clasa pentru gestionarea fluxului de date intre interfata web si baza de date
 * @author David Matei
 * @version 5 Ianuarie 2026
 */
package io.github.davidmatei1902.uni_assets.controller;

import io.github.davidmatei1902.uni_assets.service.DatabaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
public class AppController {

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        if (databaseService.checkLogin(username, password)) {
            session.setAttribute("user", username);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Eroare autentificare!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model,
                                @RequestParam(required = false) String selectedTable,
                                @RequestParam(required = false) String filterParam,
                                @RequestParam(required = false, defaultValue = "false") boolean showSecret) {
        if (session.getAttribute("user") == null) return "redirect:/";

        if (!showSecret && selectedTable != null) {
            boolean isSecret = databaseService.getSecretTables().stream()
                    .anyMatch(t -> t.equalsIgnoreCase(selectedTable));

            if (isSecret) {
                return "redirect:/dashboard"; // redirect daca utilizatorul a ascuns tabelele in timp ce vizualiza una secreta
            }
        }

        model.addAttribute("tables", databaseService.getAllowedTables(showSecret));
        model.addAttribute("selectedTable", selectedTable);
        model.addAttribute("showSecret", showSecret);

        if (selectedTable != null) {
            List<Map<String, Object>> resultData;
            String filterValue = (filterParam == null || filterParam.isEmpty()) ? "" : filterParam;
            String tableDescription = "";

            switch (selectedTable) {
                case "COMPLEX_REPORT":
                    resultData = databaseService.getAssetsByFaculty(filterValue.isEmpty() ? "AC" : filterValue);
                    tableDescription = "Afiseaza toate dotarile si locatia lor pentru o anumita facultate.";
                    model.addAttribute("currentParam", filterValue.isEmpty() ? "AC" : filterValue);
                    break;
                case "ROOMS_AVG":
                    resultData = databaseService.getRoomsAboveAverageCapacity();
                    Double avg = databaseService.getAverageCapacity();

                    String formattedAvg = (avg != null) ? String.format("%.2f", avg) : "0.00";
                    tableDescription = "Identifica salile care au o capacitate mai mare decat media universitatii";

                    model.addAttribute("avgValue", formattedAvg);
                    break;
                case "DEPT_STATS":
                    resultData = databaseService.getTopEquippedDepartments();
                    tableDescription = "Afiseaza departamentele care detin cele mai multe obiecte de inventar.";

                    model.addAttribute("thresholdValue", 5);
                    break;
                case "ASSET_LOC":
                    resultData = databaseService.getAssetLocationByName(filterValue.isEmpty() ? "PC" : filterValue);
                    tableDescription = "Cauta sala si etajul unde se afla un anumit tip de obiect.";
                    model.addAttribute("currentParam", filterValue.isEmpty() ? "PC" : filterValue);
                    break;
                case "STATUS_ANALYSIS":
                    resultData = databaseService.getInventoryStatusAnalysis();
                    tableDescription = "Prezinta starea de functionare a inventarului pe categorii de obiecte.";
                    break;
                default:
                    resultData = databaseService.getSortedTableData(selectedTable);
                    tableDescription = "Lista completa a inregistrarilor din tabela (Sortata alfabetic).";
                    List<String> columns = databaseService.getTableColumns(selectedTable);
                    columns.removeIf(c -> c.toLowerCase().contains("id"));
                    model.addAttribute("editColumns", columns);
                    break;
            }
            model.addAttribute("tableDescription", tableDescription);
            model.addAttribute("tableData", resultData);
            if (!resultData.isEmpty()) model.addAttribute("columns", resultData.get(0).keySet());
        }
        return "dashboard";
    }

    @PostMapping("/insert")
    public String insertData(@RequestParam String tableName, @RequestParam Map<String, String> allParams) {
        databaseService.insertRecord(tableName, allParams);
        return "redirect:/dashboard?selectedTable=" + tableName;
    }

    @PostMapping("/update")
    public String updateData(@RequestParam String tableName, @RequestParam String targetName, @RequestParam Map<String, String> allParams) {
        databaseService.updateRecord(tableName, targetName, allParams);
        return "redirect:/dashboard?selectedTable=" + tableName;
    }

    @PostMapping("/delete")
    public String deleteData(@RequestParam String tableName, @RequestParam String identifier) {
        databaseService.deleteByBusinessName(tableName, identifier);
        return "redirect:/dashboard?selectedTable=" + tableName;
    }
}