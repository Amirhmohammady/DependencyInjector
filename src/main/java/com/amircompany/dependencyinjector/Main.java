package com.amircompany.dependencyinjector;

import com.amircompany.dependencyinjector.component.AmirAccountClientComponent;

/**
 * Created by Amir on 4/16/2021.
 */
public class Main {
    public static void main(String[] args) {
//        System.out.println("+++++++++++++++++++++++++++++++++++++++hello");
        long startTime = System.currentTimeMillis();
        AmirInjector.startApplication(Main.class);
        AmirInjector.getService(AmirAccountClientComponent.class).displayUserAccount();
        long endime = System.currentTimeMillis();
    }
}
