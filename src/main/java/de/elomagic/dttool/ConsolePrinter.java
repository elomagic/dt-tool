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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class ConsolePrinter {

    private static final Logger LOGGER = LogManager.getLogger(ConsolePrinter.class);
    private boolean verbose = false;
    private boolean debug = false;

    public static final ConsolePrinter INSTANCE = new ConsolePrinter();

    private ConsolePrinter() {}

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void trace(@NotNull String message, Object ...args) {
        if (debug) {
            LOGGER.trace(message, args);
        }
    }

    public void debug(@NotNull String message, Object ...args) {
        if (debug) {
            LOGGER.debug(message, args);
        }
    }

    public void info(@NotNull String message, Object ...args) {
        if (debug || verbose) {
            LOGGER.info(message, args);
        }
    }

    public void warn(@NotNull String message, Object ...args) {
        LOGGER.warn(message, args);
    }

    public void error(@NotNull String message, Object ...args) {
        LOGGER.error(message, args);
    }

    public void always(@NotNull String message, Object ...args) {
        LOGGER.always().log(message, args);
    }

}
