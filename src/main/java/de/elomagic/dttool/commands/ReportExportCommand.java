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

import de.elomagic.dttool.ComparatorFactory;
import de.elomagic.dttool.DtToolException;
import de.elomagic.dttool.JsonMapperFactory;
import de.elomagic.dttool.OptionsParams;
import de.elomagic.dttool.TimeUtil;
import de.elomagic.dttool.configuration.model.ExportFormat;
import de.elomagic.dttool.dt.model.Project;
import de.elomagic.dttool.dto.ReportDTO;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.core.util.ReflectionUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.ToDoubleFunction;
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
            names = { "-fg", "--fillGaps" },
            description = "Fill gaps of month, where a product has no BOM released with BOM from the previous month",
            defaultValue = "false",
            negatable = true
    )
    boolean fillGap;


    @Nullable
    @Override
    public Void call() throws IOException {
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance(Locale.getDefault());
        decimalSymbols.setDecimalSeparator(decimalSymbol);
        decimalFormat = new DecimalFormat("#0.00");
        decimalFormat.setDecimalFormatSymbols(decimalSymbols);

        Map<String, Map<String, Set<Project>>> monthMap = getMonthMap();
        Map<String, Map<String, ReportDTO>> monthReports = summaryInMonthReports(monthMap);

        if (fillGap) {
            // First month
            LocalDate firstMonth = TimeUtil.parseMonthPattern(monthReports.keySet().stream().sorted().findFirst().orElseThrow());
            LocalDate lastMonth = TimeUtil.parseMonthPattern(monthReports.keySet().stream().sorted(Comparator.reverseOrder()).limit(monthReports.size()-1L).findFirst().orElseThrow());

            LocalDate currentMonth = firstMonth;
            while (currentMonth.isBefore(lastMonth)) {
                String currentMonthString = TimeUtil.toMonthPattern(currentMonth);
                String nextMonthString = TimeUtil.toMonthPattern(currentMonth.plusMonths(1));

                Set<String> missingProjectNames = new HashSet<>(CollectionUtils.removeAll(
                        monthReports.get(currentMonthString).keySet(),
                        monthReports.get(nextMonthString).keySet()));

                for (String projectName : missingProjectNames) {
                    // Create report for next month
                    ReportDTO currentReport = monthReports.get(currentMonthString).get(projectName);

                    ReportDTO nextReport = new ReportDTO(
                            nextMonthString,
                            currentReport.projectName(),
                            currentReport.reportDate(),
                            currentReport.averageInheritedRiskScore(),
                            currentReport.averageCritical(),
                            currentReport.averageHigh(),
                            currentReport.averageMedium(),
                            currentReport.averageLow(),
                            currentReport.averageUnassigned()
                    );

                    monthReports.get(nextReport.flooredBomDate()).put(projectName, nextReport);
                }

                currentMonth = currentMonth.plusMonths(1);
            }
        }

        // Flatten into records
        List<ReportDTO> reports = monthReports
                .values()
                .stream()
                .flatMap(m -> m.values().stream())
                .sorted(ComparatorFactory.reportComparator())
                .toList();

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

    /**
     * Group reports to project name and year-month key.
     *
     * @return A map but never null
     */
    @Nonnull
    private Map<String, Map<String, ReportDTO>> summaryInMonthReports(@Nonnull Map<String, Map<String, Set<Project>>> map) {
        Map<String, Map<String, ReportDTO>> monthReports = new HashMap<>();

        // Group reports to project name and year-month key
        for (Map.Entry<String, Map<String, Set<Project>>> e : map.entrySet()) {
            // Map of a specific month with named set of projects
            Map<String, Set<Project>> namedProjects = e.getValue();

            namedProjects.forEach((key, value) -> {
                Map<String, ReportDTO> namedReport = monthReports.getOrDefault(e.getKey(), new HashMap<>());
                monthReports.put(e.getKey(), namedReport);

                ReportDTO dto = namedReport.get(key);

                if (dto == null) {
                    // Map ZonedDateTime to month pattern
                    String month = TimeUtil.toMonthPattern(value
                            .stream()
                            .findFirst()
                            .filter((p -> p.getLastBomImport() != null))
                            .map(p -> p.getLastBomImport().toLocalDate().withDayOfMonth(1)).orElseThrow());

                    dto = new ReportDTO(
                            month,
                            key,
                            ZonedDateTime.now(),
                            getAverage(value, p -> p.getMetrics().getInheritedRiskScore()),
                            getAverage(value, p -> p.getMetrics().getCritical()),
                            getAverage(value, p -> p.getMetrics().getHigh()),
                            getAverage(value, p -> p.getMetrics().getMedium()),
                            getAverage(value, p -> p.getMetrics().getLow()),
                            getAverage(value, p -> p.getMetrics().getUnassigned())
                    );
                }

                namedReport.put(key, dto);
            });
        }
        return monthReports;
    }

    @Nonnull
    private Map<String, Map<String, Set<Project>>> getMonthMap() {
        // Key = X month in the past. 0 = This month, 1 = Last month, 2 = The month before the last month and so on
        Map<String, Map<String, Set<Project>>> monthMap = new HashMap<>();

        // Get projects and put it into a map with project name key and put it into a map with year-month key
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
        return monthMap;
    }

    private double getAverage(@Nonnull Set<Project> projects, @Nonnull ToDoubleFunction<Project> mapper) {
        return projects
                .stream()
                .mapToDouble(mapper)
                .average()
                .orElse(0);
    }


    @Nonnull
    private ReportDTO getPreviousReport(@Nonnull Map<String, Map<String, ReportDTO>> map, @Nonnull String currentFlooredDate, @Nonnull String projectName) {
        int year = Integer.parseInt(currentFlooredDate.substring(0, 4));
        int month = Integer.parseInt(currentFlooredDate.substring(5, 7));

        String previousFlooredDate = LocalDate
                .of(year, month, 1)
                .minusMonths(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return map.get(previousFlooredDate).get(projectName);
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
    private String getCsvCell(@Nonnull Field field, @Nonnull ReportDTO dto) {
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
