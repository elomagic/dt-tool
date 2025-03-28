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

import jakarta.annotation.Nonnull;

import java.time.ZonedDateTime;

public record ReportDTO(
        @Nonnull
        String flooredBomDate,
        @Nonnull
        String projectName,
        @Nonnull
        ZonedDateTime reportDate,
        double averageInheritedRiskScore,
        double averageCritical,
        double averageHigh,
        double averageMedium,
        double averageLow,
        double averageUnassigned
) {
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
