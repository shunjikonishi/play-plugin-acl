package play.modules.flect.matcher;

import java.util.regex.Pattern;

public class RegexMatcher implements Matcher {
	
	private Pattern pattern;
	
	public RegexMatcher(String str) {
		this.pattern = Pattern.compile(str);
	}
	
	public boolean match(String s) {
		return this.pattern.matcher(s).matches();
	}
}
