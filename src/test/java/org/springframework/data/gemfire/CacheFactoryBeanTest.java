/*
 * Copyright 2010-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.core.io.Resource;
import org.springframework.data.util.ReflectionUtils;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheTransactionManager;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.TransactionListener;
import com.gemstone.gemfire.cache.TransactionWriter;
import com.gemstone.gemfire.cache.control.ResourceManager;
import com.gemstone.gemfire.cache.util.GatewayConflictResolver;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.Role;
import com.gemstone.gemfire.pdx.PdxSerializer;

/**
 * The CacheFactoryBeanTest class is a test suite of test cases testing the contract and functionality
 * of the CacheFactoryBean class.
 *
 * @author John Blum
 * @see org.mockito.Mockito
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see com.gemstone.gemfire.cache.Cache
 * @since 1.7.0
 */
public class CacheFactoryBeanTest {

	@Test
	public void testAfterPropertiesSet() throws Exception {
		BeanFactory mockBeanFactory = mock(BeanFactory.class, "SpringBeanFactory");
		Cache mockCache = mock(Cache.class, "GemFireCache");
		CacheTransactionManager mockCacheTransactionManager = mock(CacheTransactionManager.class, "GemFireTransactionManager");
		DistributedMember mockDistributedMember = mock(DistributedMember.class, "GemFireDistributedMember");
		DistributedSystem mockDistributedSystem = mock(DistributedSystem.class, "GemFireDistributedSystem");
		GatewayConflictResolver mockGatewayConflictResolver = mock(GatewayConflictResolver.class, "GemFireGatewayConflictResolver");
		PdxSerializer mockPdxSerializer = mock(PdxSerializer.class, "GemFirePdxSerializer");
		Resource mockCacheXml = mock(Resource.class, "GemFireCacheXml");
		ResourceManager mockResourceManager = mock(ResourceManager.class, "GemFireResourceManager");
		TransactionListener mockTransactionLister = mock(TransactionListener.class, "GemFireTransactionListener");
		TransactionWriter mockTransactionWriter = mock(TransactionWriter.class, "GemFireTransactionWriter");

		final CacheFactory mockCacheFactory = mock(CacheFactory.class, "GemFireCacheFactory");

		when(mockCacheFactory.create()).thenReturn(mockCache);
		when(mockCache.getCacheTransactionManager()).thenReturn(mockCacheTransactionManager);
		when(mockCache.getDistributedSystem()).thenReturn(mockDistributedSystem);
		when(mockCache.getResourceManager()).thenReturn(mockResourceManager);
		when(mockCacheXml.getInputStream()).thenReturn(mock(InputStream.class));
		when(mockDistributedSystem.getDistributedMember()).thenReturn(mockDistributedMember);
		when(mockDistributedSystem.getName()).thenReturn("MockDistributedSystem");
		when(mockDistributedMember.getId()).thenReturn("MockDistributedMember");
		when(mockDistributedMember.getGroups()).thenReturn(Collections.<String>emptyList());
		when(mockDistributedMember.getRoles()).thenReturn(Collections.<Role>emptySet());
		when(mockDistributedMember.getHost()).thenReturn("skullbox");
		when(mockDistributedMember.getProcessId()).thenReturn(12345);

		final ClassLoader expectedThreadContextClassLoader = Thread.currentThread().getContextClassLoader();

		final Properties gemfireProperties = new Properties();

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected Object createFactory(final Properties actualGemfireProperties) {
				assertSame(gemfireProperties, actualGemfireProperties);
				assertSame(ClassLoader.getSystemClassLoader(), getBeanClassLoader());
				return mockCacheFactory;
			}
		};

