package com.nikoyo.odata;

import org.apache.catalina.servlets.DefaultServlet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.ProbabilisticMimeDetectionSelector;
import org.springframework.stereotype.Service;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Service
public class AssetsServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String dir = this.getInitParameter("dir");
        String path = req.getRequestURI().replace(req.getServletPath(), AssetsServlet.class.getClassLoader().getResource(dir).getFile());
        InputStream inputStream = null;
        File file = new File(path);
        if (!file.exists()) {
            resp.setStatus(HttpStatusCode.NOT_FOUND.getStatusCode());
            return;
        }
        TikaConfig config = TikaConfig.getDefaultConfig();
        Detector detector = config.getDetector();
        //TikaInputStream stream = TikaInputStream.get(fileOrStream);

        Metadata metadata = new Metadata();
        metadata.add(Metadata.RESOURCE_NAME_KEY, path);
        MediaType mediaType = detector.detect(null, metadata);

        metadata.add(Metadata.RESOURCE_NAME_KEY, path);
        try {
            resp.setHeader("content-type", mediaType.toString());
            inputStream = new FileInputStream(path);
            IOUtils.copy(inputStream, resp.getOutputStream());
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(resp.getOutputStream());
        }
    }

}
