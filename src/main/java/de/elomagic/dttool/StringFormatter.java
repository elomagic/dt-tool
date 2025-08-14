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

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public interface StringFormatter {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss XXX");

    @NonNull
    default String t2s(@Nullable ZonedDateTime time) {
        return time == null ? "<unset>" : time.format(FORMATTER);
    }

    /**
     * Right pad a string with minimum count of spaces (' ').
     *
     * @param value String to pad
     * @param minWidth Minimum count of spaces
     *
     * @return Returns the string with pad right spaces
     */
    @Nullable
    default String mnw(@Nullable String value, int minWidth) {
        return StringUtils.rightPad(value == null ? "" : value, minWidth);
    }

}
