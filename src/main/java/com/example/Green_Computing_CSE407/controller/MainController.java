package com.example.Green_Computing_CSE407.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.File;
import java.nio.file.Paths;

@Controller
public class MainController {

    private static final Map<String, DatasetInfo> DATASETS = new HashMap<>();
    private static final String STATIC_DIR = "src/main/resources/static/";

    static {
        initializeDatasets();
    }

    private static void initializeDatasets() {
        // Create File object for the static directory
        File staticDir = new File(STATIC_DIR);

        // Check if directory exists and is readable
        if (staticDir.exists() && staticDir.isDirectory()) {
            // Filter for CSV files
            File[] csvFiles = staticDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

            if (csvFiles != null) {
                for (File csvFile : csvFiles) {
                    String fileName = csvFile.getName();
                    String fileKey = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                    // Create dataset info based on file name
                    String displayName = fileName; // Use original filename for display
                    String description = "Data from " + fileName;
                    String icon = determineIconFromFileName(fileName);

                    DATASETS.put(fileKey, new DatasetInfo(
                            displayName,
                            STATIC_DIR + fileName,
                            description,
                            icon,
                            getColumnsFromCSV(STATIC_DIR + fileName)
                    ));
                }
            }
        }

        // If no CSV files were found, add default datasets as fallback
        if (DATASETS.isEmpty()) {
            DATASETS.put("netlab", new DatasetInfo(
                    "new_netLab.csv",
                    STATIC_DIR + "new_netLab.csv",
                    "Network traffic and usage patterns from our lab environment",
                    "chart-bar",
                    new String[]{"timestamp", "network_usage", "bandwidth"}
            ));

            DATASETS.put("powerusage", new DatasetInfo(
                    "pc_ups_netLab.csv",
                    STATIC_DIR + "pc_ups_netLab.csv",
                    "Detailed power consumption metrics across different devices",
                    "bolt",
                    new String[]{"timestamp", "power_consumption", "voltage"}
            ));
        }
    }

    private static String[] getColumnsFromCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] headers = reader.readNext();
            return headers != null ? headers : new String[0];
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    private static String determineIconFromFileName(String fileName) {
        fileName = fileName.toLowerCase();
        if (fileName.contains("net") || fileName.contains("network")) {
            return "chart-bar";
        } else if (fileName.contains("power") || fileName.contains("ups")) {
            return "bolt";
        } else if (fileName.contains("temp") || fileName.contains("temperature")) {
            return "thermometer-half";
        } else if (fileName.contains("cpu") || fileName.contains("processor")) {
            return "microchip";
        } else if (fileName.contains("memory") || fileName.contains("ram")) {
            return "memory";
        } else if (fileName.contains("disk") || fileName.contains("storage")) {
            return "hdd";
        } else if (fileName.contains("energy") || fileName.contains("consumption")) {
            return "plug";
        } else {
            return "file-csv";
        }
    }

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/overview")
    public String overview(Model model) {
        model.addAttribute("content", "overview :: content");
        return "overview";
    }

    @GetMapping("/visualization")
    public String visualization(@RequestParam(required = false) String dataset, Model model) {
        model.addAttribute("datasets", DATASETS);

        if (dataset != null && DATASETS.containsKey(dataset)) {
            try (CSVReader reader = new CSVReader(new FileReader(DATASETS.get(dataset).getFilePath()))) {
                List<String[]> records = reader.readAll();

                // Add all data related attributes
                model.addAttribute("data", records);
                model.addAttribute("selectedDataset", dataset);
                model.addAttribute("datasetInfo", DATASETS.get(dataset));
                model.addAttribute("columns", records.get(0)); // Add column headers

            } catch (IOException | CsvException e) {
                e.printStackTrace();
                model.addAttribute("error", "Error reading CSV file: " + e.getMessage());
            }
        }

        return "visualization";
    }

    @GetMapping("/my-data")
    public String myData(@RequestParam(required = false) String dataset, Model model) {
        model.addAttribute("datasets", DATASETS);

        if (dataset != null && DATASETS.containsKey(dataset)) {
            try (CSVReader reader = new CSVReader(new FileReader(DATASETS.get(dataset).getFilePath()))) {
                List<String[]> records = reader.readAll();
                model.addAttribute("data", records);
                model.addAttribute("selectedDataset", dataset);
                model.addAttribute("datasetInfo", DATASETS.get(dataset));
            } catch (IOException | CsvException e) {
                e.printStackTrace();
                model.addAttribute("error", "Error reading CSV file: " + e.getMessage());
            }
        }

        return "my-data";
    }

    @GetMapping("/sample-input")
    public String sampleInput(Model model) {
        return "sample-input";
    }

    @GetMapping("/calculator")
    public String calculator(Model model) {
        return "calculator";
    }
}

class DatasetInfo {
    private final String name;
    private final String filePath;
    private final String description;
    private final String icon;
    private final String[] visualizableColumns;

    public DatasetInfo(String name, String filePath, String description, String icon, String[] visualizableColumns) {
        this.name = name;
        this.filePath = filePath;
        this.description = description;
        this.icon = icon;
        this.visualizableColumns = visualizableColumns;
    }

    // Getters
    public String getName() { return name; }
    public String getFilePath() { return filePath; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public String[] getVisualizableColumns() { return visualizableColumns; }
}