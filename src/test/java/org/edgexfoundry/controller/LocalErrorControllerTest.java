/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * @microservice: support-notifications
 * @author: Jim White, Dell
 * @version: 1.0.0
 *******************************************************************************/

package org.edgexfoundry.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.edgexfoundry.controller.impl.LocalErrorController;
import org.edgexfoundry.test.category.RequiresNone;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;

@Category(RequiresNone.class)
public class LocalErrorControllerTest {

  private static final String PATH = "/error";

  @InjectMocks
  private LocalErrorController controller;

  @Mock
  private ErrorAttributes attributes;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetErrorPath() {
    assertEquals("Path not the same as controller's path", PATH, controller.getErrorPath());
    assertTrue("Error string does not begin with null",
        controller.error(request, response).startsWith("null"));
  }

}
