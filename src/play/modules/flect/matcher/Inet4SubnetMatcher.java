package play.modules.flect.matcher;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Inet4SubnetMatcher implements Matcher {
	
	private byte[] addr;
	private byte[] mask;
	
	public Inet4SubnetMatcher(byte[] addr, byte[] mask) {
		this.addr = addr;
		this.mask = mask;
		if (addr.length != 4 || mask.length != 4) {
			throw new IllegalArgumentException();
		}
	}
	
	public boolean match(String s) {
		try {
			InetAddress ip = InetAddress.getByName(s);
			byte[] data = ip.getAddress();
			if (data.length != 4) {
				return false;
			}
			for (int i=0; i<4; i++) {
				if ((data[i] & mask[i]) != addr[i]) {
					return false;
				}
			}
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}
}

