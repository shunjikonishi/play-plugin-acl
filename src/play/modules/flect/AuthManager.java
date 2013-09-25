package play.modules.flect;

import org.apache.commons.codec.binary.Base64;
import play.mvc.results.Result;
import play.mvc.Http;
import java.io.UnsupportedEncodingException;

/**
 * 認証を実行するクラス
 */
public class AuthManager {
	
	public static final String AUTHORIZATION = "authorization";
	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
	public static final String BASIC =  "Basic";
	public static final String DIGEST = "Digest";
	
	private AuthProvider provider;
	
	public AuthManager(AuthProvider provider) {
		this.provider = provider;
	}
	
	public AuthProvider getProvider() { return provider;}
	
	/**
	 * Basic認証を実行します。
	 * authorizationヘッダがない場合は401を返してBasic認証を要求します。
	 */
	public void basicAuth() {
		Http.Request request = Http.Request.current();
		Http.Response response = Http.Response.current();
		String realm = provider.getRealm();
		if (realm == null) {
			realm = "";
		}
		if (!authenticate(request)) {
			throw new Unauthorized(BASIC, realm);
		}
	}
	
	public boolean authenticate(Http.Request request) {
		Http.Header authHeader = request.headers.get(AUTHORIZATION);
		if (authHeader == null) {
			return false;
		}
		String scheme = authHeader.value().substring(0, authHeader.value().indexOf(" "));
		if (!BASIC.equalsIgnoreCase(scheme)) {
			return false;
		}
		String cred = authHeader.value().substring(scheme.length()).trim();
		try {
			String decoded = new String(Base64.decodeBase64(cred.getBytes("utf-8")), "utf-8");
			String[] userAndPass = decoded.split(":");
			if (userAndPass == null || userAndPass.length != 2) {
				return false;
			}
			if (!provider.authenticate(userAndPass[0], userAndPass[1])) {
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		return true;
	}
	
	private static class Unauthorized extends Result {
		
		private String authType;
		private String realm;
		
		public Unauthorized(String authType, String realm) {
			super(realm);
			this.authType = authType;
			this.realm = realm;
		}
		
		public void apply(Http.Request request, Http.Response response) {
			response.status = Http.StatusCode.UNAUTHORIZED;
			response.setHeader(WWW_AUTHENTICATE, this.authType + " realm=\"" + this.realm + "\"");
		}
	}
	
}