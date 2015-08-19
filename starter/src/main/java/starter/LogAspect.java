package starter;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
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

    private final String USER_NAME = "username";
    private final String URL = "url";
    private final String IP_ADDRESS = "ipAddress";

    private final String CLASS_NAME = "className";
    private final String METHOD_NAME = "methodName";
    private final String PARAM_NAMES = "paramNames";

    private final String START_TIME_MILLIS = "startTimeMillis";
    private final String RESULT = "result";
    private final String ERROR = "error";

    private final String EXECUTION = "execution(public * starter.rest..*.*(..))";
    private final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private final String DECIMAL_FORMAT = ",###.###";

    private final String MSG_NONE = "[无]";

    @Autowired
    private RequestContext context;

    @Pointcut(EXECUTION)
    public void recordLog() {
    }

    @Before("recordLog()")
    public void before(JoinPoint joinpoint) {
        System.out.println("@Before start..");

        Map<String, Object> threadLocalMap = new HashMap<>();

        String className = joinpoint.getSignature().getDeclaringTypeName();
        threadLocalMap.put(CLASS_NAME, className);

        String methodName = joinpoint.getSignature().getName();
        threadLocalMap.put(METHOD_NAME, methodName);

        Object[] args = joinpoint.getArgs();
        String paramNames = MSG_NONE;
        if (args != null && args.length > 0) {
            paramNames = Arrays.toString(args);
            threadLocalMap.put(PARAM_NAMES, paramNames);
        }

        HttpServletRequest request = context.getRequest();

        String username = request.getUserPrincipal().getName();
        threadLocalMap.put(USER_NAME, username);

        String url = request.getRequestURI();
        threadLocalMap.put(URL, url);

        String ipAddress = getIpAddress(request);
        threadLocalMap.put(IP_ADDRESS, ipAddress);

        long startTimeMillis = System.currentTimeMillis();
        threadLocalMap.put(START_TIME_MILLIS, startTimeMillis);

        setThreadLocal(threadLocalMap);

        String startTime = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(startTimeMillis);

        System.out.println("@Before end.. 用户：" + username + " IP地址：" + ipAddress + " 访问URL：" + url + " 开始时间：" +
                startTime + " 目标类：" + className + " 方法：" + methodName + " 参数：" + paramNames);
    }


    @After("recordLog()")
    public void after(JoinPoint joinpoint) {
        System.out.println("@After start..");

        long endTimeMillis = System.currentTimeMillis();
        String endTime = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(endTimeMillis);

        long startTimeMillis = (long) getThreadLocal(START_TIME_MILLIS);
        String time_consuming_string = new DecimalFormat(DECIMAL_FORMAT).format((endTimeMillis - startTimeMillis)) +
                "ms";
        String username = (String) getThreadLocal(USER_NAME);
        String url = (String) getThreadLocal(URL);
        String ipAddress = (String) getThreadLocal(IP_ADDRESS);

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

        String error_msg = MSG_NONE;
        if (getThreadLocal().containsKey(ERROR)) {
            error_msg = (String) getThreadLocal(ERROR);
        }

        System.out.println("@After end.. 用户：" + username + " IP地址：" + ipAddress + " 访问URL：" + url + " 结束时间：" + endTime +
                " " + " 总耗时：" + time_consuming_string + " 目标类：" + className + " 方法：" + methodName + " 参数：" +
                paramNames + " 异常信息：" + error_msg + " 返回信息：" + result_msg);
    }

    @Around("recordLog()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("around start..");

        try {
            Object result = pjp.proceed();

            throw new RuntimeException("测试业务逻辑抛出异常的情况。。");

            //if (result != null) {
            //Map<String, Object> threadLocalMap = getThreadLocal();
            //threadLocalMap.put(RESULT, result);
            //}

            //System.out.println("around end..");
            //return result;

        } catch (Throwable ex) {
            System.out.println("error in around..");

            StringBuffer ex_sb = new StringBuffer();
            ex_sb.append(ex + "\r\n");
            for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
                ex_sb.append(stackTraceElement.toString() + "\r\n");
            }
            Map<String, Object> threadLocalMap = getThreadLocal();
            threadLocalMap.put(ERROR, ex_sb.toString());

            throw ex;
        }
    }

    //@AfterThrowing(pointcut = "recordLog()", throwing = "error") //注意执行顺序!
    //public void afterThrowing(JoinPoint joinpoint, Throwable error) {
    //    if (error != null) {
    //        System.out.println("error:" + error);
    //        System.out.println("error.getCause:" + error.getCause());
    //        //System.out.println("error.getMessage:" + error.getMessage());
    //        //System.out.println("error.getLocalizedMessage:" + error.getLocalizedMessage());
    //        //System.out.println("error.getStackTrace:" + error.getStackTrace());
    //        for (StackTraceElement stackTraceElement : error.getStackTrace()) {
    //            System.out.println("error.stackTraceElement:" + stackTraceElement.toString());
    //        }
    //    }
    //}


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

//Map<String, String[]> parameterMap = request.getParameterMap();
//String param_msg = "";
//if (parameterMap != null && parameterMap.size() != 0) {
//    //" 输入参数：";
//}