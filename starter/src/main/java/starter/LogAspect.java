package starter;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LogAspect {
    protected ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal();

    public Map<String, Object> getThreadLocal() {
        return this.threadLocal.get();
    }

    public void setThreadLocal(Map<String, Object> map) {
        this.threadLocal.set(map);
    }

    public Object getThreadLocal(String key) {
        return getThreadLocal().get(key);
    }

    private final String USERNAME = "username";
    private final String URL = "url";
    private final String START_TIME_MILLIS = "startTimeMillis";
    private final String RESULT = "result";
    private final String ERROR = "error";

    private final String EXECUTION = "execution(public * starter.rest..*.*(..))";
    private final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private final String DECIMAL_FORMAT = ",###.###";

    @Autowired
    private RequestContext context;

    @Pointcut(EXECUTION)
    public void recordLog() {
    }

    @Before("recordLog()")
    public void before(JoinPoint joinpoint) {
        System.out.println("@Before方法执行前");

        HttpServletRequest request = context.getRequest();
        String username = request.getUserPrincipal().getName();
        String url = request.getRequestURI();

        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put(USERNAME, username);
        threadLocalMap.put(URL, url);
        long startTimeMillis = System.currentTimeMillis();
        threadLocalMap.put(START_TIME_MILLIS, startTimeMillis);
        setThreadLocal(threadLocalMap);

        String startTime = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(startTimeMillis);

        System.out.println("用户：" + username + " 访问URL：" + url + " 开始于：" + startTime);
    }


    @After("recordLog()")
    public void after(JoinPoint joinpoint) {
        System.out.println("@After方法执行后");

        long endTimeMillis = System.currentTimeMillis();
        String endTime = new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(endTimeMillis);

        long startTimeMillis = (long) getThreadLocal(START_TIME_MILLIS);
        String time_consuming_string = new DecimalFormat(DECIMAL_FORMAT).format((endTimeMillis - startTimeMillis)) +
                "ms";
        String username = (String) getThreadLocal(USERNAME);
        String url = (String) getThreadLocal(URL);


        Object result = (Object) getThreadLocal(RESULT);
        Throwable e = (Throwable) getThreadLocal(ERROR);

        System.out.println("用户：" + username + " 访问URL：" + url + " 结束于：" + endTime + " 耗时：" + time_consuming_string);
    }

    @Around("recordLog()")
    public void around(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("around start..");

        try {
            Object result = pjp.proceed();

            Map<String, Object> threadLocalMap = getThreadLocal();
            threadLocalMap.put(RESULT, result);

        } catch (Throwable ex) {
            System.out.println("error in around");
            throw ex;
        }

        System.out.println("around end");
    }

    @AfterThrowing(pointcut = "recordLog()", throwing = "error")
    public void afterThrowing(JoinPoint joinpoint, Throwable error) {
        if (error != null) {
            System.out.println("error:" + error);

            Map<String, Object> threadLocalMap = getThreadLocal();
            threadLocalMap.put(ERROR, error);
        }
    }

}
