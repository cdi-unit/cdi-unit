/*
 * Copyright 2013 russell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jglue.cdiunit.internal;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.faces.view.ViewScoped;
/**
 *
 * @author russell
 */
public class ViewContext implements Context{

    private static Map<String, Object> viewMap = new HashMap<String,Object>();
    
    @Override
    public Class<? extends Annotation> getScope() {
        return ViewScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> cntxtl, CreationalContext<T> cc) {
        Bean bean = (Bean) cntxtl;
        if (viewMap.containsKey(bean.getName())){
            return (T) viewMap.get(bean.getName());
        }else{
            T t = (T) bean.create(cc);
            viewMap.put(bean.getName(), t);
            return t;
        }
    }

    @Override
    public <T> T get(Contextual<T> cntxtl) {
        Bean bean = (Bean) cntxtl;
        if (viewMap.containsKey(bean.getName())){
            return (T) viewMap.get(bean.getName());
        }else{
            return null;
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }
    
}
