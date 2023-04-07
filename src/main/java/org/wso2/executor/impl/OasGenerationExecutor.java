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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

        List<Path> selectedPaths = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(""))) {
            for (Path path : paths.collect(Collectors.toList())) {
                if (Files.isRegularFile(path) && path.toString().endsWith(".xml")) {
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        while (reader.ready()) {
                            String line = reader.readLine();
                            if (line.contains("<api")) {
                                selectedPaths.add(path);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ExecutorException("Error while detecting the API file", e);
        }
        if (selectedPaths.isEmpty()) {
            throw new ExecutorException("No API file found");
        }
        for (Path path : selectedPaths) {
            generateOasFile(path);
        }
    }

    private void generateOasFile(Path selectedPath) throws ExecutorException {

        try (InputStream inputStream = new FileInputStream(selectedPath.toFile())) {
            OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(inputStream);
            OMElement documentElement = builder.getDocumentElement();
            API api = APIFactory.createAPI(documentElement);
            RestApiAdmin restApiAdmin = new RestApiAdmin();
            final String swagger = generateSwaggerFromApi(api, restApiAdmin);
            //write to file
            final String fileSuffix = isOutputJson ? "_swagger.json" : "_swagger.yaml";
            final String fileName = api.getName() + fileSuffix;
            Files.write(Paths.get(fileName), swagger.getBytes());
            System.out.printf("%s generated successfully", fileName);
        } catch (IOException e) {
            throw new ExecutorException("Error while reading API file", e);
        }
    }

    private String generateSwaggerFromApi(API api, RestApiAdmin restApiAdmin) throws ExecutorException {

        try {
            return restApiAdmin.generateSwaggerFromSynapseAPIByFormat(api, isOutputJson);
        } catch (APIException e) {
            throw new ExecutorException("Error while generating swagger", e);
        }
    }

}
