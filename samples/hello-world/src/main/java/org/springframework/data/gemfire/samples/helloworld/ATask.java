package org.springframework.data.gemfire.samples.helloworld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:zhuangzhi.liu@thistech.com">Zhuangzhi Liu</a>
 *         Created on: 2015/6/15
 */
public  class ATask<R> implements Runnable {
    private static final Log log = LogFactory.getLog(ATask.class);

    volatile boolean active = false;
    final int times;
    IterFunc<R> func;
    R r=null;

    public ATask(int times, IterFunc<R> func, R def) {
        this.times = times;
        this.func = func;
        this.r = def;
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
            final long start = System.currentTimeMillis();
            for (int i = 0; i < times && active; i++) {
                r = func.apply(r, i);
                if (i % 1000 == 0) {
                    long now = System.currentTimeMillis();
                    String info = String.format("Run %d tasks, use %d ms, %d tps", i, now-start, (i * 1000) / (now - start + 1));
                    log.info(info);
                    System.out.println(info);
                }

            }
            long now = System.currentTimeMillis();
            String info = String.format("Complete %d tasks, use %d ms, %d tps", times, now-start, (times * 1000) / (now - start + 1));
            log.info(info);
            System.out.println(info);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}