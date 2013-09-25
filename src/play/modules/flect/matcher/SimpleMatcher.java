package play.modules.flect.matcher;

public class SimpleMatcher implements Matcher {
	
	private String str;
	
	public SimpleMatcher(String str) {
		this.str = str;
	}
	
	public boolean match(String s) {
		return this.str.equals(s);
	}
}
