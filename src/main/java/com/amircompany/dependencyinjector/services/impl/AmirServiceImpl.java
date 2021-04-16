package com.amircompany.dependencyinjector.services.impl;

import com.amircompany.dependencyinjector.annotations.AmirComponent;
import com.amircompany.dependencyinjector.services.AmirService;

@AmirComponent
public class AmirServiceImpl implements AmirService {

    @Override
    public String getUserName() {
        return "username";
    }
}