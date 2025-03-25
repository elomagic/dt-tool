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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CommandLine.Command(name = "report", description = "Report export")
public class ReportExportCommand extends AbstractProjectFilterCommand implements Callable<Void> {

    @CommandLine.Option(
            names = { "--format" },
            description = "Export format. Supported values are: CSV, JSON",
            defaultValue = "CSV")
    ExportFormat format;
    @CommandLine.Option(
            names = { OptionsParams.VERSION_MATCH, OptionsParams.VERSION_MATCH_SHORT },
            description = "Regular expression to match version",
            defaultValue = "^\\d+(\\.\\d+)*(\\-Final)?$")
    String versionMatch;
    @CommandLine.Option(
            names = { "-f", "--file" },
            description = "Target file or path",
            required = true
    )
    private Path file;

    // Key = X month in the past. 0 = This month, 1 = Last month, 2 = The month before the last month and so on
    private final List<Map<String, Set<Project>>> projectsPerMonthList = new ArrayList<>();

    @Override
    public Void call() throws IOException {

        // Init projectSetMap
        // TODO Range must be calculated by given days
        long months = ChronoUnit.MONTHS.between(getNotBeforeInZonedTime(365 * 12), getNotAfterInZonedTime(0));
        IntStream.range(0, (int)months).forEach(index -> projectsPerMonthList.add(index, new HashMap<>()));

        int current = ZonedDateTime.now().getYear() * 12 + ZonedDateTime.now().getMonthValue();

        // Get projects and put it into a box
        fetchProjects(
                getNotBeforeInZonedTime(365 * 12),
                getNotAfterInZonedTime(0),
                versionMatch)
                .stream()
                .filter(p -> p.getLastBomImport() != null)
                .forEach(p -> {
                    int ref = p.getLastBomImport().getYear() * 12 + p.getLastBomImport().getMonthValue();
                    int key = current - ref;

                    // Check if key out of range
                    if (key >= projectsPerMonthList.size() || key < 0) return;

                    // Group by project name
                    Map<String, Set<Project>> projectsOfMonth = projectsPerMonthList.get(key);
                    Set<Project> projects = projectsOfMonth.getOrDefault(p.getName(), new HashSet<>());
                    projectsOfMonth.put(p.getName(), projects);
                });


        List<Map<String, ReportDTO>> monthReports = new ArrayList<>();
        IntStream.range(0, projectsPerMonthList.size()).forEach(index -> monthReports.add(index, new HashMap<>()));

        IntStream.range(0, 11).forEach(index -> projectsPerMonthList.add(index, new HashMap<>()));

        // Group reports to project name and month
        for (int i = 0; i < projectsPerMonthList.size(); i++) {
            // Map of a specific month with named set of projects
            Map<String, Set<Project>> namedProjects = projectsPerMonthList.get(i);

            int finalI = i;
            namedProjects.forEach((key, value) -> {
                double averagedRisk = value
                        .stream()
                        .mapToDouble(p -> p.getMetrics().getInheritedRiskScore())
                        .average()
                        .orElse(0);

                ReportDTO dto = monthReports.get(finalI).getOrDefault(key, new ReportDTO());
                dto.setProjectName(key);
                dto.setReportDate(ZonedDateTime.now());
                dto.setDate(ZonedDateTime.now().withDayOfMonth(1).minusMonths(finalI));
                dto.setAverageInheritedRiskScore(averagedRisk);
                monthReports.get(finalI).put(key, dto);
            });
        }

        List<ReportDTO> reports = monthReports.stream().flatMap(m -> m.values().stream()).toList();

        Files.createDirectories(file.getParent());

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
            writer.write(Arrays.stream(fields).map(Field::getName).collect(Collectors.joining(",")));
            writer.write("\n");

            reports.forEach(r -> writeCsvRecord(r, writer));
        }
    }

    private void writeCsvRecord(@Nonnull ReportDTO dto, @Nonnull Writer writer) {
        try {
            Field[] fields = ReportDTO.class.getDeclaredFields();

            writer.write(Arrays
                    .stream(fields)
                    .map(f -> getFieldValue(dto, f))
                    .map(v -> v == null ? "" : v)
                    .collect(Collectors.joining(",")));
            writer.write("\n");
        } catch (IOException ex) {
            throw new DtToolException(ex);
        }
    }

    @Nullable
    private String getFieldValue(@Nonnull ReportDTO dto, @Nonnull Field field) {
        try {
            if (!field.canAccess(dto)) {
                field.setAccessible(true);
            }
            return String.valueOf(field.get(dto));
        } catch (IllegalAccessException ex) {
            throw new DtToolException(ex);
        }
    }

    private void writeReportAsJson(@Nonnull List<ReportDTO> reports) throws IOException {
        ObjectMapper mapper = JsonMapperFactory.create();

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(mapper.writeValueAsString(reports));
        }
    }

}
