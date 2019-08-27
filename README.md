# ThreadLocalUtils_Problem
Potential highlight....

ThreadLocalUtils的主要作用是，在同一个jvm中，同一线程要访问的变量可以放在这个，不需要用方法参数传递变量的值。
icif目前使用了这种方式，让同一个线程方法调用链上的时间保持一致。

在使用该工具类获取时间，出现获取的时间不是当天时间的情况：由于使用了ThreadLocal缓存时间
解决方案：
try...catch...finally将ThreadLocal缓存晴空

出错的具体示例如下：

public void a(){
try{
   ...业务代码
   b();
   ...业务代码
  }
}

public void b(){
  boolean isNew = false;
  try{
      //获取统一的系统时间
      isNew = ThreadLocalUtils.getSysDateTime().getValue();
      ...业务代码
      c();
      ...业务代码
  }catch(Exception e){
  }finally{
    ThreadLocalUtils.clear(isNew);
  }
}

private void c(){
    String t1 = ThreadLocalUtils.getSysDate().getValue();
    String t2 = ThreadLocalUtils.getSysTime().getValue();
}

正确的使用方法是在a的方法上也要加上try...catch..finally块将ThreadLocal缓存清空（c中调用了ThreadLocalUtils工具类，如果c方法中没有使用，而c方法嵌套调用了d方法而d方法中使用了工具类，若a中没有使用try...catch...finally块将ThreadLocal缓存清空,可能会导致将来所有使用的ThreadLocalUtils工具类获取时间的地方都有可能拿到的是历史缓存的时间）

正确的示例：
public void a(){
   boolean isNew = false;
   try{
      isNew = ThreadLocalUtils.getSysDateTime().getValue();
      c();
   }catch(Exception e){
   }finally{
      ThreadLocalUtils.clear(isNew);
   }
}

private void c(){
    String t1 = ThreadLocalUtils.getSysDate().getValue();
    String t2 = ThreadLocalUtils.getSysTime().getValue();
}

Plan to solve this problem:
A:在所有使用的ThreadLocalUtils方法中必须使用try...catch...finally块将ThreadLocal混存清空
B：在使用ThreadLocalUtils的方法的地方，看业务上是否需要上下文线程方法，使用时间必须是同一时间的要求，如果有可以最外层获取时间，然后通过参数传入到要调用的方法中，如果没有必要的时间一致要求，那么建议不使用此共苦累，直接获取系统时间即可。如果业务上要求获取数据库时间，那么通过dual这个傀儡表获取的时间也非常的快。
