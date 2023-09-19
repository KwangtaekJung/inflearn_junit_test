package com.example.testcontainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TestContainerApplicationRunner implements ApplicationRunner {
    @Autowired
    Environment environment;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("ApplicationArguments contains foo is " + args.containsOption("foo"));
        System.out.println("ApplicationArguments contains bar is " + args.containsOption("bar"));

        System.out.println("System Property foo=" + System.getProperty("foo"));

        System.out.println("environment: foo=" + environment.getProperty("foo"));
        System.out.println("environment: bar=" + environment.getProperty("bar"));

    }
}
