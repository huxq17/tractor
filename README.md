# tractor
轻量级的任务管理器
###Usage
----

#### Gradle

```groovy
dependencies {
   compile 'com.andbase.tractor:tractor:0.0.4'
}
```

在平时的android开发工作中，我们经常需要执行耗时操作，有时为了用户体验还需要显示个等待框，我之前的做法都是开一个线程，然后用handler发消息进行显示和关闭等待框以及相关的ui操作。如果任务比较多的话,频繁的new Thread会让代码看上去比较混乱，而且还不好管理，针对这种情况我写了tractor。tractor主要的作用有：
 1. 代码变得整洁，不用在到处new Thread和new Handler；
 2. 能够监控任务的执行情况，可以随时取消一个或多个任务；
 3. 支持大文件上传，多线程下载，get,post以及其他的网络请求



## **效果图**
![效果图](http://img.my.csdn.net/uploads/201512/07/1449480498_3231.gif)

## **使用说明**

###类图
结构其实很简单，没有多少东西。
![uml](http://img.my.csdn.net/uploads/201512/08/1449567185_3103.jpg)


###普通任务
```
//当LoadListenerImpl构造函数传入context，则显示progressdialog
 doNormalTask(new LoadListenerImpl(this) {
          @Override
          public void onStart(Object result) {
                  super.onStart(result);
                  setMessage("任务开始执行");
          }

          @Override
          public void onSuccess(Object result) {
                 super.onSuccess(result);
                 String response = (String) result;
                 setMessage("任务结束");
          }

          @Override
          public void onFail(Object result) {
                 super.onFail(result);
                 String response = (String) result;
                 setMessage(response);
          }

          @Override
          public void onLoading(Object result) {
                super.onLoading(result);
                //以后不用写handler了，这样就可以处理了
                int response = (int) result;
                switch (response) {
                    case 1:
    			        setMessage("正在执行 response=" + response);
                        break;
	                case 2:
	                    setMessage("正在执行 response=" + response);
                       break;
	                case 3:
	                    setMessage("正在执行 response=" + response);
                        break;
                    default:
                        break;
                 }
                    }

          @Override
          public void onCancel(Object result) {
                  super.onCancel(result);
                  setMessage("任务被取消了");
          }

          @Override
          public void onCancelClick() {
	          super.onCancelClick();
              TaskPool.getInstance().cancelTask(MainActivity.this);
          }
  }, this);
```
在上面的代码块中，LoadListenerImpl是LoadListener的实现类，用于监听任务加载的整个过程，使用LoadListenerImpl而不是LoadListener的好处有两点：
1.可以不实现所有的方法，只要根据自己的需要来实现相应的方法就行了；‘
2.LoadListenerImpl中可以管理ProgressDialog，ProgressDialog可以用tractor中自带的，也可以自己定义。
LoadListenerImpl 部分源码：
```
public class LoadListenerImpl implements LoadListener {
    private WeakReference<Context> context;
    private ProgressDialog mProgressDialog;
    private String mMessage = "加载中...";
    private long mDismissTime = 500;

    /**
     * 不显示progressdialog
     */
    public LoadListenerImpl() {
    }

    /**
     * 显示progressdialog，其上显示的文字是默认的
     * @param context
     */
    public LoadListenerImpl(Context context) {
        init(context, null);
    }

    /**
     * 显示progressdialog,其上显示的文字是message
     * @param context
     * @param message
     */
    public LoadListenerImpl(Context context, String message) {
        init(context, message);
    }
    /**
     * 设置自定义的progressdialog，如果不设置则使用tractor自带的
     * @param progressDialog
     */
    public void setProgressDialog(ProgressDialog progressDialog) {
        mProgressDialog = progressDialog;
    }
    ......
}

```
当然了，你也可以自己实现LoadListener，毕竟是面向接口编程。
doNarmalTask方法的具体实现
```
 /**
     * 发起个普通的任务
     *
     * @param listener
     * @param tag
     */
    public void doNormalTask(LoadListener listener, Object tag) {
        TaskPool.getInstance().execute(new Task(tag, listener) {
            @Override
            public void onRun() {
                SystemClock.sleep(500);
                notifyLoading(1);
                SystemClock.sleep(500);
                notifyLoading(2);
                SystemClock.sleep(500);
                notifyLoading(3);
                SystemClock.sleep(500);
                Random random = new Random();
                //任务是模拟的，所以随机下
                if (random.nextBoolean()) {
                //notifySuccess(null);
                } else {
                    notifyFail("糟糕，任务失败了");
                }
            }

            @Override
            public void cancelTask() {

            }
        });
    }
```
TaskPool.getInstance().execute()方法最终是把task交由线程池来执行，TaskPool只负责添加和取消任务。接下来说Task，在上面的类图中有说明，Task是实现了Runnable接口，并重写run(),所以线程池可以执行Task，我们看下run()方法是怎么实现的:
```
public abstract class Task implements Runnable {
    ......
    @Override
    public final void run() {
        start();
        onRun();
        finish();
    }
     ......
     private void start() {
      notifyStart(null);
       ......
    }
    /**
     * 实现这个方法来执行具体的任务
     */
    public abstract void onRun();
    private void finish() {
        if (isRunning()) {
            // 默认加载成功
            mStatus = Status.SUCCESS;
            notifySuccess(null);
        }
        clear();
    }

```
run()中分别执行了start(),onRun()和finish()，start()方法中调用了notifyStart(null)，finish()中调用了notifySuccess(null)，也就是说在开始的时候会通知ui线程任务开始，结束的时候默认通知ui线程任务结束。onRun()是抽象方法，是给任务调用者来实现执行具体的任务的，在执行的过程中可以通过notifyLoading(result)来通知ui任务的进度，notifySuccess(result)和notifyFail(result)通知ui任务成功和失败，并把需要的数据result作为参数传给ui线程。至于Task中的cancelTask()放到后面取消任务的时候再说。
###超时任务

```
 doTimeoutTask(500, new LoadListenerImpl() {
                    @Override
                    public void onStart(Object result) {
                        super.onStart(result);
                        toast("超时任务开始执行");
                    }

                    @Override
                    public void onSuccess(Object result) {
                        super.onSuccess(result);
                        toast("超时任务执行成功");
                    }

                    @Override
                    public void onTimeout(Object result) {
                        super.onTimeout(result);
                        toast("任务超时");
                    }
                }, this);
  .......
  public void doTimeoutTask(long timeout, LoadListener listener, Object tag) {
        TaskPool.getInstance().execute(new Task(timeout, tag, listener) {
            @Override
            public void onRun() {
                SystemClock.sleep(1000);
            }

            @Override
            public void cancelTask() {

            }
        });
    }
```
可以看到，超时任务相较于普通任务来说只是Task构造函数多了个timeout参数，这个timeout参数的含义就是任务执行的时间限制，如果超过这个限制就回调onTimeout()方法。
###取消任务
在平时的开发过程中，有时开了一个超耗时的操作，在耗时操作未返回的时候页面就被关闭了，当页面关闭以后耗时操作才有了返回，这时候需要操作控件的话就有可能会报null或者其他的一些异常，为了避免异常，我们通常需要进行一些页面是否处于激活状态的判断，但是这样总是很麻烦的。tractor解决了这个问题，可以调用取消任务的方法，就像这样：
```
//取消任务的方法，参数可以是任务的tag，也可以是task，如果是tag，则取消tag相关的所有任务，是task则取消指定的task。
//可以在onDestroy()中调用
TaskPool.getInstance().cancelTask(tag|task);
```
 这样调用以后会调用notifyCancel(null),在ui上显示给用户任务已取消的效果，tractor有个特性：当任务有结果时（已经取消，超时，成功和失败），后续的notifyXXX()就都不会通知到ui线程了，所以如果任务在执行取消任务代码以后，当任务有结果返回的时候，ui回调也不会被执行，那么上面说的那个问题也就不存在了，就省的自己去加判断了。
前面也有提到，取消任务的代码只能在保证ui上有取消的效果，可是任务实际上还是在执行的，虽然用户看不到可是资源还是在损耗，所以还不行。
从类图中能看到Task有onRun()和cancelTask()两个抽象方法,onRun()是执行具体任务的，cancelTask()则是执行具体的取消任务的操作，他是在非ui线程中执行的，具体怎么停止任务是由你来决定的。

###网络请求部分
为了让任务管理器看起来更有用些，我封装了网络框架，实现是okhttp，可以支持head,get,post,多线程下载，大文件上传以及其他一些http请求。由于是面向接口编程，所以如果以后有更合适的库，可以很方便的就换掉okhttp。这部分的例子我就不贴出来了，感兴趣可以自己看代码，很简单，代码全在demo module的MainActivity里。
下载：
![下载](http://img.my.csdn.net/uploads/201512/09/1449629046_4805.gif)
上传打文件：
![上传打文件](http://img.my.csdn.net/uploads/201512/09/1449628980_4123.gif)

##**最后**
如果你有什么问题和建议可以留言或者给我发邮件。我的博客地址：[https://github.com/huxq17/tractor](https://github.com/huxq17/tractor)


## License

    Copyright (C) 2015 huxq17

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
