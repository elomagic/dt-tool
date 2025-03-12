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
package de.elomagic.dttool;

import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;

@CommandLine.Command
public class ProjectFilterOptions {

    @CommandLine.Option(names = { "--olderThenDays", "-otd" }, description = "Older then days", defaultValue = "30")
    private int olderThenDays;
    @CommandLine.Option(names = { "--projectFilter", "-pf" }, description = "Project name or UUID filter", split = ",")
    private String[] projectFilter = new String[0];
    @CommandLine.Option(names = { "--maxCount", "-mc" }, description = "Maximum count of results", defaultValue = "999999")
    private int maxCount;

    public int getOlderThenDays() {
        return olderThenDays;
    }

    public void setOlderThenDays(int olderThenDays) {
        this.olderThenDays = olderThenDays;
    }

    public List<String> getProjectFilter() {
        return Arrays.asList(projectFilter);
    }

    public int getMaxCount() {
        return maxCount;
    }

}
