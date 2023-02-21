package com.youlai.auth.oauth2.customizer.handler;

import com.youlai.auth.oauth2.customizer.JwtCustomizerHandler;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;


public abstract class AbstractJwtCustomizerHandler implements JwtCustomizerHandler {

	protected JwtCustomizerHandler jwtCustomizerHandler;
	
	public AbstractJwtCustomizerHandler(JwtCustomizerHandler jwtCustomizerHandler) {
		this.jwtCustomizerHandler = jwtCustomizerHandler;
	}

	protected abstract boolean supportCustomizeContext(Authentication authentication);
	protected abstract void customizeJwt(JwtEncodingContext jwtEncodingContext);
	
	@Override
	public void customize(JwtEncodingContext jwtEncodingContext) {
		
		boolean supportCustomizeContext = false;
		AbstractAuthenticationToken token = null;
    	
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	
    	if (authentication instanceof OAuth2ClientAuthenticationToken ) {
    		token = (OAuth2ClientAuthenticationToken) authentication;
    	} 
    	
    	if (token != null) {
    		if (token.isAuthenticated() && OAuth2TokenType.ACCESS_TOKEN.equals(jwtEncodingContext.getTokenType())) {
    			Authentication principal = jwtEncodingContext.getPrincipal();
    			supportCustomizeContext = supportCustomizeContext(principal);
    		}
    	}
		
    	if (supportCustomizeContext) {
    		customizeJwt(jwtEncodingContext);
    	} else {
    		jwtCustomizerHandler.customize(jwtEncodingContext);
    	}

	}

}
