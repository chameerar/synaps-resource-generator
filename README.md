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
2. Run the command in terminal. Use `oas` to specify the swagger generation operation.
```
java -jar synaps-resource-generator-1.0-SNAPSHOT-jar-with-dependencies.jar oas
```
By default a yaml file will be generated. You can use the `output=json` option to generate the swagger file in json format.
```
3. Check the generated swagger file in the same directory
