package org.example.structural.decorator.datasource;

public class EncryptionDecorator implements DataSource {
    private static final String PREFIX = "encrypted:";

    private final DataSource wrapped;

    public EncryptionDecorator(DataSource source) {
        this.wrapped = source;
    }

    @Override
    public void writeData(String data) {
        wrapped.writeData(encrypt(data));
    }

    @Override
    public String readData() {
        return decrypt(wrapped.readData());
    }

    private String encrypt(String data) {
        return PREFIX + data;
    }

    private String decrypt(String data) {
        return data.startsWith(PREFIX) ? data.substring(PREFIX.length()) : data;
    }
}
