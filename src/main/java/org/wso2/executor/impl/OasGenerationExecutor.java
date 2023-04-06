package org.wso2.executor.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.synapse.api.API;
import org.apache.synapse.config.xml.rest.APIFactory;
import org.wso2.carbon.rest.api.APIException;
import org.wso2.carbon.rest.api.service.RestApiAdmin;
import org.wso2.exception.ExecutorException;
import org.wso2.executor.Executor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OasGenerationExecutor implements Executor {

    private static final String OUTPUT = "output";
    private static final String JSON = "json";
    private boolean isOutputJson = false;

    public OasGenerationExecutor(String[] args) {

        init(args);
    }

    @Override
    public void init(String[] args) {

        if (args.length < 2) {
            return;
        }
        String outputArg = args[1];
        String[] outArgSplit = outputArg.split("=");
        if (outArgSplit.length < 2) {
            return;
        }
        if (outArgSplit[0].equalsIgnoreCase(OUTPUT) && outArgSplit[1].equalsIgnoreCase(JSON)) {
            isOutputJson = true;
        }
    }

    @Override
    public void execute() throws ExecutorException {

        Path selectedPath = null;
        try (Stream<Path> paths = Files.walk(Paths.get(""))) {
            List<Path> pathList = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".xml")).collect(Collectors.toList());
            selectedPath = selectApiFilePath(pathList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (selectedPath == null) {
            throw new ExecutorException("No API file found");
        }

        try (InputStream inputStream = new FileInputStream(selectedPath.toFile())) {
            OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(inputStream);
            OMElement documentElement = builder.getDocumentElement();
            API api = APIFactory.createAPI(documentElement);
            RestApiAdmin restApiAdmin = new RestApiAdmin();
            final String swagger = generateSwaggerFromApi(api, restApiAdmin);
            //write to file
            final String fileName = isOutputJson ? "swagger.json" : "swagger.yaml";
            Files.write(Paths.get(fileName), swagger.getBytes());
            System.out.printf("%s generated successfully", fileName);
        } catch (IOException e) {
            throw new ExecutorException("Error while reading API file", e);
        }
    }

    private Path selectApiFilePath(List<Path> paths) throws ExecutorException {
        for (Path path : paths) {
            try (Stream<String> lines = Files.lines(path)) {
                Optional<String> pathWithStringOptional = lines.filter(line -> line.contains("<api"))
                        .findFirst();
                if (pathWithStringOptional.isPresent()) {
                    return path;
                }
            } catch (IOException e) {
                throw new ExecutorException("Couldn't find an API file in the project", e);
            }
        }
        return null;
    }

    private String generateSwaggerFromApi(API api, RestApiAdmin restApiAdmin) throws ExecutorException {
        try {
            return restApiAdmin.generateSwaggerFromSynapseAPIByFormat(api, isOutputJson);
        } catch (APIException e) {
            throw new ExecutorException("Error while generating swagger", e);
        }
    }

}
