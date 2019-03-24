package com.zxin.apigateway.constant;

/**
 * Cookie 的name、value、maxAge，最大过期时间
 */

public interface CookieConstant {

	String TOKEN = "token";

	String OPENID = "openid";

	/**
	 * 过期时间(单位:s)
	 */
	Integer expire = 7200;
}
