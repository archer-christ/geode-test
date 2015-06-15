/*
 * Copyright 2012 the original author or authors.
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

package org.springframework.data.gemfire.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.gemstone.gemfire.cache.asyncqueue.AsyncEventQueue;
import com.gemstone.gemfire.cache.wan.GatewaySender;

/**
 * Abstract base class encapsulating functionality common to all Region Parsers.
 *
 * @author David Turanski
 * @author John Blum
 */
abstract class AbstractRegionParser extends AbstractSingleBeanDefinitionParser {

	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected Class<?> getBeanClass(Element element) {
		return getRegionFactoryClass();
	}

	protected abstract Class<?> getRegionFactoryClass();

	@Override
	protected String getParentName(final Element element) {
		String regionTemplate = element.getAttribute("template");
		return (StringUtils.hasText(regionTemplate) ? regionTemplate : super.getParentName(element));
	}

	protected boolean isRegionTemplate(final Element element) {
		String localName = element.getLocalName();
		return (localName != null && localName.endsWith("-template"));
	}

	protected boolean isSubRegion(final Element element) {
		String localName = element.getParentNode().getLocalName();
		return (localName != null && localName.endsWith("region"));
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		super.doParse(element, builder);
		builder.setAbstract(isRegionTemplate(element));
        doParseRegion(element, parserContext, builder, isSubRegion(element));
	}

	protected abstract void doParseRegion(Element element, ParserContext parserContext, BeanDefinitionBuilder builder,
			boolean subRegion);

	protected void doParseCommonRegionConfiguration(Element element, ParserContext parserContext,
			BeanDefinitionBuilder regionBuilder, BeanDefinitionBuilder regionAttributesBuilder, boolean subRegion) {

		mergeTemplateRegionAttributes(element, parserContext, regionBuilder, regionAttributesBuilder);

		String resolvedCacheRef = ParsingUtils.resolveCacheReference(element.getAttribute("cache-ref"));

		if (!subRegion) {
			regionBuilder.addPropertyReference("cache", resolvedCacheRef);
			ParsingUtils.setPropertyValue(element, regionBuilder, "close");
			ParsingUtils.setPropertyValue(element, regionBuilder, "destroy");
		}

		ParsingUtils.setPropertyValue(element, regionBuilder, "name");
		ParsingUtils.setPropertyValue(element, regionBuilder, "ignore-if-exists", "lookupEnabled");
		ParsingUtils.setPropertyValue(element, regionBuilder, "data-policy");
		ParsingUtils.setPropertyValue(element, regionBuilder, "persistent");
		ParsingUtils.setPropertyValue(element, regionBuilder, "shortcut");

		if (StringUtils.hasText(element.getAttribute("disk-store-ref"))) {
			ParsingUtils.setPropertyValue(element, regionBuilder, "disk-store-ref", "diskStoreName");
			regionBuilder.addDependsOn(element.getAttribute("disk-store-ref"));
		}

		ParsingUtils.parseOptionalRegionAttributes(parserContext, element, regionAttributesBuilder);
		ParsingUtils.parseSubscription(parserContext, element, regionAttributesBuilder);
		ParsingUtils.parseStatistics(element, regionAttributesBuilder);
		ParsingUtils.parseMembershipAttributes(parserContext, element, regionAttributesBuilder);
		ParsingUtils.parseExpiration(parserContext, element, regionAttributesBuilder);
		ParsingUtils.parseEviction(parserContext, element, regionAttributesBuilder);
		ParsingUtils.parseCompressor(parserContext, element, regionAttributesBuilder);

		String enableGateway = element.getAttribute("enable-gateway");
		String hubId = element.getAttribute("hub-id");

		// Factory will enable gateway if it is not set and hub-id is set.
		if (StringUtils.hasText(enableGateway)) {
			if (GemfireUtils.isGemfireVersion7OrAbove()) {
				log.warn("'enable-gateway' has been deprecated since Gemfire 7.0");
			}
		}

		ParsingUtils.setPropertyValue(element, regionBuilder, "enable-gateway");

		if (StringUtils.hasText(hubId)) {
			if (GemfireUtils.isGemfireVersion7OrAbove()) {
				log.warn("'hub-id' has been deprecated since Gemfire 7.0");
			}
			if (!CollectionUtils.isEmpty(DomUtils.getChildElementsByTagName(element, "gateway-sender"))) {
				parserContext.getReaderContext().error("specifying both 'hub-id' and 'gateway-sender' is invalid",
					element);
			}
		}

		ParsingUtils.setPropertyValue(element, regionBuilder, "hub-id");

		parseCollectionOfCustomSubElements(element, parserContext, regionBuilder, AsyncEventQueue.class.getName(),
			"async-event-queue", "asyncEventQueues");

		parseCollectionOfCustomSubElements(element, parserContext, regionBuilder, GatewaySender.class.getName(),
			"gateway-sender", "gatewaySenders");

		List<Element> subElements = DomUtils.getChildElements(element);

		for (Element subElement : subElements) {
			if (subElement.getLocalName().equals("cache-listener")) {
				regionBuilder.addPropertyValue("cacheListeners", ParsingUtils.parseRefOrNestedBeanDeclaration(
					parserContext, subElement, regionBuilder));
			}
			else if (subElement.getLocalName().equals("cache-loader")) {
				regionBuilder.addPropertyValue("cacheLoader", ParsingUtils.parseRefOrSingleNestedBeanDeclaration(
					parserContext, subElement, regionBuilder));
			}
			else if (subElement.getLocalName().equals("cache-writer")) {
				regionBuilder.addPropertyValue("cacheWriter", ParsingUtils.parseRefOrSingleNestedBeanDeclaration(
					parserContext, subElement, regionBuilder));
			}
		}

		if (!subRegion) {
			parseSubRegions(element, parserContext, resolvedCacheRef);
		}
	}

