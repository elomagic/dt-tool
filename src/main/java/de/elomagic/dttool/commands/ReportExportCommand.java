package de.elomagic.dttool.commands;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "report", description = "Report export")
public class ReportExportCommand extends AbstractProjectFilterCommand implements Callable<Void> {

    @Override
    public Void call() {
        return null;
    }

}
