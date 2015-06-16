package org.springframework.data.gemfire.samples.helloworld;

/**
 * @author <a href="mailto:zhuangzhi.liu@thistech.com">Zhuangzhi Liu</a>
 *         Created on: 2015/6/16
 */
public interface ITask extends Runnable {
    void cancel();
}
