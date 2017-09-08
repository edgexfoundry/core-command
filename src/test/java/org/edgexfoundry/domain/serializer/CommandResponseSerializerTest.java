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

package org.edgexfoundry.domain.serializer;

import static org.edgexfoundry.test.data.CommandResponseData.TEST_HOST;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.edgexfoundry.domain.CommandResponse;
import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.Command;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.DeviceProfile;
import org.edgexfoundry.test.category.RequiresNone;
import org.edgexfoundry.test.data.AddressableData;
import org.edgexfoundry.test.data.CommandData;
import org.edgexfoundry.test.data.DeviceData;
import org.edgexfoundry.test.data.ProfileData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

@Category({RequiresNone.class})
public class CommandResponseSerializerTest {

  private static final String RESULT_JSON1 =
      "{\"name\":\"TEST_DEVICE.NAME\",\"id\":null,\"description\":\"TEST_DESCRIPTION\",\"labels\":[\"MODBUS\",\"TEMP\"],\"adminState\":\"UNLOCKED\",\"operatingState\":\"ENABLED\",\"lastConnected\":1000000,\"lastReported\":1000000,\"location\":\"{40lat;45long}\",\"commands\":[{\"id\":null,\"name\":\"setTemp\",\"get\":{\"url\":\"nulllocalhostnullnullnullnull\",\"responses\":[{\"code\":\"200\",\"description\":\"ok\",\"expectedValues\":[\"temperature\",\"humidity\"]}]},\"put\":{\"url\":\"nulllocalhostnullnullnullnull\",\"parameterNames\":[\"Temperature\",\"Humidity\"],\"responses\":[{\"code\":\"200\",\"description\":\"ok\",\"expectedValues\":[\"temperature\",\"humidity\"]}]}}]}";
  private static final String RESULT_JSON2 =
      "{\"name\":\"TEST_DEVICE.NAME\",\"id\":null,\"description\":null,\"labels\":null,\"adminState\":\"UNLOCKED\",\"operatingState\":\"ENABLED\",\"lastConnected\":1000000,\"lastReported\":1000000,\"location\":null,\"commands\":null}";

  CommandResponseSerializer serializer;
  JsonGenerator jgen;
  SerializerProvider provider;
  StringWriter stringJson;

  @Before
  public void setup() throws IOException {
    serializer = new CommandResponseSerializer();
    stringJson = new StringWriter();
    jgen = new JsonFactory().createGenerator(stringJson);
    provider = new ObjectMapper().getSerializerProvider();
  }

  @After
  public void cleanup() {}

  @Test
  public void testSerialize() throws IOException {
    Addressable addressable = AddressableData.newTestInstance();
    DeviceProfile profile = ProfileData.newTestInstance();
    Command command = CommandData.newTestInstance();
    profile.addCommand(command);
    Device device = DeviceData.newTestInstance();
    device.setAddressable(addressable);
    device.setProfile(profile);
    CommandResponse resp = new CommandResponse(device, TEST_HOST);
    serializer.serialize(resp, jgen, provider);
    jgen.flush();
    jgen.close();
    assertEquals("Serialized response does not match expected", RESULT_JSON1,
        stringJson.toString());
  }

  @Test
  public void testProblematicNullFieldSerialization() throws IOException {
    Addressable addressable = AddressableData.newTestInstance();
    DeviceProfile profile = ProfileData.newTestInstance();
    Command command = CommandData.newTestInstance();
    profile.addCommand(command);
    Device device = DeviceData.newTestInstance();
    device.setAddressable(addressable);
    device.setProfile(profile);
    CommandResponse resp = new CommandResponse(device, TEST_HOST);
    // labels should be empty
    device.setLabels(null);
    // description should be empty
    device.setDescription(null);
    // location should be empty
    device.setLocation(null);
    // command should be empty
    device.getProfile().setCommands(null);
    serializer.serialize(resp, jgen, provider);
    jgen.flush();
    jgen.close();
    System.out.println(stringJson.toString());
    assertEquals("Serialized response does not match expected", RESULT_JSON2,
        stringJson.toString());

  }

}
