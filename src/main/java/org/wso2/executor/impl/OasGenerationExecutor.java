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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OasGenerationExecutor implements Executor {

    private static final String OUTPUT_FORMAT = "output-format";
    private static final String JSON = "json";
    private static final String API_FILE = "api-file";
    private static final String PROCESS_ONE_API = "process-one";

    private static final String OUTPUT_SWAGGER_FILE = "output-file";

    private static final String DEFAULT_SINGLE_SWAGGER_FILE_PREFIX = "default";

    private boolean isOutputJson = false;
    private String apiFileName = null;
    private boolean processOneApi = false;
    private String outputSwaggerFileName = null;

    public OasGenerationExecutor(String[] args) {

        init(args);
    }

    @Override
    public void init(String[] args) {

        if (args.length < 2) {
            return;
        }

        Map<String, String> argsMap = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            String[] argSplit = args[i].split("=");
            if (argSplit.length < 2) {
                continue;
            }
            argsMap.put(argSplit[0], argSplit[1]);
        }

        mapConfigs(argsMap);
    }

    @Override
    public void execute() throws ExecutorException {

        List<Path> selectedPaths = getApiFilePaths();
        if (selectedPaths.isEmpty()) {
            throw new ExecutorException("No API file found");
        }
        for (Path path : selectedPaths) {
            generateOasFile(path);
        }
    }

    private List<Path> getApiFilePaths() throws ExecutorException {

        List<Path> selectedPaths = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(""))) {
            List<Path> pathsList = paths.collect(Collectors.toList());
            //If the api file name is given, get the file path for that file name
            if (apiFileName != null) {
                return getApiFilePathForFileName(pathsList);
            }
            for (Path path : pathsList) {
                if (Files.isRegularFile(path) && path.toString().endsWith(".xml")) {
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        while (reader.ready()) {
                            String line = reader.readLine();
                            if (line.contains("<api")) {
                                selectedPaths.add(path);
                                //If the process one api flag is set, return the selected paths with one path
                                if (processOneApi) {
                                    return selectedPaths;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ExecutorException("Error while detecting the API file", e);
        }
        return selectedPaths;
    }

    private List<Path> getApiFilePathForFileName(List<Path> pathsList) {

        List<Path> selectedPaths = new ArrayList<>();
        for (Path path : pathsList) {
            if (Files.isRegularFile(path) && path.getFileName().toString().equals(apiFileName)) {
                selectedPaths.add(path);
                return selectedPaths;
            }
        }
        return selectedPaths;
    }

    private void generateOasFile(Path selectedPath) throws ExecutorException {

        try (InputStream inputStream = new FileInputStream(selectedPath.toFile())) {
            OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(inputStream);
            OMElement documentElement = builder.getDocumentElement();
            API api = APIFactory.createAPI(documentElement);
            RestApiAdmin restApiAdmin = new RestApiAdmin();
            final String swagger = generateSwaggerFromApi(api, restApiAdmin);
            //write to file
            final String fileSuffix = isOutputJson ? "-swagger.json" : "-swagger.yaml";
            String fileName = api.getName() + fileSuffix;
            if (processOneApi) {
                fileName = outputSwaggerFileName != null ? outputSwaggerFileName : DEFAULT_SINGLE_SWAGGER_FILE_PREFIX + fileSuffix;
            }
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

    private void mapConfigs(Map<String, String> argMap){
        if (argMap.containsKey(OUTPUT_FORMAT) && argMap.get(OUTPUT_FORMAT).equalsIgnoreCase(JSON)) {
            isOutputJson = true;
        }
        if (argMap.containsKey(API_FILE)) {
            apiFileName = argMap.get(API_FILE);
        }
        if (argMap.containsKey(PROCESS_ONE_API) && argMap.get(PROCESS_ONE_API).equalsIgnoreCase(Boolean.TRUE.toString())) {
            processOneApi = true;
        }
        if (argMap.containsKey(OUTPUT_SWAGGER_FILE)) {
            outputSwaggerFileName = argMap.get(OUTPUT_SWAGGER_FILE);
        }
    }

}
