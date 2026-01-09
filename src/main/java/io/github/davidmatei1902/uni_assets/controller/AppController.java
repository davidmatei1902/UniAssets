/** Clasa pentru gestionarea fluxului de date intre interfata web si baza de date
 * @author David Matei
 * @version 8 Ianuarie 2026
 */
package io.github.davidmatei1902.uni_assets.controller;

import io.github.davidmatei1902.uni_assets.service.DatabaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
public class AppController {

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/")
    public String showIndex() {
        return "index";
    }

    @GetMapping("/login")
    public String showLogin(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "login";
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
        if (session.getAttribute("user") == null) return "redirect:/login";

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
                    tableDescription = "Afișează toate dotările și locația lor pentru o anumită facultate.";
                    model.addAttribute("currentParam", filterValue.isEmpty() ? "AC" : filterValue);
                    break;
                case "ROOMS_AVG":
                    resultData = databaseService.getRoomsAboveAverageCapacity();
                    Double avg = databaseService.getAverageCapacity();
                    model.addAttribute("avgValue", (avg != null) ? String.format("%.0f", avg) : "0.00");
                    tableDescription = "Identifică sălile care au o capacitate mai mare decât media universității.";
                    break;
                case "DEPT_STATS":
                    resultData = databaseService.getTopEquippedDepartments();
                    tableDescription = "Afișează departamentele care dețin cele mai multe obiecte de inventar.";
                    model.addAttribute("thresholdValue", 5);
                    break;
                case "ASSET_LOC":
                    resultData = databaseService.getAssetLocationByName(filterValue.isEmpty() ? "PC" : filterValue);
                    tableDescription = "Caută sala și etajul unde se află un anumit tip de obiect.";
                    model.addAttribute("currentParam", filterValue.isEmpty() ? "PC" : filterValue);
                    break;
                case "STATUS_ANALYSIS":
                    resultData = databaseService.getInventoryStatusAnalysis();
                    tableDescription = "Prezintă starea de funcționare a inventarului pe categorii de obiecte.";
                    break;
                default:
                    resultData = databaseService.getSortedTableData(selectedTable);
                    tableDescription = "Lista completă a înregistrărilor din tabelă (Sortată alfabetic).";
                    List<String> columns = databaseService.getTableColumns(selectedTable);
                    columns.removeIf(c -> c.toLowerCase().contains("id"));
                    model.addAttribute("editColumns", columns);
                    break;
            }
            model.addAttribute("tableData", resultData);
            if (!resultData.isEmpty()) model.addAttribute("columns", resultData.get(0).keySet());
            model.addAttribute("tableDescription", tableDescription);
        }
        return "dashboard";
    }

    @PostMapping("/insert")
    public String insertData(@RequestParam String tableName, @RequestParam Map<String, String> allParams, RedirectAttributes ra) {
        try {
            databaseService.insertRecord(tableName, allParams);
            return "redirect:/dashboard?selectedTable=" + tableName;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard?selectedTable=" + tableName;
        }
    }

    @PostMapping("/update")
    public String updateData(@RequestParam String tableName, @RequestParam String targetName, @RequestParam Map<String, String> allParams, RedirectAttributes ra) {
        try {
            databaseService.updateRecord(tableName, targetName, allParams);
            return "redirect:/dashboard?selectedTable=" + tableName;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard?selectedTable=" + tableName;
        }
    }

    @PostMapping("/delete")
    public String deleteData(@RequestParam String tableName, @RequestParam String identifier) {
        databaseService.deleteByBusinessName(tableName, identifier);
        return "redirect:/dashboard?selectedTable=" + tableName;
    }
}