		cacheFactoryBean.setBeanClassLoader(ClassLoader.getSystemClassLoader());
		cacheFactoryBean.setBeanFactory(mockBeanFactory);
		cacheFactoryBean.setBeanName("TestGemFireCache");
		cacheFactoryBean.setCacheXml(mockCacheXml);
		cacheFactoryBean.setCopyOnRead(true);
		cacheFactoryBean.setCriticalHeapPercentage(0.90f);
		cacheFactoryBean.setDynamicRegionSupport(null);
		cacheFactoryBean.setEnableAutoReconnect(false);
		cacheFactoryBean.setEvictionHeapPercentage(0.75f);
		cacheFactoryBean.setGatewayConflictResolver(mockGatewayConflictResolver);
		cacheFactoryBean.setJndiDataSources(null);
		cacheFactoryBean.setLazyInitialize(false);
		cacheFactoryBean.setLockLease(15000);
		cacheFactoryBean.setLockTimeout(5000);
		cacheFactoryBean.setMessageSyncInterval(20000);
		cacheFactoryBean.setPdxSerializer(mockPdxSerializer);
		cacheFactoryBean.setPdxReadSerialized(true);
		cacheFactoryBean.setPdxPersistent(true);
		cacheFactoryBean.setPdxIgnoreUnreadFields(false);
		cacheFactoryBean.setPdxDiskStoreName("TestPdxDiskStore");
		cacheFactoryBean.setProperties(gemfireProperties);
		cacheFactoryBean.setSearchTimeout(45000);
		cacheFactoryBean.setTransactionListeners(Collections.singletonList(mockTransactionLister));
		cacheFactoryBean.setTransactionWriter(mockTransactionWriter);

		assertTrue(gemfireProperties.isEmpty());

		cacheFactoryBean.afterPropertiesSet();

		assertEquals(2, gemfireProperties.size());
		assertTrue(gemfireProperties.containsKey("disable-auto-reconnect"));
		assertTrue(gemfireProperties.containsKey("use-cluster-configuration"));
		assertEquals("true", gemfireProperties.getProperty("disable-auto-reconnect"));
		assertEquals("false", gemfireProperties.getProperty("use-cluster-configuration"));
		assertSame(expectedThreadContextClassLoader, Thread.currentThread().getContextClassLoader());

		GemfireBeanFactoryLocator beanFactoryLocator = cacheFactoryBean.getBeanFactoryLocator();

		assertNotNull(beanFactoryLocator);

		BeanFactoryReference beanFactoryReference = beanFactoryLocator.useBeanFactory("TestGemFireCache");

		assertNotNull(beanFactoryReference);
		assertSame(mockBeanFactory, beanFactoryReference.getFactory());

