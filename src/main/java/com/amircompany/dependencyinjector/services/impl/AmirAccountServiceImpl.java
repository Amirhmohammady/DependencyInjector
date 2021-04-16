package com.amircompany.dependencyinjector.services.impl;

import com.amircompany.dependencyinjector.annotations.AmirComponent;
import com.amircompany.dependencyinjector.services.AmirAccountService;

/**
 * Created by Amir on 4/16/2021.
 */
@AmirComponent
public class AmirAccountServiceImpl implements AmirAccountService {

    @Override
    public Long getAccountNumber(String userName) {
        return 12345689L;
    }
}