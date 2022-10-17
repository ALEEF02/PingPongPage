package ppp.auth;

import com.okta.idx.sdk.api.client.IDXAuthenticationWrapper;
import com.okta.idx.sdk.api.client.ProceedContext;
import com.okta.idx.sdk.api.model.AuthenticationOptions;
import com.okta.idx.sdk.api.model.UserProfile;
import com.okta.idx.sdk.api.model.VerifyAuthenticatorOptions;
import com.okta.idx.sdk.api.response.AuthenticationResponse;
import com.okta.idx.sdk.api.response.TokenResponse;

import ppp.ServerConfig;

public class OktaAuth {
	
	private static AuthenticationResponse beginResponse;
	private static IDXAuthenticationWrapper authWrapper;
	
	public static void init() {
		authWrapper = new IDXAuthenticationWrapper(ServerConfig.OKTA_ISSUER, ServerConfig.OKTA_CLIENT_ID, ServerConfig.OKTA_CLIENT_SECRET, null, ServerConfig.OKTA_REDIRECT_URI);
		beginResponse = new AuthenticationResponse();
	}
	
}