		verify(mockCacheFactory, times(1)).setPdxDiskStore(eq("TestPdxDiskStore"));
		verify(mockCacheFactory, times(1)).setPdxIgnoreUnreadFields(eq(false));
		verify(mockCacheFactory, times(1)).setPdxPersistent(eq(true));
		verify(mockCacheFactory, times(1)).setPdxReadSerialized(eq(true));
		verify(mockCacheFactory, times(1)).setPdxSerializer(eq(mockPdxSerializer));
		verify(mockCacheFactory, times(1)).create();
		verify(mockCache, times(1)).loadCacheXml(any(InputStream.class));
		verify(mockCache, times(1)).setCopyOnRead(eq(true));
		verify(mockCache, times(1)).setGatewayConflictResolver(same(mockGatewayConflictResolver));
		verify(mockCache, times(1)).setLockLease(eq(15000));
		verify(mockCache, times(1)).setLockTimeout(eq(5000));
		verify(mockCache, times(1)).setMessageSyncInterval(eq(20000));
		verify(mockCache, times(1)).setSearchTimeout(eq(45000));
		verify(mockCache, times(2)).getResourceManager();
		verify(mockResourceManager, times(1)).setCriticalHeapPercentage(eq(0.90f));
		verify(mockResourceManager, times(1)).setEvictionHeapPercentage(eq(0.75f));
		verify(mockCache, times(2)).getCacheTransactionManager();
		verify(mockCacheTransactionManager, times(1)).addListener(same(mockTransactionLister));
		verify(mockCacheTransactionManager, times(1)).setWriter(same(mockTransactionWriter));
	}

	@Test
	public void testFetchExistingCache() throws Exception {
		Cache mockCache = mock(Cache.class, "GemFireCache");

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		ReflectionUtils.setField(CacheFactoryBean.class.getDeclaredField("cache"), cacheFactoryBean, mockCache);

		Cache actualCache = cacheFactoryBean.resolveCache();

		assertSame(mockCache, actualCache);
	}

	@Test
	public void testCreateWhenCacheExists() throws Exception {
		Cache mockCache = mock(Cache.class, "GemFireCache");

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		ReflectionUtils.setField(CacheFactoryBean.class.getDeclaredField("cache"), cacheFactoryBean, mockCache);

		GemFireCache actualCache = cacheFactoryBean.createCache(null);

		assertSame(mockCache, actualCache);
	}

	@Test
	public void testCreateFactory() {
		Properties gemfireProperties = new Properties();
		Object cacheFactoryReference = new CacheFactoryBean().createFactory(gemfireProperties);

		assertTrue(gemfireProperties.isEmpty());
		assertTrue(cacheFactoryReference instanceof CacheFactory);

		CacheFactory cacheFactory = (CacheFactory) cacheFactoryReference;

		cacheFactory.set("name", "TestCreateCacheFactory");

		assertTrue(gemfireProperties.containsKey("name"));
		assertEquals("TestCreateCacheFactory", gemfireProperties.getProperty("name"));
	}

	@Test
	public void testPrepareFactory() {
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setPdxSerializer(mock(PdxSerializer.class, "MockGemFirePdxSerializer"));
		cacheFactoryBean.setPdxReadSerialized(true);
		cacheFactoryBean.setPdxIgnoreUnreadFields(false);

		CacheFactory mockCacheFactory = mock(CacheFactory.class, "MockGemFireCacheFactory");

		cacheFactoryBean.prepareFactory(mockCacheFactory);

		verify(mockCacheFactory, times(1)).setPdxSerializer(any(PdxSerializer.class));
		verify(mockCacheFactory, never()).setPdxDiskStore(any(String.class));
		verify(mockCacheFactory, times(1)).setPdxIgnoreUnreadFields(eq(false));
		verify(mockCacheFactory, never()).setPdxPersistent(any(Boolean.class));
		verify(mockCacheFactory, times(1)).setPdxReadSerialized(eq(true));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPostProcessCacheWithInvalidCriticalHeapPercentage() throws Exception {
		try {
			CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

			cacheFactoryBean.setCriticalHeapPercentage(200.0f);
			cacheFactoryBean.postProcess(null);
		}
		catch (IllegalArgumentException expected) {
			assertEquals("'criticalHeapPercentage' (200.0) is invalid; must be > 0.0 and <= 100.0",
				expected.getMessage());
			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPostProcessCacheWithInvalidEvictionHeapPercentage() throws Exception {
		try {
			CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

			cacheFactoryBean.setEvictionHeapPercentage(-75.0f);
			cacheFactoryBean.postProcess(null);
		}
		catch (IllegalArgumentException expected) {
			assertEquals("'evictionHeapPercentage' (-75.0) is invalid; must be > 0.0 and <= 100.0",
				expected.getMessage());
			throw expected;
		}
	}

	@Test
	public void testPostProcessPropertiesBeforeInitializationDefaults() {
		assumeTrue(GemfireUtils.isGemfireVersion8OrAbove());

		Properties gemfireProperties = new Properties();

		assertTrue(gemfireProperties.isEmpty());

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.postProcessPropertiesBeforeInitialization(gemfireProperties);

		assertEquals(2, gemfireProperties.size());
		assertTrue(gemfireProperties.containsKey("disable-auto-reconnect"));
		assertTrue(gemfireProperties.containsKey("use-cluster-configuration"));
		assertEquals("true", gemfireProperties.getProperty("disable-auto-reconnect"));
		assertEquals("false", gemfireProperties.getProperty("use-cluster-configuration"));
	}

	@Test
	public void testPostProcessPropertiesBeforeInitializationDisabled() {
		assumeTrue(GemfireUtils.isGemfireVersion8OrAbove());

		Properties gemfireProperties = new Properties();

		assertTrue(gemfireProperties.isEmpty());

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setEnableAutoReconnect(false);
		cacheFactoryBean.setUseClusterConfiguration(false);
		cacheFactoryBean.postProcessPropertiesBeforeInitialization(gemfireProperties);

		assertEquals(2, gemfireProperties.size());
		assertTrue(gemfireProperties.containsKey("disable-auto-reconnect"));
		assertTrue(gemfireProperties.containsKey("use-cluster-configuration"));
		assertEquals("true", gemfireProperties.getProperty("disable-auto-reconnect"));
		assertEquals("false", gemfireProperties.getProperty("use-cluster-configuration"));
	}

	@Test
	public void testPostProcessPropertiesBeforeInitializationEnabled() {
		assumeTrue(GemfireUtils.isGemfireVersion8OrAbove());

		Properties gemfireProperties = new Properties();

		assertTrue(gemfireProperties.isEmpty());

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setEnableAutoReconnect(true);
		cacheFactoryBean.setUseClusterConfiguration(true);
		cacheFactoryBean.postProcessPropertiesBeforeInitialization(gemfireProperties);

		assertEquals(2, gemfireProperties.size());
		assertTrue(gemfireProperties.containsKey("disable-auto-reconnect"));
		assertTrue(gemfireProperties.containsKey("use-cluster-configuration"));
		assertEquals("false", gemfireProperties.getProperty("disable-auto-reconnect"));
		assertEquals("true", gemfireProperties.getProperty("use-cluster-configuration"));
	}

	@Test
	public void testGetObject() throws Exception {
		final ClassLoader expectedThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
		final Cache mockCache = mock(Cache.class, "GemFireCache");

		DistributedMember mockDistributedMember = mock(DistributedMember.class, "GemFireDistributedMember");
		DistributedSystem mockDistributedSystem = mock(DistributedSystem.class, "GemFireDistributedSystem");

		when(mockCache.getDistributedSystem()).thenReturn(mockDistributedSystem);
		when(mockDistributedSystem.getDistributedMember()).thenReturn(mockDistributedMember);
		when(mockDistributedSystem.getName()).thenReturn("MockDistributedSystem");
		when(mockDistributedMember.getId()).thenReturn("MockDistributedMember");
		when(mockDistributedMember.getGroups()).thenReturn(Collections.<String>emptyList());
		when(mockDistributedMember.getRoles()).thenReturn(Collections.<Role>emptySet());
		when(mockDistributedMember.getHost()).thenReturn("skullbox");
		when(mockDistributedMember.getProcessId()).thenReturn(67890);

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected GemFireCache fetchCache() {
				assertSame(ClassLoader.getSystemClassLoader(), getBeanClassLoader());
				return mockCache;
			}
		};

		cacheFactoryBean.setBeanClassLoader(ClassLoader.getSystemClassLoader());
		cacheFactoryBean.setBeanName("MockGemFireCache");
		cacheFactoryBean.setCopyOnRead(true);
		cacheFactoryBean.setLockTimeout(5000);
		cacheFactoryBean.setSearchTimeout(15000);
		cacheFactoryBean.setUseBeanFactoryLocator(false);

		Cache actualCache = cacheFactoryBean.getObject();

		assertSame(mockCache, actualCache);
		assertSame(expectedThreadContextClassLoader, Thread.currentThread().getContextClassLoader());

		verify(mockCache, times(1)).setCopyOnRead(eq(true));
		verify(mockCache, times(1)).setLockTimeout(eq(5000));
		verify(mockCache, times(1)).setSearchTimeout(eq(15000));
		verify(mockCache, never()).getCacheTransactionManager();
		verify(mockCache, never()).getResourceManager();
		verify(mockCache, never()).loadCacheXml(any(InputStream.class));
		verify(mockCache, never()).setGatewayConflictResolver(any(GatewayConflictResolver.class));
		verify(mockCache, never()).setLockLease(anyInt());
		verify(mockCache, never()).setMessageSyncInterval(anyInt());
	}

	@Test
	public void testGetObjectType() {
		assertEquals(Cache.class, new CacheFactoryBean().getObjectType());
	}

	@Test
	public void testIsSingleton() {
		assertTrue(new CacheFactoryBean().isSingleton());
	}

	@Test
	public void testDestroy() throws Exception {
		final Cache mockCache = mock(Cache.class, "GemFireCache");

		GemfireBeanFactoryLocator mockGemfireBeanFactoryLocator = mock(GemfireBeanFactoryLocator.class);

		when(mockCache.isClosed()).thenReturn(false);

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected GemFireCache fetchCache() {
				return mockCache;
			}
		};

		ReflectionUtils.setField(CacheFactoryBean.class.getDeclaredField("beanFactoryLocator"), cacheFactoryBean,
			mockGemfireBeanFactoryLocator);

		cacheFactoryBean.setClose(true);
		cacheFactoryBean.setUseBeanFactoryLocator(true);
		cacheFactoryBean.destroy();

		verify(mockCache, times(1)).isClosed();
		verify(mockCache, times(1)).close();
		verify(mockGemfireBeanFactoryLocator, times(1)).destroy();
	}

	@Test
	public void testDestroyWhenCacheIsNull() throws Exception {
		final AtomicBoolean fetchCacheCalled = new AtomicBoolean(false);

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected GemFireCache fetchCache() {
				fetchCacheCalled.set(true);
				return null;
			}
		};

		cacheFactoryBean.setClose(true);
		cacheFactoryBean.setUseBeanFactoryLocator(true);
		cacheFactoryBean.destroy();

		assertTrue(fetchCacheCalled.get());
	}

	@Test
	public void testDestroyWhenClosedIsFalse() throws Exception {
		final AtomicBoolean fetchCacheCalled = new AtomicBoolean(false);
		final Cache mockCache = mock(Cache.class, "GemFireCache");

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected GemFireCache fetchCache() {
				fetchCacheCalled.set(true);
				return mockCache;
			}
		};

		cacheFactoryBean.setClose(false);
		cacheFactoryBean.setUseBeanFactoryLocator(false);
		cacheFactoryBean.destroy();

		verify(mockCache, never()).isClosed();
		verify(mockCache, never()).close();

		assertFalse(fetchCacheCalled.get());
	}

	@Test
	public void testSetAndGetCacheFactoryBeanProperties() throws Exception {
		BeanFactory mockBeanFactory = mock(BeanFactory.class, "SpringBeanFactory");
		GatewayConflictResolver mockGatewayConflictResolver = mock(GatewayConflictResolver.class, "GemFireGatewayConflictResolver");
		PdxSerializer mockPdxSerializer = mock(PdxSerializer.class, "GemFirePdxSerializer");
		Resource mockCacheXml = mock(Resource.class, "GemFireCacheXml");
		TransactionListener mockTransactionListener = mock(TransactionListener.class, "GemFireTransactionListener");
		TransactionWriter mockTransactionWriter = mock(TransactionWriter.class, "GemFireTransactionWriter");

		Properties gemfireProperties = new Properties();

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
		cacheFactoryBean.setBeanFactory(mockBeanFactory);
		cacheFactoryBean.setBeanName("TestCache");
		cacheFactoryBean.setCacheXml(mockCacheXml);
		cacheFactoryBean.setProperties(gemfireProperties);
		cacheFactoryBean.setLazyInitialize(false);
		cacheFactoryBean.setUseBeanFactoryLocator(false);
		cacheFactoryBean.setClose(false);
		cacheFactoryBean.setCopyOnRead(true);
		cacheFactoryBean.setDynamicRegionSupport(new CacheFactoryBean.DynamicRegionSupport());
		cacheFactoryBean.setEnableAutoReconnect(true);
		cacheFactoryBean.setCriticalHeapPercentage(0.95f);
		cacheFactoryBean.setEvictionHeapPercentage(0.70f);
		cacheFactoryBean.setGatewayConflictResolver(mockGatewayConflictResolver);
		cacheFactoryBean.setJndiDataSources(Collections.singletonList(new CacheFactoryBean.JndiDataSource()));
		cacheFactoryBean.setLockLease(15000);
		cacheFactoryBean.setLockTimeout(5000);
		cacheFactoryBean.setMessageSyncInterval(10000);
		cacheFactoryBean.setPdxSerializer(mockPdxSerializer);
		cacheFactoryBean.setPdxReadSerialized(false);
		cacheFactoryBean.setPdxPersistent(true);
		cacheFactoryBean.setPdxIgnoreUnreadFields(true);
		cacheFactoryBean.setPdxDiskStoreName("TestPdxDiskStore");
		cacheFactoryBean.setSearchTimeout(30000);
		cacheFactoryBean.setTransactionListeners(Collections.singletonList(mockTransactionListener));
		cacheFactoryBean.setTransactionWriter(mockTransactionWriter);
		cacheFactoryBean.setUseClusterConfiguration(true);

		assertEquals(Thread.currentThread().getContextClassLoader(), cacheFactoryBean.getBeanClassLoader());
		assertSame(mockBeanFactory, cacheFactoryBean.getBeanFactory());
		assertNull(cacheFactoryBean.getBeanFactoryLocator());
		assertEquals("TestCache", cacheFactoryBean.getBeanName());
		assertSame(mockCacheXml, cacheFactoryBean.getCacheXml());
		assertSame(gemfireProperties, cacheFactoryBean.getProperties());
		assertFalse(cacheFactoryBean.isLazyInitialize());
		assertTrue(Boolean.FALSE.equals(TestUtils.readField("useBeanFactoryLocator", cacheFactoryBean)));
		assertTrue(Boolean.FALSE.equals(TestUtils.readField("close", cacheFactoryBean)));
		assertTrue(cacheFactoryBean.getCopyOnRead());
		assertEquals(0.95f, cacheFactoryBean.getCriticalHeapPercentage().floatValue(), 0.0f);
		assertNotNull(cacheFactoryBean.getDynamicRegionSupport());
		assertTrue(cacheFactoryBean.getEnableAutoReconnect());
		assertEquals(0.70f, cacheFactoryBean.getEvictionHeapPercentage().floatValue(), 0.0f);
		assertSame(mockGatewayConflictResolver, cacheFactoryBean.getGatewayConflictResolver());
		assertNotNull(cacheFactoryBean.getJndiDataSources());
		assertEquals(1, cacheFactoryBean.getJndiDataSources().size());
		assertEquals(15000, cacheFactoryBean.getLockLease().intValue());
		assertEquals(5000, cacheFactoryBean.getLockTimeout().intValue());
		assertEquals(10000, cacheFactoryBean.getMessageSyncInterval().intValue());
		assertSame(mockPdxSerializer, cacheFactoryBean.getPdxSerializer());
		assertFalse(cacheFactoryBean.getPdxReadSerialized());
		assertTrue(cacheFactoryBean.getPdxPersistent());
		assertTrue(cacheFactoryBean.getPdxIgnoreUnreadFields());
		assertEquals("TestPdxDiskStore", cacheFactoryBean.getPdxDiskStoreName());
		assertEquals(30000, cacheFactoryBean.getSearchTimeout().intValue());
		assertNotNull(cacheFactoryBean.getTransactionListeners());
		assertEquals(1, cacheFactoryBean.getTransactionListeners().size());
		assertSame(mockTransactionListener, cacheFactoryBean.getTransactionListeners().get(0));
		assertSame(mockTransactionWriter, cacheFactoryBean.getTransactionWriter());
		assertTrue(cacheFactoryBean.getUseClusterConfiguration());
	}

}
