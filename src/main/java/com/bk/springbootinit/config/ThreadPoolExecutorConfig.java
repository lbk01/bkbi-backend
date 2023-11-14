package com.bk.springbootinit.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池注册类
 * @author bk
 * @data 2023/11/9 0:18
*/
@Configuration
public class ThreadPoolExecutorConfig
{
    @Bean
    public ThreadPoolExecutor threadPoolExecutor()
    {
        ThreadFactory threadFactory = new ThreadFactory(){
            public int  cnt = 1;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程"+cnt);
                cnt++;
                return thread;
            }
        };
        /**
         * int corePoolSize,  正常员工的个数
         *   int maximumPoolSize, 请外包了
         *         long keepAliveTime, 非核心的工作最多的等待时间
         *            BlockingQueue<Runnable> workQueue, 工作队列
         *         ThreadFactory threadFactory, 线程工厂
         *   RejectedExecutionHandler handler 拒绝规则
         */

//        一个线程池 core 7； max 20 ， queue： 50， 100并发进来怎么分配的？

//答：先有7个能直接得到执行， 接下来把50个进入队列排队等候，
// 在多开13个继续执行。 现在 70 个被安排上了。 剩下 30 个默认执行饱和策略。
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(4), threadFactory);
        return threadPoolExecutor;
    }
}
