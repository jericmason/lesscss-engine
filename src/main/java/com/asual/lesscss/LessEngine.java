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

package com.asual.lesscss;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;

import com.asual.lesscss.compiler.LessCompiler;
import com.asual.lesscss.compiler.RhinoCompiler;
import com.asual.lesscss.loader.ChainedResourceLoader;
import com.asual.lesscss.loader.ClasspathResourceLoader;
import com.asual.lesscss.loader.CssProcessingResourceLoader;
import com.asual.lesscss.loader.FilesystemResourceLoader;
import com.asual.lesscss.loader.HTTPResourceLoader;
import com.asual.lesscss.loader.JNDIResourceLoader;
import com.asual.lesscss.loader.ResourceLoader;
import com.asual.lesscss.loader.UnixNewlinesResourceLoader;

/**
 * @author Rostislav Hristov
 * @author Uriah Carpenter
 * @author Noah Sloan
 */
public class LessEngine {

	private final Log logger = LogFactory.getLog(getClass());

	private final LessOptions options;
	private final ResourceLoader loader;

	private LessCompiler compiler;

	public LessEngine() {
		this(new LessOptions());
	}

	public LessEngine(LessOptions options) {
		this(options, defaultResourceLoader(options));
	}

	private static ResourceLoader defaultResourceLoader(LessOptions options) {
		ResourceLoader resourceLoader = new ChainedResourceLoader(
				new FilesystemResourceLoader(), new ClasspathResourceLoader(
				LessEngine.class.getClassLoader()),
				new JNDIResourceLoader(), new HTTPResourceLoader());
		if (options.isCss()) {
			return new CssProcessingResourceLoader(resourceLoader);
		}
		resourceLoader = new UnixNewlinesResourceLoader(resourceLoader);
		return resourceLoader;
	}

	public LessEngine(LessOptions options, ResourceLoader loader) {
		this.options = options;
		this.loader = loader;
		try {
			logger.debug("Initializing LESS Engine.");
			ClassLoader classLoader = getClass().getClassLoader();
			URL less = options.getLess();
			URL env = classLoader.getResource("META-INF/env.js");
			URL engine = classLoader.getResource("META-INF/engine.js");
			URL cssmin = classLoader.getResource("META-INF/cssmin.js");
			URL sourceMap = classLoader.getResource("META-INF/source-map.js");
			compiler = new RhinoCompiler(options, loader, less, env, engine, cssmin, sourceMap);
			Context.exit();
		} catch (Exception e) {
			logger.error("LESS Engine intialization failed.", e);
		}
	}

	public String compile(String input) throws LessException {
		return compile(input, null, false);
	}

	public String compile(String input, String location) throws LessException {
		return compile(input, location, false);
	}

	public String compile(String input, String location, boolean compress)
		throws LessException {
		long time = System.currentTimeMillis();
		String result = compiler.compile(input, location == null ? "" : location, new ArrayList<String>(), compress);
		logger.debug("The compilation of '" + input + "' took "
				+ (System.currentTimeMillis() - time) + " ms.");
		return result;
	}

	public String compile(URL input) throws LessException, IOException {
		return compile(input, false);
	}

	public String compile(URL input, boolean compress) throws LessException, IOException {
		long time = System.currentTimeMillis();
		String location = input.toString();
		logger.debug("Compiling URL: " + location);
		ArrayList<String> resourceStack = new ArrayList<String>();
		String source = loader.load(getFile(location), getPaths(location), resourceStack, options.getCharset());
		String result = compiler.compile(source, location, resourceStack, compress);
		logger.debug("The compilation of '" + input + "' took "
				+ (System.currentTimeMillis() - time) + " ms.");
		return result;
	}
	
	public String compile(ResourceLocation location) throws LessException, IOException {
		return compile(location, false);
	}
	
	public String compile(ResourceLocation input, boolean compress) throws LessException, IOException {
		long time = System.currentTimeMillis();
		String location = input.getLocation();
		logger.debug("Compiling URL: " + location);
		ArrayList<String> resourceStack = new ArrayList<String>();
		String source = loader.load(location, getPaths(location), resourceStack, options.getCharset());
		String result = compiler.compile(source, location, resourceStack, compress);
		logger.debug("The compilation of '" + input + "' took "
				+ (System.currentTimeMillis() - time) + " ms.");
		return result;
	}

	public String compile(File input) throws LessException, IOException {
		return compile(input, false);
	}

	public String compile(File input, boolean compress) throws LessException, IOException {
		long time = System.currentTimeMillis();
		String location = input.getAbsolutePath();
		logger.debug("Compiling File: " + "file:" + location);
		String source = null;
		ArrayList<String> resourceStack = new ArrayList<String>();
		source = loader.load(getFile(location), getPaths(location), resourceStack, options.getCharset());
		String result = compiler.compile(source, location, resourceStack, compress);
		logger.debug("The compilation of '" + input + "' took "
				+ (System.currentTimeMillis() - time) + " ms.");
		return result;
	}
	
	public void compile(File input, File output) throws LessException,
			IOException {
		compile(input, output, false);
	}

	public void compile(File input, File output, boolean compress)
			throws LessException, IOException {
		String content = compile(input, compress);
		if (!output.exists()) {
			output.createNewFile();
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		bw.write(content);
		bw.close();
	}

	private String[] getPaths(String currentFile) {
		String[] paths;
		if(options.getPaths() != null) {
			paths = Arrays.copyOf(options.getPaths(), options.getPaths().length + 1);
		} else {
			paths = new String[1];
		}
		String currentPath = currentFile.replaceAll("^(.*[\\/\\\\])[^\\/\\\\]*$", "$1");
		paths[paths.length - 1] = currentPath;
		for(int i = 0; i < paths.length; i++) {
			paths[i] = paths[i].replace('\\', '/');
		}
		return paths;
	}
	
	private String getFile(String currentFile) {
		String file = currentFile.replaceAll("^(.*[\\/\\\\])([^\\/\\\\]*)$", "$2");
		return file;
	}
}