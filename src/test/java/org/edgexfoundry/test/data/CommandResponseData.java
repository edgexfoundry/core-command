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
 * @microservice: core-command
 * @author: Jim White, Dell
 * @version: 1.0.0
 *******************************************************************************/

package org.edgexfoundry.test.data;

import static org.edgexfoundry.test.data.DeviceData.TEST_ADMIN;
import static org.edgexfoundry.test.data.DeviceData.TEST_DESCRIPTION;
import static org.edgexfoundry.test.data.DeviceData.TEST_LABELS;
import static org.edgexfoundry.test.data.DeviceData.TEST_LAST_CONNECTED;
import static org.edgexfoundry.test.data.DeviceData.TEST_LAST_REPORTED;
import static org.edgexfoundry.test.data.DeviceData.TEST_LOCATION;
import static org.edgexfoundry.test.data.DeviceData.TEST_NAME;
import static org.edgexfoundry.test.data.DeviceData.TEST_OP;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.edgexfoundry.domain.CommandResponse;

public interface CommandResponseData {

  static final String TEST_HOST = "localhost";
  static final String TEST_PARAMS = "{abc}";

  static void checkTestData(CommandResponse response) {
    assertEquals("Command response Host does not match expected", TEST_HOST, response.getHost());
    assertEquals("Command response device description does not match expected", TEST_DESCRIPTION,
        response.getDevice().getDescription());
    assertEquals("Command response device name does not match expected", TEST_NAME,
        response.getDevice().getName());
    assertArrayEquals("Command response device labels does not match expected", TEST_LABELS,
        response.getDevice().getLabels());
    assertEquals("Command response device last connected does not match expected",
        TEST_LAST_CONNECTED, response.getDevice().getLastConnected());
    assertEquals("Command response device last reported does not match expected",
        TEST_LAST_REPORTED, response.getDevice().getLastReported());
    assertEquals("Command response device location does not match expected", TEST_LOCATION,
        response.getDevice().getLocation());
    assertEquals("Command response device operating state does not match expected", TEST_OP,
        response.getDevice().getOperatingState());
    assertEquals("Command response device admin state does not match expected", TEST_ADMIN,
        response.getDevice().getAdminState());
  }
}
