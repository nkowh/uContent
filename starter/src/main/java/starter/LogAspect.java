package starter;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import starter.service.LogService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LogAspect {
    private ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal();

    public Map<String, Object> getThreadLocal() {
        return this.threadLocal.get();
    }

    public void setThreadLocal(Map<String, Object> map) {
        this.threadLocal.set(map);
    }

    public Object getThreadLocal(String key) {
        return getThreadLocal().get(key);
    }

    private final String USER_NAME = "userName";
    private final String URL = "url";
    private final String IP_ADDRESS = "ipAddress";

    private final String CLASS_NAME = "className";
    private final String METHOD_NAME = "methodName";
    private final String PARAM_NAMES = "paramNames";

    private final String START_TIME_MILLIS = "startTimeMillis";

    private final String RESULT = "resultInfo";
    private final String EX_MSG = "ex_msg";
    private final String EX_STATUSCODE = "ex_statusCode";
    private final String EX_STACKTRACE = "ex_stackTrace";

    private final String EXECUTION = "execution(public * starter.rest..*.*(..)) " +
            "&& !execution(public * starter.rest.ErrorHandler.*(..)) " +  //ErrorHandler里处理异常，不记录日志
            "&& !execution(public * starter.rest.Logs.*(..))"; //Logs里查询日志方法，不记录日志

    private final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private final String DECIMAL_FORMAT = ",###.###";
    private final String MSG_NONE = "";

    @Autowired
    private RequestContext context;

    @Autowired
    private LogService logService;

    @Pointcut(EXECUTION)
    public void recordLog() {
    }

    @Around("recordLog()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try {
            doBefore(pjp);

            Object result = pjp.proceed();

            //记录返回信息
            if (result != null) {
                Map<String, Object> threadLocalMap = getThreadLocal();
                threadLocalMap.put(RESULT, result);
            }

            //返回结果给业务逻辑处理
            return result;

        } catch (Throwable ex) {
            //获取本地线程变量
            Map<String, Object> threadLocalMap = getThreadLocal();

            //记录异常信息
            threadLocalMap.put(EX_MSG, ex + "");

            //记录异常信息中http状态码
            if (ex instanceof uContentException) {
                threadLocalMap.put(EX_STATUSCODE, ((uContentException) ex).getStatusCode() + "");
            }

            //记录异常栈信息
            StringBuffer ex_sb = new StringBuffer();
            for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
                ex_sb.append(stackTraceElement.toString() + "\r\n");
            }
            threadLocalMap.put(EX_STACKTRACE, ex_sb.toString());

            //存入本地线程变量
            setThreadLocal(threadLocalMap);

            //抛出异常给业务逻辑处理
            throw ex;
        }
    }

    public void doBefore(JoinPoint joinpoint) {
        Map<String, Object> threadLocalMap = new HashMap<>();

        //从joinpoint获取相关信息
        String className = joinpoint.getSignature().getDeclaringTypeName();
        threadLocalMap.put(CLASS_NAME, className);

        String methodName = joinpoint.getSignature().getName();
        threadLocalMap.put(METHOD_NAME, methodName);

        Object[] args = joinpoint.getArgs();
        if (args != null && args.length > 0) {
            String paramNames = Arrays.toString(args);
            threadLocalMap.put(PARAM_NAMES, paramNames);
        }

        //从request获取相关信息
        HttpServletRequest request = context.getRequest();

        String url = request.getRequestURI();
        threadLocalMap.put(URL, url);

        String username = request.getUserPrincipal().getName();
        threadLocalMap.put(USER_NAME, username);

        String ipAddress = LogAspect.getIpAddress(request);
        threadLocalMap.put(IP_ADDRESS, ipAddress);

        //记录开始时间
        long startTimeMillis = System.currentTimeMillis();
        threadLocalMap.put(START_TIME_MILLIS, startTimeMillis);

        //存入本地线程变量
        setThreadLocal(threadLocalMap);
    }


    @After("recordLog()")
    public void doAfter() throws uContentException {
        //记录结束时间
        long endTimeMillis = System.currentTimeMillis();
        String endTime_format = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(endTimeMillis);//格式化,为了展示使用

        //计算耗时
        long startTimeMillis = (long) getThreadLocal(START_TIME_MILLIS);
        String startTime_format = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(startTimeMillis);//格式化,为了展示使用
        long time_consuming = endTimeMillis - startTimeMillis;
        String time_consuming_format = new DecimalFormat(DECIMAL_FORMAT).format(time_consuming) + "ms";//格式化,为了展示使用

        //从本地线程变量获取信息
        String userName = (String) getThreadLocal(USER_NAME);
        String ipAddress = (String) getThreadLocal(IP_ADDRESS);

        String url = (String) getThreadLocal(URL);
        String className = (String) getThreadLocal(CLASS_NAME);
        String methodName = (String) getThreadLocal(METHOD_NAME);
        String paramNames = MSG_NONE;
        if (getThreadLocal().containsKey(PARAM_NAMES)) {
            paramNames = (String) getThreadLocal(PARAM_NAMES);
        }

        Object result = getThreadLocal(RESULT);
        String result_msg = MSG_NONE;
        if (result != null) {
            result_msg = result + "";
        }

        String ex_msg = MSG_NONE;
        if (getThreadLocal().containsKey(EX_MSG)) {
            ex_msg = (String) getThreadLocal(EX_MSG);
        }
        String ex_statusCode = MSG_NONE;
        if (getThreadLocal().containsKey(EX_STATUSCODE)) {
            ex_statusCode = (String) getThreadLocal(EX_STATUSCODE);
        }
        String ex_stackTrace = MSG_NONE;
        if (getThreadLocal().containsKey(EX_STACKTRACE)) {
            ex_stackTrace = (String) getThreadLocal(EX_STACKTRACE);
        }

        //构建JSON格式对象
        XContentBuilder builder;
        try {
            builder = XContentFactory.jsonBuilder();
            builder.startObject()
                    .field(USER_NAME, userName)
                    .field(IP_ADDRESS, ipAddress)
                    .startObject("timeInfo")
                    .field("start", startTimeMillis)
                    .field("start_format", startTime_format)
                    .field("end", endTimeMillis)
                    .field("end_format", endTime_format)
                    .field("consume", time_consuming)
                    .field("consume_format", time_consuming_format)
                    .endObject()
                    .startObject("actionInfo")
                    .field(URL, url)
                    .field(CLASS_NAME, className)
                    .field(METHOD_NAME, methodName)
                    .field(PARAM_NAMES, paramNames)
                    .endObject()
                    .field(RESULT, result_msg)
                    .startObject("exceptionInfo")
                    .field(EX_MSG, ex_msg)
                    .field(EX_STATUSCODE, ex_statusCode)
                    .field(EX_STACKTRACE, ex_stackTrace)
                    .endObject()
                    .field("logDate", new DateTime().toLocalDateTime())
                    .endObject();

            System.out.println("builder is :" + builder.string());

            XContentBuilder builder_return = logService.createLog(builder);

            System.out.println("builder_return is :" + builder_return.string());

        } catch (IOException e) {
            //系统内部是否要记录异常log？
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    //获取远程IP的真实地址
    private static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }


}