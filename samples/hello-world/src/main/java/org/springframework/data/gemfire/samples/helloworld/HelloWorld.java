/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.samples.helloworld;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * Main bean for interacting with the cache from the console. 
 * 
 * @author Costin Leau
 */
@Component
public class HelloWorld {

	private static final Log log = LogFactory.getLog(HelloWorld.class);

	// inject the region
	private Region<String, String> region;

	@Resource
	private CommandProcessor processor;

	@PostConstruct
	void start() {
		ClientCache cache = new ClientCacheFactory()
				.addPoolLocator("172.31.4.121", 10334)
				.addPoolLocator("172.31.4.122", 10334)
				.create();

		region = cache.<String, String>createClientRegionFactory(ClientRegionShortcut.PROXY)
				.create("region");

		log.info("Member " + region.getCache().getDistributedSystem().getDistributedMember().getId()
				+ " connecting to region [" + region.getName() + "]");
		System.out.println("Member " + region.getCache().getDistributedSystem().getDistributedMember().getId()
				+ " connecting to region [" + region.getName() + "]");
		processor.setRegion(region);
		processor.start();
	}

	@PreDestroy
	void stop() throws Exception {
		log.info("Member " + region.getCache().getDistributedSystem().getDistributedMember().getId()
				+ " disconnecting from region [" + region.getName() + "]");
		System.out.println("Member " + region.getCache().getDistributedSystem().getDistributedMember().getId()
				+ " disconnecting from region [" + region.getName() + "]");
		processor.stop();
	}

	public void greetWorld() {
		try {
			processor.awaitCommands();
		} catch (Exception ex) {
			throw new IllegalStateException("Cannot greet world", ex);
		}
	}
}