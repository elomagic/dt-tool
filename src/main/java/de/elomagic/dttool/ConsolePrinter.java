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

import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@SuppressWarnings("squid:S6548")
public final class ConsolePrinter {

    private static final Marker ALWAYS = MarkerFactory.getMarker("ALWAYS");
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePrinter.class);
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

    public void trace(@Nonnull String message, Object ...args) {
        if (debug) {
            LOGGER.trace(message, args);
        }
    }

    public void debug(@Nonnull String message, Object ...args) {
        if (debug) {
            LOGGER.debug(message, args);
        }
    }

    public void info(@Nonnull String message, Object ...args) {
        if (debug || verbose) {
            LOGGER.info(message, args);
        }
    }

    public void warn(@Nonnull String message, Object ...args) {
        LOGGER.warn(message, args);
    }

    public void error(@Nonnull String message, Object ...args) {
        LOGGER.error(message, args);
    }

    public void always(@Nonnull String message, Object ...args) {
        LOGGER.info(ALWAYS, message, args);
    }

}
