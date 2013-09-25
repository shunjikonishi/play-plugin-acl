package play.modules.flect;

/**
 * 1組の認証情報セットで認証するAuthProvider
 */
public class DefaultAuthProvider implements AuthProvider {
	
	private String realm;
	private String username;
	private String password;
	
	public DefaultAuthProvider(String realm, String username, String password) {
		this.realm = realm;
		this.username = username;
		this.password = password;
	}
	
	public String getRealm() { return this.realm;}
	
	public boolean authenticate(String username, String password) {
		return this.username.equals(username) && this.password.equals(password);
	}
}
