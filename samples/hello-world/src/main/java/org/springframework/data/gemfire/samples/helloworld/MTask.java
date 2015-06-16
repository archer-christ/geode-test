package org.springframework.data.gemfire.samples.helloworld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * @author <a href="mailto:zhuangzhi.liu@thistech.com">Zhuangzhi Liu</a>
 *         Created on: 2015/6/15
 */
public  class MTask<R> implements ITask  {
    private static final Log log = LogFactory.getLog(MTask.class);

    final int times;
    final IterFunc<R> func;
    final R r;
    CountDownLatch cdl;
    ArrayList<ATask> tasks = new ArrayList<ATask>();
    final Executor executor;

    public MTask(int split, Executor executor, int times, IterFunc<R> func, R def) {
        this.times = times;
        this.func = func;
        this.r = def;
        this.executor = executor;
        int num = times/split;
        this.cdl = new CountDownLatch(split);
        for (int i=0;i<split;i++) {
            ATask task = new ATask<R>(cdl,num,i*num,func,r);
            tasks.add(task);
        }
    }

    public void cancel() {
        for (ATask t : tasks) {
            t.cancel();
        }
    }

    public void run() {
        for (ATask t : tasks) {
            executor.execute(t);
        }
    }

}