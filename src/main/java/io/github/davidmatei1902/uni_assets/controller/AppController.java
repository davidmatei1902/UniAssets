package io.github.davidmatei1902.uni_assets.controller;

import io.github.davidmatei1902.uni_assets.service.DatabaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class AppController {

    @Autowired
    private DatabaseService dbService;

    // --- LOGIN SECTION ---
    @GetMapping("/")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        if (dbService.checkLogin(username, password)) {
            session.setAttribute("user", username);
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Incorrect username or password!");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // --- DASHBOARD SECTION ---
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model, @RequestParam(required = false) String selectedTable) {
        if (session.getAttribute("user") == null) return "redirect:/";

        // List of tables for the Dropdown menu
        List<String> tables = List.of("Dotari", "Sali", "Facultati", "Departament");
        model.addAttribute("tables", tables);
        model.addAttribute("selectedTable", selectedTable);

        // Load data and schema if a table is selected
        if (selectedTable != null && !selectedTable.isEmpty()) {
            try {
                // Fetch data for the View tab
                List<Map<String, Object>> rows = dbService.getTable(selectedTable);
                model.addAttribute("tableData", rows);

                // Extract column names for the table header
                if (!rows.isEmpty()) {
                    model.addAttribute("columns", rows.get(0).keySet());
                }

                // Fetch schema for the Insert tab
                List<String> schemaColumns = dbService.getTableColumns(selectedTable);

                // Filter out ID columns so they don't appear in the insert form
                schemaColumns.removeIf(col -> col.toLowerCase().endsWith("id"));
                model.addAttribute("insertColumns", schemaColumns);

            } catch (Exception e) {
                model.addAttribute("error", "DB Error: " + e.getMessage());
            }
        }
        return "dashboard";
    }

    // --- DATA ACTIONS ---
    @PostMapping("/insert")
    public String insertData(@RequestParam String tableName, @RequestParam Map<String, String> allParams) {
        // Remove technical parameters that are not part of the database columns
        allParams.remove("tableName");

        dbService.insertRow(tableName, allParams);
        return "redirect:/dashboard?selectedTable=" + tableName;
    }

    @PostMapping("/delete")
    public String deleteData(@RequestParam String tableName, @RequestParam int id) {
        dbService.deleteRow(tableName, id);
        return "redirect:/dashboard?selectedTable=" + tableName;
    }
}