import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadLocalUntilsRe {
    private static ThreadLocalUntilsRe instance = null;

    private static final String SYS_TIME_FMT="HHmmss";
    private static final String SYS_DATE_FMT="yyyyMMdd";
    private static final String SYS_DATETIME_FMT=SYS_DATE_FMT+SYS_TIME_FMT;

    @Autowired
    private WorkDayManager workDayManager = null;

    private ThreadLocalUntilsRe(){
        setInstance(this);
    }

    //初始化标志
    private static final Object noop = new Object();
    private static ThreadLocal<Object> flag = new InheritableThreadLocal<Object>(){
        @Override
        protected Object initialValue(){
            return null;
        }
    };

    private static ThreadLocal<DateTimeVo> sysDateTime = new ThreadLocal<DateTimeVo>(){
        @Override
        protected DateTimeVo initialValue() {
            return null;
        }
    };

    //当前线程的UUID的信息，主要用于打印日志；
    private static ThreadLocal<String> currLogUuid = new InheritableThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return null;
        }
    };

    //调用方传入的apDate参数
    private static ThreadLocal<String> apDate = new InheritableThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return null;
        }
    };
    //调用方传入的apTime参数
    private static ThreadLocal<String> apTime = new InheritableThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return null;
        }
    };


    public static String getSysDateTime(){
        if (!isInitialized()){
            throw new IllegalStateException("TLS未初始化");
        }
        DateTimeVo pair = innerGetSysDateTime();

        return pair.getDate();
    }

    public static String getSysDate(){
        if (!isInitialized()){
            throw new IllegalStateException("TLS未初始化");
        }
        DateTimeVo pair = instance.initSysDateTime();
        return pair.getDate();
    }

    public static DateTimeVo innerGetSysDateTime(){
        DateTimeVo dateTimeVo = sysDateTime.get();
        if (dateTimeVo==null){
            instance.initSysDateTime();
            sysDateTime.set(dateTimeVo);
        }
        return dateTimeVo;
    }

    public static String getSysTime(){
        if (!isInitialized()){
            throw new IllegalStateException("TLS未初始化");
        }
        DateTimeVo pair = innerGetSysDateTime();
        return pair.getTime();
    }

    public static void clear(Boolean isNew){
        if (isNew){
            sysDateTime.remove();
            currLogUuid.remove();
            apDate.remove();
            apTime.remove();
            flag.remove();;
        }
    }

    public static String getCurrLogUuid(){
        if (!isInitialized()){
            throw new IllegalStateException("TLS未初始化");
        }
        return currLogUuid.get();
    }

    public static String getLogPrefix(){
        if (!isInitialized()){
//            return StringUtils.EMPTY;
            return null;
        }
        return "<uuid =" + getCurrLogUuid() + ">";
    }

    public static void setApDate(String apDate){
        ThreadLocalUntilsRe.apDate.set(apDate);
    }

    public static String getApDate(){
        return apDate.get();
    }

    public static void setApTime(String apTime){
        ThreadLocalUntilsRe.apTime.set(apTime);
    }

    private static boolean isInitialized(){
        return flag.get()!=null;
    }

    /**
     * 初始化上下文，如果已经初始化则返回false，否则返回true
     * @return
     */
    public static boolean initialize(){
        if (isInitialized()){
            return false;
        }
        flag.set(noop);
        return true;
    }

    public DateTimeVo initSysDateTime(){
        DateTimeVo vo = new DateTimeVo();
        WorkDayManager manager = instance.getWorkDayManager();
        vo.setDateTime(manager.getSystemDateTime());
        try{
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(SYS_DATETIME_FMT);
            Date date = dateTimeFormat.parse(vo.getDateTime());

            //获取日期
            SimpleDateFormat dateFormat = new SimpleDateFormat(SYS_DATE_FMT);
            vo.setDate(dateFormat.format(date));

            //获取时间
            SimpleDateFormat timeFormat = new SimpleDateFormat(SYS_TIME_FMT);
            vo.setTime(timeFormat.format(date));
        }catch (Exception e){
            throw new IllegalArgumentException("系统时间的格式不正确:"+vo.getDateTime());
        }
        return vo;
    }


    public static void setInstance(ThreadLocalUntilsRe instance){
        ThreadLocalUntilsRe.instance = instance;
    }

    public WorkDayManager getWorkDayManager() {
        return workDayManager;
    }

    public void setWorkDayManager(WorkDayManager workDayManager) {
        this.workDayManager = workDayManager;
    }

    private static abstract class CountedVo{
        private int count = (0);
        public int getValue(){
            return count;
        }

        public int inc(){
            ++count;
            return count;
        }

        public int dec(){
            --count;
            if (count<0){
                count = 0;
            }
            return count;
        }
    }

    private static class DateTimeVo extends CountedVo{
        private String dateTime =null;
        private String date = null;
        private String time = null;
        public String getDateTime(){
            return dateTime;
        }

        public void setDateTime(String dateTime){
            this.dateTime = dateTime;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date){
            this.date = date;
        }
        public String getTime(){
            return time;
        }
        public void setTime(String time){
            this.time = time;
        }
    }
}
