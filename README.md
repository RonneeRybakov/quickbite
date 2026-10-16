# QuickBite – Restaurant Pickup Prototype
Plain Java prototype (JDK only). Requires Java 17 or newer.

## Run
Run `com.quickbite.App` from the IDE, or from the terminal:
```
javac -d out $(find src/main/java -name "*.java")       # macOS/Linux/Git Bash
java -cp out com.quickbite.App
```

Then open http://localhost:8080

## Test
Run `CartTest` from the IDE, or with Maven: `mvn test` (Maven is only used to fetch JUnit).
