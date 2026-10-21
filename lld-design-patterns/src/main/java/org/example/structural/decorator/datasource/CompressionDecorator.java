package org.example.structural.decorator.datasource;

public class CompressionDecorator implements DataSource {
    private static final String PREFIX = "compressed:";

    private final DataSource wrapped;

    public CompressionDecorator(DataSource source) {
        this.wrapped = source;
    }

    @Override
    public void writeData(String data) {
        wrapped.writeData(compress(data));
    }

    @Override
    public String readData() {
        return decompress(wrapped.readData());
    }

    private String compress(String data) {
        return PREFIX + data;
    }

    private String decompress(String data) {
        return data.startsWith(PREFIX) ? data.substring(PREFIX.length()) : data;
    }
}
