package com.nikoyo.odata;

import org.apache.olingo.commons.api.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.apache.olingo.server.api.processor.ReferenceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class ODataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ODataServlet.class);

    @Autowired
    private RequestContext context;

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


    public ODataServlet() {
        super();
    }


    @Override
    public void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        OData odata = OData.newInstance();
        context.setHttpServletRequest(req);
        List<EdmxReference> references = new ArrayList<>();
        ServiceMetadata serviceMetadata = odata.createServiceMetadata(context.getProvider(), references);
        ODataHttpHandler handler = odata.createHandler(serviceMetadata);
        context.setServiceMetadata(serviceMetadata);
        handler.setSplit(1);
        handler.register(defaultProcessor);
        handler.register(entityCollectionProcessor);
        handler.register(entityProcessor);
        handler.register(referenceProcessor);
        handler.register(primitiveProcessor);
        handler.process(req, resp);
    }


}