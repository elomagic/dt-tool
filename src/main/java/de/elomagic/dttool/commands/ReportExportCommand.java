/*
 * DT-Tool
 * Copyright (c) 2024-present Carsten Rambow
 * mailto:developer AT elomagic DOT de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.elomagic.dttool.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import picocli.CommandLine;

import de.elomagic.dttool.DtToolException;
import de.elomagic.dttool.JsonMapperFactory;
import de.elomagic.dttool.OptionsParams;
import de.elomagic.dttool.configuration.model.ExportFormat;
import de.elomagic.dttool.dt.model.Project;
import de.elomagic.dttool.dto.ReportDTO;

import org.apache.logging.log4j.core.util.ReflectionUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(name = "report", description = "Report export")
public class ReportExportCommand extends AbstractProjectFilterCommand implements Callable<Void> {

    private DecimalFormat decimalFormat;

    @CommandLine.Option(
            names = { "--format" },
            description = "Export format. Supported values are: CSV, JSON",
            defaultValue = "CSV"
    )
    ExportFormat format;
    @CommandLine.Option(
            names = { OptionsParams.VERSION_MATCH, OptionsParams.VERSION_MATCH_SHORT },
            description = "Regular expression to match version",
            defaultValue = "^\\d+(\\.\\d+)*(\\-Final)?$"
    )
    String versionMatch;
    @CommandLine.Option(
            names = { "-f", "--file" },
            description = "Target file or path",
            required = true
    )
    Path file;
    @CommandLine.Option(
            names = { "-dc", "--delimiterChar" },
            description = "Delimiter char between columns when export format is CSV",
            defaultValue = ";"
    )
    String delimiterChar;
    @CommandLine.Option(
            names = { "-ds", "--decimalSymbol" },
            description = "Decimal symbol for floating values",
            defaultValue = "."
    )
    char decimalSymbol;
    @CommandLine.Option(
            names = { "-fg", "--filLGaps" },
            description = "Fill gaps of month, where a product has no BOM released with BOM from the previous month",
            defaultValue = "false",
            negatable = true
    )
    boolean fillGap;


    @Override
    public Void call() throws IOException {
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance(Locale.getDefault());
        decimalSymbols.setDecimalSeparator(decimalSymbol);
        decimalFormat = new DecimalFormat("#0.00");
        decimalFormat.setDecimalFormatSymbols(decimalSymbols);

        // Key = X month in the past. 0 = This month, 1 = Last month, 2 = The month before the last month and so on
        Map<String, Map<String, Set<Project>>> monthMap = new HashMap<>();

        // Get projects and put it into a box
        fetchProjects(
                getNotBeforeInZonedTime(365 * 12),
                getNotAfterInZonedTime(0),
                versionMatch)
                .stream()
                .filter(p -> p.getLastBomImport() != null)
                .forEach(p -> {
                    String key = "%s-%02d".formatted(p.getLastBomImport().getYear(), p.getLastBomImport().getMonthValue());

                    // Group by project name
                    Map<String, Set<Project>> projectsMap = monthMap.getOrDefault(key, new HashMap<>());
                    monthMap.put(key, projectsMap);

                    Set<Project> projects = projectsMap.getOrDefault(p.getName(), new HashSet<>());
                    projects.add(p);
                    projectsMap.put(p.getName(), projects);
                });


        Map<String, Map<String, ReportDTO>> monthReports = new HashMap<>();

        // Group reports to project name and month
        for (Map.Entry<String, Map<String, Set<Project>>> e : monthMap.entrySet()) {
            // Map of a specific month with named set of projects
            Map<String, Set<Project>> namedProjects = e.getValue();

            namedProjects.forEach((key, value) -> {
                double averageRiskScore = value
                        .stream()
                        .mapToDouble(p -> p.getMetrics().getInheritedRiskScore())
                        .average()
                        .orElse(0);

                Map<String, ReportDTO> namedReport = monthReports.getOrDefault(e.getKey(), new HashMap<>());
                monthReports.put(e.getKey(), namedReport);

                ReportDTO dto = namedReport.getOrDefault(key, new ReportDTO());
                namedReport.put(key, dto);

                dto.setProjectName(key);
                dto.setReportDate(ZonedDateTime.now());
                dto.setFlooredBomDate(value
                        .stream()
                        .findFirst()
                        .filter((p -> p.getLastBomImport() != null))
                        .map(p -> p.getLastBomImport().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)).orElse(null));
                dto.setAverageInheritedRiskScore(averageRiskScore);
            });
        }

        if (fillGap) {
            // Fill gaps
            List<String> months = monthReports
                    .keySet()
                    .stream()
                    .sorted(Comparator.naturalOrder())
                    .toList();


        }

        // Flatten into records
        List<ReportDTO> reports = monthReports.values().stream().flatMap(m -> m.values().stream()).toList();

        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        if (format == ExportFormat.CSV) {
            writeReportAsCsv(reports);
        } else {
            writeReportAsJson(reports);
        }

        return null;
    }

    private void writeReportAsCsv(@Nonnull List<ReportDTO> reports) throws IOException {
        Field[] fields = ReportDTO.class.getDeclaredFields();

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            // Write header
            writer.write(Arrays.stream(fields).map(Field::getName).collect(Collectors.joining(delimiterChar)));
            writer.write("\n");

            reports.forEach(r -> writeCsvRecord(r, writer));
        }
    }

    private void writeCsvRecord(@Nonnull ReportDTO dto, @Nonnull Writer writer) {
        try {
            Field[] fields = ReportDTO.class.getDeclaredFields();

            writer.write(Arrays
                    .stream(fields)
                    .map(f -> getCsvCell(f, dto))
                    .map(v -> v == null ? "" : v)
                    .collect(Collectors.joining(delimiterChar)));
            writer.write("\n");
        } catch (IOException ex) {
            throw new DtToolException(ex);
        }
    }

    @Nullable
    private String getCsvCell(@Nonnull Field field, ReportDTO dto) {
        Object o = ReflectionUtil.getFieldValue(field, dto);

        if (o instanceof Double d) {
            return decimalFormat.format(d);
        } else if (o instanceof Float f) {
            return decimalFormat.format(f);
        }

        return String.valueOf(o);
    }

    private void writeReportAsJson(@Nonnull List<ReportDTO> reports) throws IOException {
        ObjectMapper mapper = JsonMapperFactory.create();

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            String json = mapper.writeValueAsString(reports);
            writer.write(json);
        }
    }

}
