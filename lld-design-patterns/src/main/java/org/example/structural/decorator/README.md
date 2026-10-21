# Decorator Pattern
- https://github.com/dstar55/100-words-design-patterns-java#Decorator

### Technical thought process to implement Decorator pattern
1. Create a component interface that defines the operations that can be decorated (both the real object and the decorators will implement it).
2. Create a concrete component class that implements the component interface and provides the core behavior.
3. Create decorator classes that implement the same component interface and hold a reference (`wrapped`) to another component object.
4. In each decorator, add the extra behavior before and/or after delegating the call to the wrapped component.
5. The client code wraps the concrete component in one or more decorators at runtime; since every layer shares the same interface, decorators can be stacked in any order and the client treats the result as a plain component.

### Example: DataSource with encryption and compression
A data source that reads and writes to a file can be extended with extra responsibilities, such as
encryption and compression, without modifying the file class or creating a subclass for every
combination (`EncryptedFileDataSource`, `CompressedEncryptedFileDataSource`, ...). Each feature is a
decorator that wraps another `DataSource`, so features can be mixed and matched at runtime.

| Pattern role       | Class                                                                         | Step |
|--------------------|-------------------------------------------------------------------------------|------|
| Component          | `datasource/DataSource` — `void writeData(String data)`, `String readData()`  | 1    |
| Concrete Component | `datasource/FileDataSource` — reads/writes the actual file                    | 2    |
| Concrete Decorator | `datasource/EncryptionDecorator` — encrypts on write, decrypts on read        | 3, 4 |
| Concrete Decorator | `datasource/CompressionDecorator` — compresses on write, decompresses on read | 3, 4 |
| Client             | `Main` — wraps `FileDataSource` with the decorators                           | 5    |

Each decorator depends only on the `DataSource` interface, so new features (logging, caching, ...) can
be added without touching existing classes. Calls flow inward on write and outward on read:

- `writeData()`: `CompressionDecorator` → `EncryptionDecorator` → `FileDataSource` (compress, then encrypt, then write)
- `readData()`: `FileDataSource` → `EncryptionDecorator` → `CompressionDecorator` (read, then decrypt, then decompress)

Usage:
```java
DataSource source = new FileDataSource("data.txt");
source = new EncryptionDecorator(source);
source = new CompressionDecorator(source);

source.writeData("sensitive info");
// Data gets compressed, then encrypted, then written to file
// File contains: "encrypted:compressed:sensitive info"

String data = source.readData();
// Data gets read from file, then decrypted, then decompressed
```

Run the demo:
```bash
./gradlew :lld-design-patterns:run -PmainClass=org.example.structural.decorator.Main
```
(or run `Main` directly from the IDE)
