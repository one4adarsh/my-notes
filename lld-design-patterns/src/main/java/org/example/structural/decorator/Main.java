package org.example.structural.decorator;

import org.example.structural.decorator.datasource.CompressionDecorator;
import org.example.structural.decorator.datasource.DataSource;
import org.example.structural.decorator.datasource.EncryptionDecorator;
import org.example.structural.decorator.datasource.FileDataSource;

public class Main {
    public static void main(String[] args) {
        DataSource source = new FileDataSource("data.txt");
        source = new EncryptionDecorator(source);
        source = new CompressionDecorator(source);

        source.writeData("sensitive info");

        String data = source.readData();
    }
}
