package com.example.Green_Computing_CSE407.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@Controller
public class MainController {

    private static final Map<String, DatasetInfo> DATASETS = new HashMap<>();

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    private void initializeDatasets() {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/");
            File staticDir = resource.getFile();

            if (staticDir.exists() && staticDir.isDirectory()) {
                File[] csvFiles = staticDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

                if (csvFiles != null) {
                    for (File csvFile : csvFiles) {
                        String fileName = csvFile.getName();
                        String fileKey = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                        Resource csvResource = resourceLoader.getResource("classpath:static/" + fileName);
                        DATASETS.put(fileKey, new DatasetInfo(
                                fileName,
                                csvResource.getFile().getPath(),
                                "Data from " + fileName,
                                determineIconFromFileName(fileName),
                                getColumnsFromCSV(csvResource.getInputStream())
                        ));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            addFallbackDatasets();
        }
    }

    private void addFallbackDatasets() {
        DATASETS.put("netlab", new DatasetInfo(
                "new_netLab.csv",
                "classpath:static/new_netLab.csv",
                "Network traffic and usage patterns from our lab environment",
                "chart-bar",
                new String[]{"timestamp", "network_usage", "bandwidth"}
        ));

        DATASETS.put("powerusage", new DatasetInfo(
                "pc_ups_netLab.csv",
                "classpath:static/pc_ups_netLab.csv",
                "Detailed power consumption metrics across different devices",
                "bolt",
                new String[]{"timestamp", "power_consumption", "voltage"}
        ));
    }

    private static String[] getColumnsFromCSV(InputStream inputStream) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
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
            try {
                Resource csvResource = resourceLoader.getResource("classpath:static/" + DATASETS.get(dataset).getName());
                try (CSVReader reader = new CSVReader(new InputStreamReader(csvResource.getInputStream()))) {
                    List<String[]> records = reader.readAll();

                    model.addAttribute("data", records);
                    model.addAttribute("selectedDataset", dataset);
                    model.addAttribute("datasetInfo", DATASETS.get(dataset));
                    model.addAttribute("columns", records.get(0));
                }
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
            try {
                Resource csvResource = resourceLoader.getResource("classpath:static/" + DATASETS.get(dataset).getName());
                try (CSVReader reader = new CSVReader(new InputStreamReader(csvResource.getInputStream()))) {
                    List<String[]> records = reader.readAll();
                    model.addAttribute("data", records);
                    model.addAttribute("selectedDataset", dataset);
                    model.addAttribute("datasetInfo", DATASETS.get(dataset));
                }
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
