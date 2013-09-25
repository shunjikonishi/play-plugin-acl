package play.modules.flect;

/**
 * 認証のための情報のProvider
 */
public interface AuthProvider {
	
	/**
	 * Realmを返します。
	 */
	public String getRealm();
	
	/**
	 * 引数のユーザー名とパスワードで認証します。
	 * @param username ユーザー名
	 * @param password パスワード
	 * @return 認証に成功した場合true
	 */
	public boolean authenticate(String username, String password);
}
