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

package org.edgexfoundry.controller.integration;

import static org.edgexfoundry.test.data.CommandResponseData.TEST_HOST;
import static org.edgexfoundry.test.data.CommandResponseData.TEST_PARAMS;
import static org.edgexfoundry.test.data.CommandResponseData.checkTestData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.List;

import org.edgexfoundry.Application;
import org.edgexfoundry.controller.AddressableClient;
import org.edgexfoundry.controller.CommandClient;
import org.edgexfoundry.controller.DeviceClient;
import org.edgexfoundry.controller.DeviceProfileClient;
import org.edgexfoundry.controller.DeviceServiceClient;
import org.edgexfoundry.controller.impl.AddressableClientImpl;
import org.edgexfoundry.controller.impl.CommandClientImpl;
import org.edgexfoundry.controller.impl.CommandControllerImpl;
import org.edgexfoundry.controller.impl.DeviceClientImpl;
import org.edgexfoundry.controller.impl.DeviceProfileClientImpl;
import org.edgexfoundry.controller.impl.DeviceServiceClientImpl;
import org.edgexfoundry.domain.CommandResponse;
import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.AdminState;
import org.edgexfoundry.domain.meta.Command;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.DeviceProfile;
import org.edgexfoundry.domain.meta.DeviceService;
import org.edgexfoundry.domain.meta.OperatingState;
import org.edgexfoundry.exception.controller.NotFoundException;
import org.edgexfoundry.exception.controller.ServiceException;
import org.edgexfoundry.test.category.RequiresMetaDataRunning;
import org.edgexfoundry.test.category.RequiresMongoDB;
import org.edgexfoundry.test.category.RequiresSpring;
import org.edgexfoundry.test.category.RequiresWeb;
import org.edgexfoundry.test.data.AddressableData;
import org.edgexfoundry.test.data.CommandData;
import org.edgexfoundry.test.data.DeviceData;
import org.edgexfoundry.test.data.ProfileData;
import org.edgexfoundry.test.data.ServiceData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration("src/test/resources")
@Category({RequiresMongoDB.class, RequiresMetaDataRunning.class, RequiresSpring.class,
    RequiresWeb.class})
public class CommandControllerTest {

  private static final String CLIENT_FIELD = "deviceClient";
  private static int TEST_PORT = 48089;

  private static final String ENDPT = "http://localhost:48081/api/v1/device";
  private static final String SRV_ENDPT = "http://localhost:48081/api/v1/deviceservice";
  private static final String PRO_ENDPT = "http://localhost:48081/api/v1/deviceprofile";
  private static final String ADDR_ENDPT = "http://localhost:48081/api/v1/addressable";
  private static final String CMD_ENDPT = "http://localhost:48081/api/v1/command";

  @Autowired
  private CommandControllerImpl controller;

  private DeviceClient client;
  private DeviceServiceClient srvClient;
  private DeviceProfileClient proClient;
  private AddressableClient addrClient;
  private CommandClient cmdClient;
  private String id;
  private String pId;

  @Before
  public void setup() throws Exception {
    client = new DeviceClientImpl();
    srvClient = new DeviceServiceClientImpl();
    proClient = new DeviceProfileClientImpl();
    addrClient = new AddressableClientImpl();
    cmdClient = new CommandClientImpl();
    setURL();
    Addressable address = AddressableData.newTestInstance();
    addrClient.add(address);
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(address);
    srvClient.add(service);
    DeviceProfile profile = ProfileData.newTestInstance();
    Command command = CommandData.newTestInstance();
    profile.addCommand(command);
    pId = proClient.add(profile);
    Device device = DeviceData.newTestInstance();
    device.setAddressable(address);
    device.setProfile(profile);
    device.setService(service);
    id = client.add(device);
    assertNotNull("Device did not get created correctly", id);
  }

