package play.modules.flect;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import play.Play;
import play.PlayPlugin;
import play.Logger;
import play.mvc.Http.Request;
import play.mvc.Http.Response;

import play.modules.flect.matcher.Matcher;
import play.modules.flect.matcher.RegexMatcher;

public class AccessControlPlugin extends PlayPlugin {
	
	private IPFilter filter;
	private List<Matcher> excludes;
	private List<Matcher> baExcludes;
	private AuthManager auth;
	
	private static String getConfig(String key) {
		String ret = Play.configuration.getProperty(key);
		if (ret != null && ret.startsWith("${") && ret.endsWith("}")) {
			ret = null;
		}
		return ret;
	}
	
	public boolean isAllowUnknown() { 
		return this.filter != null && this.filter.isAllowUnknown();
	}
	public void setAllowUnknown(boolean b) { 
		if (this.filter != null) {
			this.filter.setAllowUnknown(b);
		}
	}
	
	@Override
	public void onApplicationStart() {
		//Settings for IP filterling
		String allow = getConfig("flect.acl.ipfilter.allow");
		if (allow != null && !"all".equalsIgnoreCase(allow)) {
			this.filter = new IPFilter();
			String[] strs = allow.split(",");
			for (int i=0; i<strs.length; i++) {
				this.filter.addAllowAddress(strs[i]);
			}
		}
		//Exclude path for IP filtering
		String excludes = getConfig("flect.acl.ipfilter.excludes");
		if (this.filter != null && excludes != null && !"none".equals(excludes)) {
			this.excludes = new ArrayList<Matcher>();
			buildExcludes(this.excludes, excludes);
		}
		
		//Setting for Basic Authentication
		String basicUser = getConfig("flect.acl.basicAuth.username");
		String basicPass = getConfig("flect.acl.basicAuth.password");
		if (basicUser != null && basicPass != null) {
			this.auth = new AuthManager(new DefaultAuthProvider("", basicUser, basicPass));
		}
		//Exclude path for Basic Authentication
		String baExcludes = getConfig("flect.acl.basicAuth.excludes");
		if (this.auth != null && baExcludes != null && !"none".equals(baExcludes)) {
			this.baExcludes = new ArrayList<Matcher>();
			buildExcludes(this.baExcludes, baExcludes);
		}
	}
	
	private void buildExcludes(List<Matcher> list, String str) {
		String[] strs = str.split(",");
		for (String s : strs) {
			try {
				list.add(new RegexMatcher(s));
			} catch (PatternSyntaxException e) {
				Logger.error("Cannot parse flect.acl.ipfilter.excludes: " + e.toString());
			}
		}
	}
	
	@Override
    public boolean rawInvocation(Request request, Response response) throws Exception {
		return 
			checkFilter(request, response) ||
			checkAuth(request, response);
	}
	
	private boolean checkFilter(Request request, Response response) throws IOException {
		if (this.filter == null) {
			return false;
		}
		if (filter.allow(request)) {
			return false;
		}
		if (this.excludes != null) {
			for (Matcher m : this.excludes) {
				if (m.match(request.path)) {
					return false;
				}
			}
		}
		
		response.status = 403;
		response.contentType = "text/plain";
		response.out.write("Forbidden".getBytes("utf-8"));
		return true;
	}
	
	private boolean checkAuth(Request request, Response response) {
		if (this.auth == null) {
			return false;
		}
		if (this.baExcludes != null) {
			for (Matcher m : this.baExcludes) {
				if (m.match(request.path)) {
					return false;
				}
			}
		}
		if (this.auth.authenticate(request)) {
			return false;
		}
		response.status = 401;
		response.setHeader(AuthManager.WWW_AUTHENTICATE, "Basic realm=\"\"");
		return true;
	}
}
