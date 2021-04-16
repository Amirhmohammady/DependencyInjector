package com.amircompany.dependencyinjector;

import com.amircompany.dependencyinjector.annotations.AmirComponent;
import com.amircompany.dependencyinjector.utils.AmirInjectionUtil;
import org.burningwave.core.assembler.ComponentContainer;
import org.burningwave.core.classes.CacheableSearchConfig;
import org.burningwave.core.classes.ClassCriteria;
import org.burningwave.core.classes.ClassHunter;
import org.burningwave.core.classes.ClassHunter.*;
import org.burningwave.core.classes.SearchConfig;

import javax.management.RuntimeErrorException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Amir on 4/16/2021.
 */
public class AmirInjector {
    private static AmirInjector amirinjector;
    private Map<Class<?>, Class<?>> diMap;
    private Map<Class<?>, Object> applicationScope;

    private AmirInjector() {
        super();
        diMap = new HashMap();
        applicationScope = new HashMap();
    }

    /**
     * Start application
     *
     * @param mainClass
     */
    public static void startApplication(Class<?> mainClass) {
        try {
            synchronized (AmirInjector.class) {
                if (amirinjector == null) {
                    amirinjector = new AmirInjector();
                    amirinjector.initFramework(mainClass);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static <T> T getService(Class<T> classz) {
        try {
            return amirinjector.getBeanInstance(classz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * initialize the injector framework
     */
    private void initFramework(Class<?> mainClass)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        Class<?>[] classes = getClasses(mainClass.getPackage().getName(), true);
        ComponentContainer componentConatiner = ComponentContainer.getInstance();
        ClassHunter classHunter = componentConatiner.getClassHunter();
        String packageRelPath = mainClass.getPackage().getName().replace(".", "/");
        try (SearchResult result = classHunter.findBy(
                //Highly optimized scanning by filtering resources before loading from ClassLoader
                SearchConfig.forResources(
                        packageRelPath
                ).by(ClassCriteria.create().allThoseThatMatch(cls -> {
                    return cls.getAnnotation(AmirComponent.class) != null;
                }))
        )) {
            Collection<Class<?>> types = result.getClasses();
            for (Class<?> implementationClass : types) {
                Class<?>[] interfaces = implementationClass.getInterfaces();
                if (interfaces.length == 0) {
                    diMap.put(implementationClass, implementationClass);
                } else {
                    for (Class<?> iface : interfaces) {
                        diMap.put(implementationClass, iface);
                    }
                }
            }

            for (Class<?> classz : classes) {
                if (classz.isAnnotationPresent(AmirComponent.class)) {
                    Object classInstance = classz.newInstance();
                    applicationScope.put(classz, classInstance);
                    AmirInjectionUtil.autowire(this, classz, classInstance);
                }
            }
        }
        ;

    }

    /**
     * Get all the classes for the input package
     */
    public Class<?>[] getClasses(String packageName, boolean recursive) throws ClassNotFoundException, IOException {
        ComponentContainer componentConatiner = ComponentContainer.getInstance();
        ClassHunter classHunter = componentConatiner.getClassHunter();
        String packageRelPath = packageName.replace(".", "/");
        //Highly optimized scanning by filtering resources before loading from ClassLoader
        CacheableSearchConfig config = SearchConfig.forResources(
                packageRelPath
        );
        if (!recursive) {
            config.notRecursiveOnPath(
                    packageRelPath, false
            );
        }

        SearchResult result = null;
        try {
            result = classHunter.findBy(config);
            Collection<Class<?>> classes = result.getClasses();
            return classes.toArray(new Class[classes.size()]);
        } finally {
            if (result != null) result.close();
            return null;
        }
    }


    /**
     * Create and Get the Object instance of the implementation class for input
     * interface service
     */
    @SuppressWarnings("unchecked")
    private <T> T getBeanInstance(Class<T> interfaceClass) throws InstantiationException, IllegalAccessException {
        return (T) getBeanInstance(interfaceClass, null, null);
    }

    /**
     * Overload getBeanInstance to handle qualifier and autowire by type
     */
    public <T> Object getBeanInstance(Class<T> interfaceClass, String fieldName, String qualifier)
            throws InstantiationException, IllegalAccessException {
        Class<?> implementationClass = getImplimentationClass(interfaceClass, fieldName, qualifier);

        if (applicationScope.containsKey(implementationClass)) {
            return applicationScope.get(implementationClass);
        }

        synchronized (applicationScope) {
            Object service = implementationClass.newInstance();
            applicationScope.put(implementationClass, service);
            return service;
        }
    }

    /**
     * Get the name of the implimentation class for input interface service
     */
    private Class<?> getImplimentationClass(Class<?> interfaceClass, final String fieldName, final String qualifier) {
        Set<Map.Entry<Class<?>, Class<?>>> implementationClasses = diMap.entrySet().stream()
                .filter(entry -> entry.getValue() == interfaceClass).collect(Collectors.toSet());
        String errorMessage = "";
        if (implementationClasses == null || implementationClasses.size() == 0) {
            errorMessage = "no implementation found for interface " + interfaceClass.getName();
        } else if (implementationClasses.size() == 1) {
            Optional<Map.Entry<Class<?>, Class<?>>> optional = implementationClasses.stream().findFirst();
            if (optional.isPresent()) {
                return optional.get().getKey();
            }
        } else if (implementationClasses.size() > 1) {
            final String findBy = (qualifier == null || qualifier.trim().length() == 0) ? fieldName : qualifier;
            Optional<Map.Entry<Class<?>, Class<?>>> optional = implementationClasses.stream()
                    .filter(entry -> entry.getKey().getSimpleName().equalsIgnoreCase(findBy)).findAny();
            if (optional.isPresent()) {
                return optional.get().getKey();
            } else {
                errorMessage = "There are " + implementationClasses.size() + " of interface " + interfaceClass.getName()
                        + " Expected single implementation or make use of @CustomQualifier to resolve conflict";
            }
        }
        throw new RuntimeErrorException(new Error(errorMessage));
    }
}