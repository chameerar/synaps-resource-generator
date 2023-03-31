package org.wso2.executor.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.synapse.api.API;
import org.apache.synapse.config.xml.rest.APIFactory;
import org.wso2.carbon.rest.api.APIException;
import org.wso2.carbon.rest.api.service.RestApiAdmin;
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

    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    @Override
    public void execute() {

        Path selectedPath = null;
        try (Stream<Path> paths = Files.walk(Paths.get(""))) {
            List<Path> pathList = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".xml")).collect(Collectors.toList());
            for (Path path : pathList) {
                try (Stream<String> lines = Files.lines(path)) {
                    Optional<String> pathWithStringOptional = lines.filter(line -> line.contains("<api"))
                            .findFirst();
                    if (pathWithStringOptional.isPresent()) {
                        selectedPath = path;
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(selectedPath);
        if (selectedPath == null) {
            System.out.println("No API found");
            return;
        }

        try (InputStream inputStream = new FileInputStream(selectedPath.toFile())) {
            OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(inputStream);
            OMElement documentElement = builder.getDocumentElement();
            API api = APIFactory.createAPI(documentElement);
            RestApiAdmin restApiAdmin = new RestApiAdmin();
            try {
                String swagger = restApiAdmin.generateSwaggerFromSynapseAPIByFormat(api, false);
                System.out.println(swagger);
                //write to file
                Files.write(Paths.get("swagger.json"), swagger.getBytes());

            } catch (APIException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
