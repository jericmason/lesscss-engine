package com.asual.lesscss.compiler;

import java.util.ArrayList;

import com.asual.lesscss.LessException;

public interface LessCompiler {
	String compile(String input, String location, ArrayList<String> loadedStack, boolean compress) throws LessException;
}
