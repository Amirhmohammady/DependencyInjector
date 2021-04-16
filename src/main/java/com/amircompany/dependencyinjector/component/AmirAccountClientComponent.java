package com.amircompany.dependencyinjector.component;

import com.amircompany.dependencyinjector.annotations.AmirAutowired;
import com.amircompany.dependencyinjector.annotations.AmirComponent;
import com.amircompany.dependencyinjector.annotations.AmirQualifier;
import com.amircompany.dependencyinjector.services.AmirAccountService;
import com.amircompany.dependencyinjector.services.AmirService;

/**
 * Created by Amir on 4/16/2021.
 */

@AmirComponent
public class AmirAccountClientComponent {

    @AmirAutowired
    private AmirService amirservice;

    @AmirAutowired
    @AmirQualifier(value = "accountServiceImpl")
    private AmirAccountService amiraccountservice;

    public void displayUserAccount() {
        String username = amirservice.getUserName();
        Long accountNumber = amiraccountservice.getAccountNumber(username);
        System.out.println("\n\tUser Name: " + username + "\n\tAccount Number: " + accountNumber);
    }
}