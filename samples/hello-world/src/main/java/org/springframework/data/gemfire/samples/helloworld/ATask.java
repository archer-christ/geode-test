package org.springframework.data.gemfire.samples.helloworld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:zhuangzhi.liu@thistech.com">Zhuangzhi Liu</a>
 *         Created on: 2015/6/15
 */
public  class ATask<R> implements ITask {
    private static final Log log = LogFactory.getLog(ATask.class);

    final CountDownLatch cdl;
    volatile boolean active = false;
    final int times;
    final int start;
    IterFunc<R> func;
    R r=null;

    public ATask(CountDownLatch cdl,int times, int start, IterFunc<R> func, R def) {
        this.times = times;
        this.start = start;
        this.func = func;
        this.r = def;
        this.cdl = cdl;
    }

    public void cancel() {
        active = false;
    }

    public void run() {
        try {
            System.out.println("Start a task" + this);
            log.info("Start a task");
            active = true;
            R r = null;
            final long start_time = System.currentTimeMillis();
            for (int i = 0; i < times && active; i++) {
                r = func.apply(r, i+start);
                if (i % 1000 == 0) {
                    long escape = System.currentTimeMillis() - start_time;
                    String info = String.format("Run %d tasks, use %d ms, %d tps", i, escape, (i * 1000) / (escape + 1));
                    log.info(info);
                    System.out.println(info);
                }

            }
            long escape = System.currentTimeMillis() - start_time;
            String info = String.format("Complete %d tasks, use %d ms, %d tps", times, escape, (times * 1000) / (escape + 1));
            log.info(info);
            System.out.println(info);
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (cdl!=null) {
            cdl.countDown();
        }
    }

}