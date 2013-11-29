/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.universe.testcfx;

import org.universe.tests.jaxb.MyVo1;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService(targetNamespace = "My-First-Cfx-Service")
public interface HelloWorld {

    @WebMethod
    String returnCopyOfSecretHeader();

    @WebMethod
    String sayHi(String text);


    @WebMethod(action = "sayHi2", operationName = "sayHi2")
    String sayHi(int arg);


    /* Advanced usecase of passing an Interface in.  JAX-WS/JAXB does not
     * support interfaces directly.  Special XmlAdapter classes need to
     * be written to handle them
     */
    @WebMethod
    String sayHiToUser(User user);


    /* Map passing
     * JAXB also does not support Maps.  It handles Lists great, but Maps are
     * not supported directly.  They also require use of a XmlAdapter to map
     * the maps into beans that JAXB can use. 
     */
    // @XmlJavaTypeAdapter(IntegerUserMapAdapter.class)
    @WebMethod
    List<User> getUsers();

    MyVo1 getMyVo1();


}
// END SNIPPET: service
