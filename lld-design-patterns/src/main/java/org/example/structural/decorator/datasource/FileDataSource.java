package org.example.structural.decorator.datasource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileDataSource implements DataSource {
    private final Path file;

    public FileDataSource(String filename) {
        this.file = Path.of(filename);
    }

    @Override
    public void writeData(String data) {
        try {
            Files.writeString(file, data, StandardCharsets.UTF_8);
            System.out.println("Wrote to " + file + ": " + data);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write " + file, e);
        }
    }

    @Override
    public String readData() {
        try {
            String data = Files.readString(file, StandardCharsets.UTF_8);
            System.out.println("Read from " + file + ": " + data);
            return data;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + file, e);
        }
    }
}
