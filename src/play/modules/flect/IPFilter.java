package play.modules.flect;

import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;

import play.mvc.Http;
import play.modules.flect.matcher.Matcher;
import play.modules.flect.matcher.SimpleMatcher;
import play.modules.flect.matcher.Inet4SubnetMatcher;

/**
 * IPアドレスを許可するかどうかを判定するクラスです。
 */
public class IPFilter {
	
	public static IPFilter getInstance(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		IPFilter filter = new IPFilter();
		String[] strs = str.split(",");
		for (int i=0; i<strs.length; i++) {
			filter.addAllowAddress(strs[i]);
		}
		return filter;
	}
	
	private List<Matcher> matchers = new ArrayList<Matcher>();
	private boolean allowUnknown = false;
	
	public boolean isAllowUnknown() { return this.allowUnknown;}
	public void setAllowUnknown(boolean b) { this.allowUnknown = b;}
	
	/**
	 * 許可するIPアドレスを追加します。
	 * 引数のIPアドレスにはサブネット表記が使用できます。
	 * Ex). 127.0.0.1
	 *      24.24.24.0/24
	 *      24.24.24.0/255.255.255.0
	 */
	public void addAllowAddress(String ip) {
		Matcher m = null;
		int idx = ip.indexOf("/");
		if (idx == -1) {
			m = new SimpleMatcher(ip);
		} else {
			String addr = ip.substring(0, idx);
			String mask = ip.substring(idx+1);
			if (mask.indexOf(".") == -1) {
				mask = getAddressStr(mask);
			}
			try {
				m = new Inet4SubnetMatcher(
					InetAddress.getByName(addr).getAddress(), 
					InetAddress.getByName(mask).getAddress()
				);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException(ip);
			}
		}
		this.matchers.add(m);
	}
	
	/**
	 * 引数のRequestのRemoteAddress(「x-forwarded-for」ヘッダがある場合はそちら)が許可されるかどうかを返します。
	 */
	public boolean allow(Http.Request request) {
		String ip = request.remoteAddress;
		Http.Header h = request.headers.get("x-forwarded-for");
		if (h != null && h.value().length() > 0) {
			ip = h.value() + "," + ip;
		}
		return allow(ip);
	}
	
	/**
	 * 引数のIPアドレスが許可されるかどうかを返します。
	 */
	public boolean allow(String ip) {
		String[] tests = ip.split(",");
		for (String s : tests) {
			s = s.trim();
			if ("unknown".equals(s)) {
				if (this.allowUnknown) {
					continue;
				} else {
					return false;
				}
			}
			for (Matcher m : this.matchers) {
				if (m.match(s)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static String getAddressStr(String mask) {
		try {
			int n = Integer.parseInt(mask);
			
			StringBuilder buf = new StringBuilder();
			for (int i=0; i<4; i++) {
				if (i != 0) {
					buf.append(".");
				}
				if (n >= 8) {
					buf.append("255");
					n -= 8;
					continue;
				}
				switch (n) {
					case 0: buf.append("0"); break;
					case 1: buf.append("128"); break;
					case 2: buf.append("192"); break;
					case 3: buf.append("224"); break;
					case 4: buf.append("240"); break;
					case 5: buf.append("248"); break;
					case 6: buf.append("252"); break;
					case 7: buf.append("254"); break;
				}
				n = 0;
			}
			return buf.toString();
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	
	
}
