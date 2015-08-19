package starter;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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

    @Around("recordLog()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try {
            doBefore(pjp);

            Object result = pjp.proceed();

            //throw new RuntimeException("测试业务逻辑抛出异常的情况。。。");

            //记录返回信息
            if (result != null) {
                Map<String, Object> threadLocalMap = getThreadLocal();
                threadLocalMap.put(RESULT, result);
            }

            //返回结果给业务逻辑处理
            return result;

        } catch (Throwable ex) {
            //记录异常信息
            StringBuffer ex_sb = new StringBuffer();
            ex_sb.append(ex + "\r\n");
            if (ex instanceof uContentException) {//获取异常信息中http状态码
                ex_sb.append("状态码：" + ((uContentException) ex).getStatusCode() + "\r\n");
            }

            for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
                ex_sb.append(stackTraceElement.toString() + "\r\n");
            }
            Map<String, Object> threadLocalMap = getThreadLocal();
            threadLocalMap.put(ERROR, ex_sb.toString());

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
        String paramNames = MSG_NONE;
        if (args != null && args.length > 0) {
            paramNames = Arrays.toString(args);
            threadLocalMap.put(PARAM_NAMES, paramNames);
        }

        //从request获取相关信息
        HttpServletRequest request = context.getRequest();

        String username = request.getUserPrincipal().getName();
        threadLocalMap.put(USER_NAME, username);

        String url = request.getRequestURI();
        threadLocalMap.put(URL, url);

        String ipAddress = LogAspect.getIpAddress(request);
        threadLocalMap.put(IP_ADDRESS, ipAddress);

        //记录开始时间
        long startTimeMillis = System.currentTimeMillis();
        threadLocalMap.put(START_TIME_MILLIS, startTimeMillis);

        //存入本地线程变量
        setThreadLocal(threadLocalMap);

        //展示结果
        String startTime = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(startTimeMillis);//格式化,为了展示使用
        System.out.println("doBefore end.. \n用户：" + username + "\nIP地址：" + ipAddress + "\n访问URL：" + url + "\n开始时间：" +
                startTime + "\n目标类：" + className + "\n方法：" + methodName + "\n参数：" + paramNames);
    }


    @After("recordLog()")
    public void doAfter() {
        //记录结束时间
        long endTimeMillis = System.currentTimeMillis();
        String endTime = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(endTimeMillis);//格式化,为了展示使用

        //计算耗时
        long startTimeMillis = (long) getThreadLocal(START_TIME_MILLIS);
        String time_consuming_string = new DecimalFormat(DECIMAL_FORMAT).format((endTimeMillis - startTimeMillis)) +
                "ms";//格式化,为了展示使用

        //从本地线程变量获取信息
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

        //展示结果
        System.out.println("doAfter end.. \n用户：" + username + "\nIP地址：" + ipAddress + "\n访问URL：" + url + "\n结束时间：" +
                endTime + "\n耗时：" + time_consuming_string + "\n目标类：" + className + "\n方法：" + methodName + "\n参数：" +
                paramNames + "\n异常信息：" + error_msg + "\n返回信息：" + result_msg);
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