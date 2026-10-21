package org.example.structural.decorator.datasource;

public interface DataSource {
    void writeData(String data);

    String readData();
}
