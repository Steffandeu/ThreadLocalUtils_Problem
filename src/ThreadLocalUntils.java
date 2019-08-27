import com.sun.org.apache.xpath.internal.operations.String;
import javafx.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ThreadLocalUntils {
    private static ThreadLocalUntils instance = new ThreadLocalUntils();
    private static final java.lang.String SYS_TIME_FMT="HHmmss";
    private static final java.lang.String SYS_DATE_FMT="yyyyMMdd";
    private static final java.lang.String SYS_DATETIME_FMT=SYS_DATE_FMT+SYS_TIME_FMT;

    private static ThreadLocal<String> logKey = new ThreadLocal<String>();

    private ThreadLocalUntils(){
        setInstance(this);
    }

    //业务处理时间(使用数据库服务器的时间)
    private static ThreadLocal<DateTimeVo> sysDateTime = new ThreadLocal<DateTimeVo>(){
        @Override
        protected DateTimeVo initialValue(){
            return null;
        }
    };

    //当前线程的UUID信息，用于打印日志；
    private static ThreadLocal<java.lang.String> currLogUuid = new InheritableThreadLocal<java.lang.String>(){
        @Override
        protected java.lang.String initialValue(){
            return UUID.randomUUID().toString()/*.toUpperCase()*/;
        }
    };

    public static String geyLogKey(){
        return logKey.get();
    }

    public static void setLogKey(String key){
        logKey.set(key);
    }

    public static Pair<java.lang.String,Boolean> getSysDateTime(){
//        Pair<DateTimeVo,Boolean> pair = innerGetSysDateTime();
        Pair<DateTimeVo,Boolean> pair = innerGetSysDateTime();
        Pair<java.lang.String,Boolean> rtn = new Pair<java.lang.String,Boolean>(
                pair.getKey().getDate(),pair.getValue());
        return rtn;
    }

    public static Pair<java.lang.String,Boolean> getSysDate(){
        Pair<DateTimeVo,Boolean> pair = innerGetSysDateTime();
        Pair<java.lang.String,Boolean> rtn = new Pair<java.lang.String,Boolean>(pair.getKey().getDate(),pair.getValue());
        return rtn;
    }

    public static Pair<DateTimeVo,Boolean> innerGetSysDateTime(){
        DateTimeVo dateTimeVo = sysDateTime.get();
        boolean isNew = false;
        if (dateTimeVo==null){
            dateTimeVo = instance.initSysDateTime();
            sysDateTime.set(dateTimeVo);
            isNew = true;
        }
        Pair<DateTimeVo,Boolean> pair = new Pair<DateTimeVo,Boolean>(dateTimeVo,isNew);
        return pair;
    }

    public static void clear(Boolean isNew){
        if (isNew){
            sysDateTime.remove();
            currLogUuid.remove();
//            System.out.println(1);
        }
    }

    public static java.lang.String getCurrLogUuid(){
        return currLogUuid.get();
    }

    public static java.lang.String getLogPrefix(){
        java.lang.String currentMehodName="";
        if (Thread.currentThread().getStackTrace().length>2){
            currentMehodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        }
        return "<uuid="+getCurrLogUuid()+">"+currentMehodName+" ";
    }

    public DateTimeVo initSysDateTime(){
        DateTimeVo vo = new DateTimeVo();
        SimpleDateFormat dateFormat1 = new SimpleDateFormat(SYS_DATETIME_FMT);
        vo.setDateTime(dateFormat1.format(vo.getDateTime()));
        try{
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(SYS_DATETIME_FMT);
            Date date = dateTimeFormat.parse(vo.getDateTime());
            //获取日期
            SimpleDateFormat dateFormat =  new SimpleDateFormat(SYS_DATE_FMT);
            vo.setDateTime(dateFormat.format(date));
            //获取时间
            SimpleDateFormat timeFormat = new SimpleDateFormat(SYS_TIME_FMT);
            vo.setDateTime(timeFormat.format(date));
        }catch (Exception e){
            throw new IllegalArgumentException("系统时间格式不正确："+vo.getDateTime());
        }
        return vo;
    }

    private static void setInstance(ThreadLocalUntils instance){
        ThreadLocalUntils.instance = instance;
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
                count=0;
            }
            return count;
        }
    }

    private static class DateTimeVo extends CountedVo{
        private java.lang.String dateTime = null;
        private java.lang.String date = null;
        private java.lang.String time = null;

        public java.lang.String getDateTime(){
            return dateTime;
        }

        public java.lang.String getTime(){
            return time;
        }

        public java.lang.String getDate(){
            return date;
        }

        public void setDateTime(java.lang.String dateTime){
            this.dateTime = dateTime;
        }

        public void setDate(java.lang.String date){
            this.date  = date;
        }

        public void setTime(java.lang.String time){
            this.time = time;
        }
    }



}
