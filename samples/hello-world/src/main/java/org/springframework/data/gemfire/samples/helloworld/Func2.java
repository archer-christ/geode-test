package org.springframework.data.gemfire.samples.helloworld;

/**
 * @author <a href="mailto:zhuangzhi.liu@thistech.com">Zhuangzhi Liu</a>
 *         Created on: 2015/6/15
 */
public interface Func2<R,P1,P2> {
    R apply(P1 p1, P2 id);
}
