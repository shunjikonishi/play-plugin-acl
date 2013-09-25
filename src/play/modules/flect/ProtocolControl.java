package play.modules.flect;

import play.Play;
import play.data.validation.Validation;
import play.mvc.Http.Request;
import play.mvc.Http.Header;
import play.mvc.Scope.Flash;
import play.mvc.results.Redirect;

/**
 * プロトコル制御クラス
 */
public class ProtocolControl {
	
	/**
	 * リクエストのプロトコルがhttpの場合、httpsにリダイレクトします。
	 */
	public static void sslOnly() {
		checkProtocol(true);
	}
	
	/**
	 * リクエストのプロトコルがhttpsの場合、httpにリダイレクトします。
	 */
	public static void httpOnly() {
		checkProtocol(false);
	}
	
	private static void checkProtocol(boolean ssl) {
		if (Play.mode == Play.Mode.DEV) {
			return;
		}
		Request request = Request.current();
		if (request == null) {
			return;
		}
		
		if (isSSL(request) != ssl) {
			StringBuilder buf = new StringBuilder();
			buf.append(ssl ? "https://" : "http://")
				.append(request.domain)
				.append(request.path);
			if (request.querystring != null && request.querystring.length() > 0) {
				buf.append("?").append(request.querystring);
			}
			Flash.current().keep();
			Validation.current().keep();
			throw new Redirect(buf.toString());	
		}
	}
	
	public static boolean isSSL(Request request) {
		Header proto = request.headers.get("x-elb-proto");
		if (proto != null) {
			return "https".equals(proto.value());
		}
		proto = request.headers.get("x-forwarded-proto");
		if (proto != null) {
			return "https".equals(proto.value());
		}
		return request.secure;
	}
}
