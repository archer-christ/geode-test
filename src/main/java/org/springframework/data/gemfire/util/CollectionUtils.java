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

package org.springframework.data.gemfire.util;

import java.util.Collection;
import java.util.Collections;

/**
 * The CollectionUtils class is a utility class for working with Java Collections Framework and classes.
 *
 * @author John Blum
 * @see java.util.Collection
 * @see java.util.Collections
 * @see org.springframework.util.CollectionUtils
 * @since 1.7.0
 */
@SuppressWarnings("unused")
public abstract class CollectionUtils extends org.springframework.util.CollectionUtils {

	/**
	 * A null-safe operation returning the original Collection is non-null or an empty Collection
	 * (implemented with List) if null.
	 *
	 * @param <T> the element class type of the Collection.
	 * @param collection the Collection to evaluate for being null.
	 * @return the given Collection if not null, otherwise return an empty Collection (List).
	 * @see java.util.Collections#emptyList()
	 */
	public static <T> Collection<T> nullSafeCollection(final Collection<T> collection) {
		return (collection != null ? collection : Collections.<T>emptyList());
	}

}
