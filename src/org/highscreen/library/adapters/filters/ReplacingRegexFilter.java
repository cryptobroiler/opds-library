package org.highscreen.library.adapters.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacingRegexFilter implements StringFilter {
	private Pattern pattern;
	private String replaceWith;
	
	public ReplacingRegexFilter(String regex, String replace) {
		pattern = Pattern.compile(regex);
		replaceWith = replace;
	}
	@Override
	public String process(String input) {
		Matcher matcher = pattern.matcher(input);
		return matcher.replaceAll(replaceWith);
	}

}
