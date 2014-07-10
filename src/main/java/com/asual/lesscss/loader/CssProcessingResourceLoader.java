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
 * A {@link ResourceLoader} that allows using lesscss @include in plain .css
 * files.
 * 
 * Multiple levels of nesting are achieved by combining these two actions:
 * <ol>
 * <li>When a resource is loaded all .css strings are replaced with .less</li>
 * <li>When a .less include file is requested and it does not exist, the
 * extension is changed to .css and lookup is retried.</li>
 * </ol>
 * 
 * @author Rafał Krzewski
 */
public class CssProcessingResourceLoader implements ResourceLoader {

	private final ResourceLoader delegate;

	public CssProcessingResourceLoader(ResourceLoader delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean exists(String resource, String[] paths) throws IOException {
		return delegate.exists(resource, paths)
				|| delegate.exists(resource.replaceFirst("\\.less$", ".css"), paths);
	}

	@Override
	public String load(String resource, String[] paths, ArrayList<String> loadedStack, String charset) throws IOException {
		String content;
		if (delegate.exists(resource, paths)) {
			content = delegate.load(resource, paths, loadedStack, charset);
		} else {
			content = delegate.load(resource.replaceFirst("\\.less$", ".css"), paths, loadedStack,
					charset);
		}
		return content.replaceAll("\\.css", ".less");
	}
}
