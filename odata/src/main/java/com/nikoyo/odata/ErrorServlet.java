package com.nikoyo.odata;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.apache.olingo.server.api.processor.ReferenceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class ErrorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ErrorServlet.class);

    @Autowired
    RequestContext context;

    @Autowired
    private DefaultEntityProcessor entityProcessor;

    @Autowired
    private DefaultEntityCollectionProcessor entityCollectionProcessor;

    @Autowired
    private ReferenceProcessor referenceProcessor;

    @Autowired
    private DefaultProcessor defaultProcessor;

    @Autowired
    private DefaultPrimitiveProcessor primitiveProcessor;



    public ErrorServlet() {
        super();
    }


    @Override
    public void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.getOutputStream().print("error");
        resp.getOutputStream().close();
    }


}