  private void setURL() throws Exception {
    Class<?> clientClass = client.getClass();
    Field temp = clientClass.getDeclaredField("url");
    temp.setAccessible(true);
    temp.set(client, ENDPT);
    Class<?> clientClass2 = proClient.getClass();
    Field temp2 = clientClass2.getDeclaredField("url");
    temp2.setAccessible(true);
    temp2.set(proClient, PRO_ENDPT);
    Class<?> clientClass3 = srvClient.getClass();
    Field temp3 = clientClass3.getDeclaredField("url");
    temp3.setAccessible(true);
    temp3.set(srvClient, SRV_ENDPT);
    Class<?> clientClass4 = addrClient.getClass();
    Field temp4 = clientClass4.getDeclaredField("url");
    temp4.setAccessible(true);
    temp4.set(addrClient, ADDR_ENDPT);
    Class<?> clientClass5 = cmdClient.getClass();
    Field temp5 = clientClass5.getDeclaredField("url");
    temp5.setAccessible(true);
    temp5.set(cmdClient, CMD_ENDPT);
  }

  @After
  public void cleanup() throws Exception {
    resetClient();
    List<Device> devices = client.devices();
    devices.forEach((device) -> client.delete(device.getId()));
    List<DeviceProfile> profiles = proClient.deviceProfiles();
    profiles.forEach((profile) -> proClient.delete(profile.getId()));
    List<DeviceService> devServices = srvClient.deviceServices();
    devServices.forEach((service) -> srvClient.delete(service.getId()));
    List<Addressable> addressables = addrClient.addressables();
    addressables.forEach((addressable) -> addrClient.delete(addressable.getId()));
    List<Command> cmds = cmdClient.commands();
    cmds.forEach((cmd) -> cmdClient.delete(cmd.getId()));
  }

  @Test
  public void testDevices() {
    List<CommandResponse> responses = controller.devices(TEST_HOST);
    assertEquals("Find all device command responses not returning a list with one device", 1,
        responses.size());
    checkTestData(responses.get(0));
  }

  @Test(expected = ServiceException.class)
  public void testDevicesWithNoClient() throws Exception {
    unsetClient();
    controller.devices(TEST_HOST);
  }

  @Test
  public void testDevice() {
    checkTestData(controller.device(id, TEST_HOST));
  }

  @Test(expected = ServiceException.class)
  public void testDeviceWithNoClient() throws Exception {
    unsetClient();
    controller.device(id, TEST_HOST);
  }

  @Test(expected = NotFoundException.class)
  public void testDeviceWithBadId() {
    controller.device("baddeviceid", TEST_HOST);
  }

  @Test
  public void testDeviceByName() {
    checkTestData(controller.deviceByName(DeviceData.TEST_NAME, TEST_HOST));
  }

  @Test(expected = NotFoundException.class)
  public void testDeviceWithBadName() {
    controller.deviceByName("baddevicename", TEST_HOST);
  }

  @Test(expected = ServiceException.class)
  public void testDeviceByNameWithNoClient() throws Exception {
    unsetClient();
    controller.deviceByName(DeviceData.TEST_NAME, TEST_HOST);
  }

