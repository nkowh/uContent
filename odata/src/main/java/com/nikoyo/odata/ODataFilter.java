package com.nikoyo.odata;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ODataFilter implements Filter {


	@Autowired
	RequestContext context;

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		context.setRepositoryId(getRepositoryId(request));
		context.setHttpServletRequest(request);

		HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,PATCH,MERGE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
		chain.doFilter(req, res);
	}
	private String getRepositoryId(final HttpServletRequest req) {
		return req.getRequestURI().split("/")[1];
	}
	public void init(FilterConfig filterConfig) {}

	public void destroy() {}




}