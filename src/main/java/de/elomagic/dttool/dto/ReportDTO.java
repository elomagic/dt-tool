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
package de.elomagic.dttool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ReportDTO {

    @JsonProperty
    private String flooredBomDate;
    @JsonProperty
    private String projectName;
    @JsonProperty
    private ZonedDateTime reportDate;
    @JsonProperty
    private double averageInheritedRiskScore;
    @JsonProperty
    private double averageCritical;
    @JsonProperty
    private double averageHigh;
    @JsonProperty
    private double averageMedium;
    @JsonProperty
    private double averageLow;
    @JsonProperty
    private double averageUnassigned;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Returns the month and year as string.
     *
     * @return Returns date in pattern "yyyy-MM"
     */
    public String getFlooredBomDate() {
        return flooredBomDate;
    }

    public void setFlooredBomDate(String flooredBomDate) {
        this.flooredBomDate = flooredBomDate;
    }

    /**
     * Set bom date and will be floored to "yyyy-MM".
     *
     * @param flooredBomDate ZonedDateTime
     */
    public void setFlooredBomDate(ZonedDateTime flooredBomDate) {
        this.flooredBomDate = flooredBomDate == null ? null : flooredBomDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public ZonedDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(ZonedDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public double getAverageInheritedRiskScore() {
        return averageInheritedRiskScore;
    }

    public void setAverageInheritedRiskScore(double averageInheritedRiskScore) {
        this.averageInheritedRiskScore = averageInheritedRiskScore;
    }

    public double getAverageCritical() {
        return averageCritical;
    }

    public void setAverageCritical(double averageCritical) {
        this.averageCritical = averageCritical;
    }

    public double getAverageHigh() {
        return averageHigh;
    }

    public void setAverageHigh(double averageHigh) {
        this.averageHigh = averageHigh;
    }

    public double getAverageMedium() {
        return averageMedium;
    }

    public void setAverageMedium(double averageMedium) {
        this.averageMedium = averageMedium;
    }

    public double getAverageLow() {
        return averageLow;
    }

    public void setAverageLow(double averageLow) {
        this.averageLow = averageLow;
    }

    public double getAverageUnassigned() {
        return averageUnassigned;
    }

    public void setAverageUnassigned(double averageUnassigned) {
        this.averageUnassigned = averageUnassigned;
    }

    @Override
    public String toString() {
        return "ReportDTO{" +
                "flooredBomDate='" + flooredBomDate + '\'' +
                ", projectName='" + projectName + '\'' +
                ", reportDate=" + reportDate +
                ", averageInheritedRiskScore=" + averageInheritedRiskScore +
                ", averageCritical=" + averageCritical +
                ", averageHigh=" + averageHigh +
                ", averageMedium=" + averageMedium +
                ", averageLow=" + averageLow +
                ", averageUnassigned=" + averageUnassigned +
                '}';
    }

}
