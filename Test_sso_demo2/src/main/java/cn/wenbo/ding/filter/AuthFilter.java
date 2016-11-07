package cn.wenbo.ding.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;

/**
 * Servlet Filter implementation class AuthFilter
 */
public class AuthFilter implements Filter {


	private static String authURL = StringUtils.EMPTY;
	private static String COOKIE_KEY = "authValue";
	
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		String url = req.getRequestURL().toString();

		String cookieValue = req.getParameter(COOKIE_KEY);
		if (StringUtils.isNotEmpty(cookieValue)) {
			Cookie cookie = new Cookie(COOKIE_KEY, cookieValue);
			resp.addCookie(cookie);
		} else {
			cookieValue = getCookieValue(req);
		}
		
		if (StringUtils.isEmpty(cookieValue)) {
			resp.sendRedirect(authURL + "?redirectURL=" + url);
		} else {
			GetMethod method = null;
			try {
				HttpClient client = new HttpClient();
				if (url.contains("logout")) {
					method = new GetMethod(authURL + "?action=logout&authCookie=" + cookieValue);
				} else {
					method = new GetMethod(authURL + "?action=authCookie&authCookie=" + cookieValue);
				}
				client.executeMethod(method);
				String result = method.getResponseBodyAsString();
				if (StringUtils.equals("success", result)) {
					chain.doFilter(request, response);
				} else {
					resp.sendRedirect(authURL + "?redirectURL=" + url);
				}
			} catch (Exception e) {
				resp.sendRedirect(authURL + "?redirectURL=" + url);
				e.printStackTrace();
			} finally {
				if (method != null) {
					method.releaseConnection();
				}
			}
		}
	}

	private String getCookieValue(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		} else {
			for (Cookie cookie : cookies) {
				if (StringUtils.equals(cookie.getName(), COOKIE_KEY)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		authURL = fConfig.getInitParameter("authURL");
	}

	@Override
	public void destroy() {
		
	}

}
