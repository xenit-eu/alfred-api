/**
 * Copyright (C) 2017 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package eu.xenit.alfred.api.server;

import com.github.ruediste.remoteJUnit.codeRunner.CodeRunnerStandaloneServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

public class Server implements InitializingBean {

    private static ApplicationContext applicationContext;

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private CodeRunnerStandaloneServer codeRunnerStandaloneServer;

    public void setCodeRunnerStandaloneServer(CodeRunnerStandaloneServer codeRunnerStandaloneServer) {
        this.codeRunnerStandaloneServer = codeRunnerStandaloneServer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread newThread = new Thread(() -> {
            codeRunnerStandaloneServer.startAndWait();
        });
        newThread.start();
    }

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}