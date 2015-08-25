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
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import starter.service.LogService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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

    private final String HEADERINFO = "headerInfo";
    private final String RESULTINFO = "resultInfo";
    private final String EX_MSG = "ex_msg";
    private final String EX_STATUSCODE = "ex_statusCode";
    private final String EX_STACKTRACE = "ex_stackTrace";

    private final String EXECUTION = "execution(public * starter.rest.*.*(..)) " +
            "&& !execution(public * starter.rest.ErrorHandler.*(..)) "; //ErrorHandler里处理异常，不记录日志
    //"&& !execution(public * starter.rest.Logs.*(..))"; //Logs里查询日志方法，不记录日志 //todo 测试完毕后放开此注释

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
                threadLocalMap.put(RESULTINFO, result);
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

    public void doBefore(JoinPoint joinpoint) throws IOException {
        Map<String, Object> threadLocalMap = new HashMap<>();

        //从joinpoint获取相关信息
        String className = joinpoint.getSignature().getDeclaringTypeName();
        threadLocalMap.put(CLASS_NAME, className);

        String methodName = joinpoint.getSignature().getName();
        threadLocalMap.put(METHOD_NAME, methodName);

        Object[] args = joinpoint.getArgs();
        if (args != null && args.length > 0) {
            //解析SortBuilder参数信息，还原为字符串类型的参数
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof SortBuilder[]) {
                    SortBuilder[] sorts = (SortBuilder[]) args[i];
                    String sortString = "[";
                    for (SortBuilder sort : sorts) {
                        sortString += sort.toString() + ",";
                    }
                    sortString = sortString.substring(0, sortString.length() - 1) + "]";
                    sortString = sortString.replaceAll("[\\t\\n\\r]", "");//去掉回车换行
                    args[i] = sortString;
                }
            }

            String paramNames = Arrays.toString(args);
            threadLocalMap.put(PARAM_NAMES, paramNames);
        }

        //从request获取相关信息
        HttpServletRequest request = context.getRequest();

        //获取header信息
        Enumeration<String> headerNames = request.getHeaderNames();
        List<String> headerList = new ArrayList<>();
        for (Enumeration e = headerNames; e.hasMoreElements(); ) {
            String thisName = e.nextElement().toString();
            String thisValue = request.getHeader(thisName);
            headerList.add(thisName + ":" + thisValue);
        }
        Object[] headerObjects = headerList.toArray();
        String headerInfo = Arrays.toString(headerObjects);
        threadLocalMap.put(HEADERINFO, headerInfo);

        //只能获取url的信息，不能获取body的内容,故注释
        //Map<String, String[]> parameterMap = request.getParameterMap();
        //Iterator<Map.Entry<String, String[]>> iterator = parameterMap.entrySet().iterator();
        //while (iterator.hasNext()) {
        //    Map.Entry<String, String[]> entry = iterator.next();
        //
        //    System.out.println("KEY:" + entry.getKey());
        //    for (String i : entry.getValue()) {
        //        System.out.println(i);
        //    }
        //}

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

        String headerInfo = (String) getThreadLocal(HEADERINFO);

        Object result = getThreadLocal(RESULTINFO);
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
                    .field("userName", userName)
                    .field("ipAddress", ipAddress)
                    .field("timeInfo.start", startTimeMillis)
                    .field("timeInfo.start_format", startTime_format)
                    .field("timeInfo.end", endTimeMillis)
                    .field("timeInfo.end_format", endTime_format)
                    .field("timeInfo.consume", time_consuming)
                    .field("timeInfo.consume_format", time_consuming_format)
                    .field("actionInfo.url", url)
                    .field("actionInfo.className", className)
                    .field("actionInfo.methodName", methodName)
                    .field("actionInfo.paramNames", paramNames)
                    .field("headerInfo", headerInfo)
                    .field("resultInfo", result_msg)
                    .field("exceptionInfo.msg", ex_msg)
                    .field("exceptionInfo.statusCode", ex_statusCode)
                    .field("exceptionInfo.stackTrace", ex_stackTrace)
                    .field("logDate", new DateTime().toLocalDateTime())
                    .endObject();

            System.out.println("builder is :" + builder.string());
            //XContentBuilder builder_return =
            logService.createLog(builder);
            //System.out.println("builder_return is :" + builder_return.string());

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