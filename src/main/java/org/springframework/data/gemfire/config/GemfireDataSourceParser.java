/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.data.gemfire.config;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.data.gemfire.client.GemfireDataSourcePostProcessor;
import org.w3c.dom.Element;

/**
 * @author David Turanski
 * @author John Blum
 */
public class GemfireDataSourceParser extends AbstractBeanDefinitionParser {

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.AbstractBeanDefinitionParser#parseInternal(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		AbstractBeanDefinition poolDefinition = (AbstractBeanDefinition) new PoolParser().parse(element, parserContext);

		MutablePropertyValues poolProperties = poolDefinition.getPropertyValues();

		poolProperties.add("name", GemfireConstants.DEFAULT_GEMFIRE_POOL_NAME);

		if (!element.hasAttribute("subscription-enabled")) {
			poolProperties.add("subscriptionEnabled", true);
		}
		
		parserContext.getRegistry().registerBeanDefinition(GemfireConstants.DEFAULT_GEMFIRE_POOL_NAME, poolDefinition);

		AbstractBeanDefinition clientCacheDefinition = (AbstractBeanDefinition) new ClientCacheParser()
			.parse(element, parserContext);

		MutablePropertyValues clientCacheProperties = clientCacheDefinition.getPropertyValues();

		clientCacheProperties.add("pool", poolDefinition);

		parserContext.getRegistry().registerBeanDefinition(GemfireConstants.DEFAULT_GEMFIRE_CACHE_NAME,
			clientCacheDefinition);

		System.out.printf("Registered GemFire Client Cache bean '%1$s' of type '%2$s'%n",
			GemfireConstants.DEFAULT_GEMFIRE_CACHE_NAME, clientCacheDefinition.getBeanClassName());

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
			GemfireDataSourcePostProcessor.class);

		builder.addConstructorArgReference(GemfireConstants.DEFAULT_GEMFIRE_CACHE_NAME);

		BeanDefinitionReaderUtils.registerWithGeneratedName(builder.getBeanDefinition(), parserContext.getRegistry());

		return null;
	}

}
