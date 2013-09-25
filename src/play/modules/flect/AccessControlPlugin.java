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
	private AuthManager auth;
	
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
		//許可IPの設定
		String allow = Play.configuration.getProperty("flect.acl.ipfilter.allow");
		if (allow != null && !"all".equalsIgnoreCase(allow)) {
			this.filter = new IPFilter();
			String[] strs = allow.split(",");
			for (int i=0; i<strs.length; i++) {
				this.filter.addAllowAddress(strs[i]);
			}
		}
		//除外パスの設定
		String excludes = Play.configuration.getProperty("flect.acl.ipfilter.excludes");
		if (this.filter != null && excludes != null && !"none".equals(excludes)) {
			this.excludes = new ArrayList<Matcher>();
			String[] strs = excludes.split(",");
			for (String s : strs) {
				try {
					this.excludes.add(new RegexMatcher(s));
				} catch (PatternSyntaxException e) {
					Logger.error("Cannot parse flect.acl.ipfilter.excludes: " + e.toString());
				}
			}
		}
		
		//Basic認証の設定
		String basicUser = Play.configuration.getProperty("flect.acl.basicAuth.username");
		String basicPass = Play.configuration.getProperty("flect.acl.basicAuth.password");
		if (basicUser != null && basicPass != null) {
			this.auth = new AuthManager(new DefaultAuthProvider("", basicUser, basicPass));
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
		if (this.auth == null || this.auth.authenticate(request)) {
			return false;
		}
		response.status = 401;
		response.setHeader(AuthManager.WWW_AUTHENTICATE, "Basic realm=\"\"");
		return true;
	}
}
