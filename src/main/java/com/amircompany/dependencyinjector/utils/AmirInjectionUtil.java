package com.amircompany.dependencyinjector.utils;

import com.amircompany.dependencyinjector.AmirInjector;
import com.amircompany.dependencyinjector.annotations.*;
import org.burningwave.core.classes.FieldCriteria;

import static org.burningwave.core.assembler.StaticComponentContainer.Fields;

import java.lang.reflect.Field;
import java.util.*;
/**
 * Created by Amir on 4/16/2021.
 */
public class AmirInjectionUtil {

    private AmirInjectionUtil() {
        super();
    }

    /**
     * Perform injection recursively, for each service inside the Client class
     */
    public static void autowire(AmirInjector injector, Class<?> classz, Object classInstance)
            throws InstantiationException, IllegalAccessException {
        Collection<Field> fields = Fields.findAllAndMakeThemAccessible(
                FieldCriteria.forEntireClassHierarchy().allThoseThatMatch(field ->
                                field.isAnnotationPresent(AmirAutowired.class)
                ),
                classz
        );
        for (Field field : fields) {
            String qualifier = field.isAnnotationPresent(AmirQualifier.class)
                    ? field.getAnnotation(AmirQualifier.class).value()
                    : null;
            Object fieldInstance = injector.getBeanInstance(field.getType(), field.getName(), qualifier);
            Fields.setDirect(classInstance, field, fieldInstance);
            autowire(injector, fieldInstance.getClass(), fieldInstance);
        }
    }

}