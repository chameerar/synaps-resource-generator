# synaps-resource-generator

## Prerequisits
- JDK 11
- Maven 3.6.3

## Steps to build

1. Checkout the `main` branch
2. Build the project using maven
```
mvn clean package
```

## Steps to run

1. Copy the `synaps-resource-generator-1.0-SNAPSHOT-jar-with-dependencies.jar` from in the `target` directory to the root of the desired synaps integration project.
2. Run the command in terminal
```
java -jar synaps-resource-generator-1.0-SNAPSHOT-jar-with-dependencies.jar
```
3. Check the generated swagger.json file in the same directory