	void mergeTemplateRegionAttributes(Element element, ParserContext parserContext,
			BeanDefinitionBuilder regionBuilder, BeanDefinitionBuilder regionAttributesBuilder) {

		String regionTemplateName = getParentName(element);

		if (StringUtils.hasText(regionTemplateName)) {
			if (parserContext.getRegistry().containsBeanDefinition(regionTemplateName)) {
				BeanDefinition templateRegion = parserContext.getRegistry().getBeanDefinition(regionTemplateName);

				BeanDefinition templateRegionAttributes = getRegionAttributesBeanDefinition(templateRegion);

				if (templateRegionAttributes != null) {
					// NOTE we only need to merge the parent RegionAttributes with this since the parent will have
					// already merged it's parent's RegionAttributes and so on...
					regionAttributesBuilder.getRawBeanDefinition().overrideFrom(templateRegionAttributes);
				}
			}
			else {
				parserContext.getReaderContext().error(String.format(
					"The Region template [%1$s] must be 'defined before' the Region [%2$s] referring to the template!",
						regionTemplateName, resolveId(element, regionBuilder.getRawBeanDefinition(), parserContext)), element);
			}
		}
	}

	BeanDefinition getRegionAttributesBeanDefinition(final BeanDefinition region) {
		Assert.notNull(region, "The 'Region' BeanDefinition must not be null!");

		Object regionAttributes = null;

		if (region.getPropertyValues().contains("attributes")) {
			PropertyValue attributesProperty = region.getPropertyValues().getPropertyValue("attributes");
			regionAttributes = attributesProperty.getValue();
		}

		return (regionAttributes instanceof BeanDefinition ? (BeanDefinition) regionAttributes : null);
	}

	protected void parseCollectionOfCustomSubElements(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder, String className, String subElementName, String propertyName) {
		List<Element> subElements = DomUtils.getChildElementsByTagName(element, subElementName,
			subElementName + "-ref");

		if (!CollectionUtils.isEmpty(subElements)) {
			ManagedArray array = new ManagedArray(className, subElements.size());

			for (Element subElement : subElements) {
				array.add(ParsingUtils.parseRefOrNestedCustomElement(parserContext, subElement, builder));
			}

			builder.addPropertyValue(propertyName, array);
		}
	}

	protected void parseSubRegions(Element element, ParserContext parserContext, String resolvedCacheRef) {
		Map<String, Element> allSubRegionElements = new HashMap<String, Element>();

		findSubRegionElements(element, getRegionNameFromElement(element), allSubRegionElements);

		if (!CollectionUtils.isEmpty(allSubRegionElements)) {
			for (Map.Entry<String, Element> entry : allSubRegionElements.entrySet()) {
				parseSubRegion(entry.getValue(), parserContext, entry.getKey(), resolvedCacheRef);
			}
		}
	}

	private void findSubRegionElements(Element parent, String parentPath, Map<String, Element> allSubRegionElements) {
		for (Element element : DomUtils.getChildElements(parent)) {
			if (element.getLocalName().endsWith("region")) {
				String subRegionName = getRegionNameFromElement(element);
				String subRegionPath = buildSubRegionPath(parentPath, subRegionName);
				allSubRegionElements.put(subRegionPath, element);
				findSubRegionElements(element, subRegionPath, allSubRegionElements);
			}
		}
	}

	private String getRegionNameFromElement(Element element) {
		String name = element.getAttribute(NAME_ATTRIBUTE);
		return (StringUtils.hasText(name) ? name : element.getAttribute(ID_ATTRIBUTE));
	}

	private String buildSubRegionPath(String parentName, String regionName) {
		String regionPath = StringUtils.arrayToDelimitedString(new String[] { parentName, regionName }, "/");
		if (!regionPath.startsWith("/")) {
			regionPath = "/" + regionPath;
		}
		return regionPath;
	}

	private BeanDefinition parseSubRegion(Element element, ParserContext parserContext, String subRegionPath,
			String cacheRef) {

		String parentBeanName = getParentRegionPathFrom(subRegionPath);
		String regionName = getRegionNameFromElement(element); // do before 'renaming' the element below

		element.setAttribute("id", subRegionPath);
		element.setAttribute("name", subRegionPath);

		BeanDefinition beanDefinition = parserContext.getDelegate().parseCustomElement(element);

		beanDefinition.getPropertyValues().add("cache", new RuntimeBeanReference(cacheRef));
		beanDefinition.getPropertyValues().add("parent", new RuntimeBeanReference(parentBeanName));
		beanDefinition.getPropertyValues().add("regionName", regionName);

		return beanDefinition;
	}

	private String getParentRegionPathFrom(String regionPath) {
		int index = regionPath.lastIndexOf("/");
		String parentPath = regionPath.substring(0, index);
		if (parentPath.lastIndexOf("/") == 0) {
			parentPath = parentPath.substring(1);
		}
		return parentPath;
	}

	protected void validateDataPolicyShortcutAttributesMutualExclusion(final Element element,
			final ParserContext parserContext) {
		if (element.hasAttribute("data-policy") && element.hasAttribute("shortcut")) {
			parserContext.getReaderContext().error(String.format(
				"Only one of [data-policy, shortcut] may be specified with element '%1$s'.", element.getTagName()),
					element);
		}
	}

}
