/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.lesscss.loader;

import java.io.IOException;
import java.util.ArrayList;

/**
 * ResourceLoader is used to locate and load stylesheets referenced with @include
 * directive.
 * 
 * @author Rafał Krzewski
 */
public interface ResourceLoader {

	/**
	 * Checks if the given resource exists.
	 * 
	 * @param resource
	 *            relative resource file path.
	 * @param paths
	 *            paths to search for resource under.
	 * @return {@code true} if the resource exists.
	 * @throws IOException
	 *             when i/o error occurs while checking for resource existence.
	 */
	public boolean exists(String resource, String[] paths) throws IOException;

	/**
	 * Loads the given resource's contents.
	 * 
	 * @param resource
	 *            relative resource file path.
	 * @param paths
	 *            paths to search for resource under.
	 * @param loadedStack
	 *            stack of resources that have been loaded so far (empty by default)
	 * @param charset
	 *            character set name, valid with respect to
	 *            {@link java.nio.charset.Charset}.
	 * @return resource contents as a string.
	 * @throws IOException
	 *             when i/o error occurs while loading the resource, or charset
	 *             is invalid.
	 */
	public String load(String resource, String[] paths, ArrayList<String> loadedStack, String charset) throws IOException;
}
