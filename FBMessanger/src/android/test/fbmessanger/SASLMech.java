package android.test.fbmessanger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.apache.harmony.javax.security.sasl.SaslClient;
import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.util.Base64;

import de.measite.smack.Sasl;

public class SASLMech extends SASLMechanism{

	public static final String PLATFORM = "X-FACEBOOK-PLATFORM";
	private String API_KEY = "";
	private String ACCESS_TOKEN = "";
	private SaslClient mSASLClient;
	
	public SASLMech(SASLAuthentication auth) {
		super(auth);
	}

	@Override
	protected String getName() {
		return PLATFORM;
	}
	
	@Override
	public void authenticate() {
		getSASLAuthentication().send(new AuthMechanism(PLATFORM, ""));
	}
	
	@Override
	public void authenticate(String apiKey, String host, String accessToken) throws SaslException {
		if (apiKey == null || accessToken == null) {
			throw new IllegalArgumentException("SASL auth: invalid params");
		}
		API_KEY = apiKey;
		ACCESS_TOKEN = accessToken;
		Map<String, String> prop = new HashMap<String, String>();
		mSASLClient = Sasl.createSaslClient(new String[] {PLATFORM}, null, "xmpp", host, prop, this);
		authenticate();
	}
	
	@Override
	public void authenticate(String username, String host, CallbackHandler callbackHandler) throws SaslException {
		Map<String, String> prop = new HashMap<String, String>();
		mSASLClient = Sasl.createSaslClient(new String[] {PLATFORM}, null, "xmpp", host, prop, callbackHandler);
		authenticate();
	}

	@Override
	public void challengeReceived(String challenge) throws UnsupportedEncodingException{
		byte[] response = null;
		if (challenge != null) {
			String decodeChallenge = new String(Base64.decode(challenge));
			Map<String, String> params = getQueryMap(decodeChallenge);
			
			String version = "1.0";
			String nonce = params.get("nonce");
			String method = params.get("method");
			long callId = new GregorianCalendar().getTimeInMillis();
			
			String composedResponse = "api_key="  + URLEncoder.encode(API_KEY, "utf-8") + 
									  "&call_id=" + callId + 
									  "&method="  + URLEncoder.encode(method, "utf-8")  +
									  "&nonce="   + URLEncoder.encode(nonce, "utf-8")   +
									  "&access_token=" + 
									  URLEncoder.encode(ACCESS_TOKEN, "utf-8") + "&v="  + 
									  URLEncoder.encode(version, "utf-8");
			
			response = composedResponse.getBytes("utf-8");
		}
		String authText = "";
		if (response != null) {
			authText = Base64.encodeBytes(response, Base64.DONT_BREAK_LINES);
		}
		getSASLAuthentication().send(new Response(authText));
		

	}
	
	private Map<String, String> getQueryMap(String query) {
		Map<String, String> map = new HashMap<String, String>();
		String[] params = query.split("\\&");
		
		for(String param : params) {
			String[] fields = param.split("=", 2);
			map.put(fields[0], (fields.length > 1 ? fields[1]:null));
		}
		return map;
	}
}
