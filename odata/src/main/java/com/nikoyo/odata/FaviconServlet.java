package com.nikoyo.odata;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

@Service
public class FaviconServlet extends HttpServlet {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        URL favicon = applicationContext.getClass().getClassLoader().getResource("favicon.ico");
        IOUtils.copy(favicon.openStream(), resp.getOutputStream());
        resp.setHeader("content-type", "image/x-icon");
        resp.getOutputStream().flush();
        resp.getOutputStream().close();
    }
}
