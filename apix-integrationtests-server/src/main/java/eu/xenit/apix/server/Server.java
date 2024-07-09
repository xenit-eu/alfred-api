/**
 * Copyright (C) 2017 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.xenit.apix.server;

import com.github.ruediste.remoteJUnit.codeRunner.CodeRunnerStandaloneServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class Server implements ApplicationContextAware, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
	private CodeRunnerStandaloneServer codeRunnerStandaloneServer;
	private ApplicationContext applicationContext;

	public void setCodeRunnerStandaloneServer(CodeRunnerStandaloneServer codeRunnerStandaloneServer) {
		logger.error("initialise codeRunnerStandaloneServer");
		System.out.println("initialise codeRunnerStandaloneServer");
		this.codeRunnerStandaloneServer = codeRunnerStandaloneServer;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		logger.error("initialise applicationContext in Server");
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("Loading Application Context for Integration Tests...");
		ApplicationContextProvider.setApplicationContext(this.applicationContext);
		if (ApplicationContextProvider.getApplicationContext() == null){
			logger.error("Application Context Loading Failed! context=" + ApplicationContextProvider.getApplicationContext());
		}

		System.out.println("JUnit-remote server is starting...");
		Thread newThread = new Thread(() -> {
			codeRunnerStandaloneServer.startAndWait();
		});

		newThread.start();
	}
}