  @Test
  public void testGet() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(TEST_PORT), 0);
    server.createContext("/", new TestHandler());
    server.setExecutor(null);
    server.start();
    DeviceProfile profile = proClient.deviceProfile(pId);
    assertEquals("get response not ok", HttpStatus.OK,
        controller.get(id, profile.getCommands().get(0).getId()).getStatusCode());
    server.stop(0);
  }

  @Test(expected = ServiceException.class)
  public void testGetWithNoClient() throws Exception {
    unsetClient();
    DeviceProfile profile = proClient.deviceProfile(pId);
    controller.get(id, profile.getCommands().get(0).getId()).toString();
  }

  @Test(expected = NotFoundException.class)
  public void testGetWithBadId() {
    controller.get("badid", "badcommandid").toString();
  }

  @Test
  public void testPut() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(TEST_PORT), 0);
    server.createContext("/", new TestHandler());
    server.setExecutor(null);
    server.start();
    DeviceProfile profile = proClient.deviceProfile(pId);
    assertEquals("put response not ok", HttpStatus.OK,
        controller.put(id, profile.getCommands().get(0).getId(), TEST_PARAMS).getStatusCode());
    server.stop(0);
  }

  @Test(expected = ServiceException.class)
  public void testPutWithNoClient() throws Exception {
    unsetClient();
    DeviceProfile profile = proClient.deviceProfile(pId);
    controller.put(id, profile.getCommands().get(0).getId(), TEST_PARAMS).toString();
  }

  @Test(expected = NotFoundException.class)
  public void testPutWithBadId() {
    controller.put("badid", "badcommandid", TEST_PARAMS).toString();
  }

  @Test(expected = NotFoundException.class)
  public void testPutOpStateWithBadId() throws IOException {
    controller.putOpState("badid", OperatingState.DISABLED.toString());
  }

  @Test(expected = ServiceException.class)
  public void testPutOpStateWithNoClient() throws Exception {
    unsetClient();
    controller.putOpState("badid", OperatingState.DISABLED.toString());
  }

  @Test
  public void testPutOpState() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(TEST_PORT), 0);
    server.createContext("/", new TestHandler());
    server.setExecutor(null);
    server.start();
    controller.putOpState(id, OperatingState.DISABLED.toString());
    server.stop(0);
  }

  @Test
  public void testPutOpStateByName() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(TEST_PORT), 0);
    server.createContext("/", new TestHandler());
    server.setExecutor(null);
    server.start();
    controller.putOpStateByName(DeviceData.TEST_NAME, OperatingState.DISABLED.toString());
    server.stop(0);
  }

  @Test(expected = NotFoundException.class)
  public void testPutOpStateByNameWithBadId() throws IOException {
    controller.putOpStateByName("badname", OperatingState.DISABLED.toString());
  }

  @Test(expected = ServiceException.class)
  public void testPutOpStateByNameWithNoClient() throws Exception {
    unsetClient();
    controller.putOpStateByName("badname", OperatingState.DISABLED.toString());
  }

  @Test
  public void testPutAdminState() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(TEST_PORT), 0);
    server.createContext("/", new TestHandler());
    server.setExecutor(null);
    server.start();
    controller.putAdminState(id, AdminState.LOCKED.toString());
    server.stop(0);
  }

  @Test(expected = NotFoundException.class)
  public void testPutAdminStateWithBadId() throws IOException {
    controller.putAdminState("badid", AdminState.LOCKED.toString());
  }

  @Test(expected = ServiceException.class)
  public void testPutAdminStateWithNoClient() throws Exception {
    unsetClient();
    controller.putAdminState("badid", AdminState.LOCKED.toString());
  }

  @Test
  public void testPutAdminStateByName() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(TEST_PORT), 0);
    server.createContext("/", new TestHandler());
    server.setExecutor(null);
    server.start();
    controller.putAdminStateByName(DeviceData.TEST_NAME, AdminState.LOCKED.toString());
    server.stop(0);
  }

  @Test(expected = NotFoundException.class)
  public void testPutAdminStateByNameWithBadId() throws IOException {
    controller.putAdminStateByName("badname", AdminState.LOCKED.toString());
  }

  @Test(expected = ServiceException.class)
  public void testPutAdminStateByNameWithNoClient() throws Exception {
    unsetClient();
    controller.putAdminStateByName("badname", AdminState.LOCKED.toString());
  }

  private void unsetClient() throws Exception {
    Class<?> controllerClass = controller.getClass();
    Field temp = controllerClass.getDeclaredField(CLIENT_FIELD);
    temp.setAccessible(true);
    temp.set(controller, null);
  }

  private void resetClient() throws Exception {
    Class<?> controllerClass = controller.getClass();
    Field temp = controllerClass.getDeclaredField(CLIENT_FIELD);
    temp.setAccessible(true);
    temp.set(controller, client);
  }

  public class TestHandler implements HttpHandler {

    @Override

    public void handle(HttpExchange he) throws IOException {
      String response = "ok";
      he.sendResponseHeaders(200, response.length());
      OutputStream os = he.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }

}
