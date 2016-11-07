package cn.wenbo.ding.servlet;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * Servlet implementation class AuthServlet
 */
public class AuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
      
	private static ConcurrentSkipListSet<User> users = new ConcurrentSkipListSet<>();
	private static ConcurrentSkipListSet<String> uuIDs = new ConcurrentSkipListSet<>();
	
	private static String COOKIE_KEY = "authValue";
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		users.clear();
		User user = new User("ding", "123456");
		users.add(user);
		user = new User("wen", "123456");
		users.add(user);
		
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String redirectURL = request.getParameter("redirectURL");
		String action = request.getParameter("action");
		String authCookie = request.getParameter("authCookie");
		
		if (StringUtils.isEmpty(action)) {
			if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
				User user = new User(username, password);
				if (users.contains(user)) {
					String uuId = String.valueOf(System.currentTimeMillis());
					Cookie cookie = new Cookie("authValue", uuId);
//					cookie.setPath("/");
					response.addCookie(cookie);
					uuIDs.add(uuId);
					response.sendRedirect(redirectURL + "?authValue=" + uuId);
					return;
				}
				
			} else {
				String cookie = getCookieValue(request);
				if (StringUtils.isNotEmpty(cookie) && uuIDs.contains(cookie)) {
					response.sendRedirect(redirectURL + "?authValue=" + cookie);
					return;
				}
			}
			response.sendRedirect("login.jsp?redirectURL=" + redirectURL);
		} else if (StringUtils.equals("authCookie", action)) {
			if (uuIDs.contains(authCookie)) {
				response.getWriter().write("success");
			} else {
				response.getWriter().write("fail");
			}
		} else if (StringUtils.equals("logout", action)) {
			uuIDs.remove(authCookie);
			response.getWriter().write("success");
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
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
}

class User implements Comparable<User> {
	private String username;
	private String password;
	
	
	public User() {
		super();
	}
	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public int hashCode() {
		return StringUtils.defaultString(username).hashCode() ^ StringUtils.defaultString(password).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		User temp = (User)obj;
		return StringUtils.equals(username, temp.getUsername()) && StringUtils.equals(password, temp.getPassword());
	}
	@Override
	public int compareTo(User o) {
		if (o == null) {
			return 0;
		}
		if (StringUtils.defaultString(username).compareTo(StringUtils.defaultString(o.getUsername())) == 0) {
			return StringUtils.defaultString(password).compareTo(StringUtils.defaultString(o.getPassword()));
		} else {
			return StringUtils.defaultString(username).compareTo(StringUtils.defaultString(o.getUsername()));
		}
	}
}