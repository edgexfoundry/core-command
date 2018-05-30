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

package org.edgexfoundry.controller;

import static org.edgexfoundry.test.data.CommandData.newTestInstance;
import static org.edgexfoundry.test.data.CommandResponseData.checkTestData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.edgexfoundry.controller.impl.CommandControllerImpl;
import org.edgexfoundry.domain.CommandResponse;
import org.edgexfoundry.domain.meta.AdminState;
import org.edgexfoundry.domain.meta.Command;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.DeviceService;
import org.edgexfoundry.domain.meta.OperatingState;
import org.edgexfoundry.domain.meta.Protocol;
import org.edgexfoundry.exception.controller.LockedException;
import org.edgexfoundry.exception.controller.NotFoundException;
import org.edgexfoundry.exception.controller.ServiceException;
import org.edgexfoundry.test.category.RequiresNone;
import org.edgexfoundry.test.data.AddressableData;
import org.edgexfoundry.test.data.CommandResponseData;
import org.edgexfoundry.test.data.DeviceData;
import org.edgexfoundry.test.data.ServiceData;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Category(RequiresNone.class)
public class CommandControllerTest {

  private static final String TEST_ERR_MSG = "test message";

  private static final String TEST_DEV_ID = "123";
  private static final String TEST_CMD_ID = "123";
  private static final String TEST_CMD_BODY = "{\"origin\":\"123456789\"}";

  @InjectMocks
  private CommandControllerImpl controller;

  @Mock
  DeviceClient deviceClient;

  @Mock
  CommandClient commandClient;

  private Command command;
  private Device device;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    command = newTestInstance();
    device = DeviceData.newTestInstance();
  }

  @Test
  public void testDevices() {
    List<Device> devs = new ArrayList<>();
    devs.add(device);
    when(deviceClient.devices()).thenReturn(devs);
    List<CommandResponse> responses = controller.devices(CommandResponseData.TEST_HOST);
    assertEquals("Find all device command responses not returning a list with one device", 1,
        responses.size());
    checkTestData(responses.get(0));
  }

  @Test(expected = ServiceException.class)
  public void testDevicesException() {
    when(deviceClient.devices()).thenThrow(new RuntimeException(TEST_ERR_MSG));
    controller.devices(CommandResponseData.TEST_HOST);
  }

  @Test
  public void testDevice() {
    when(deviceClient.device(TEST_DEV_ID)).thenReturn(DeviceData.newTestInstance());
    checkTestData(controller.device(TEST_DEV_ID, CommandResponseData.TEST_HOST));
  }

  @Test(expected = ServiceException.class)
  public void testDeviceException() {
    when(deviceClient.device(TEST_DEV_ID)).thenThrow(new RuntimeException(TEST_ERR_MSG));
    controller.device(TEST_DEV_ID, CommandResponseData.TEST_HOST);
  }

  @Test(expected = NotFoundException.class)
  public void testDeviceNotFound() {
    when(deviceClient.device(TEST_DEV_ID))
        .thenThrow(new javax.ws.rs.NotFoundException(TEST_ERR_MSG));
    controller.device(TEST_DEV_ID, CommandResponseData.TEST_HOST);
  }

  @Test
  public void testDeviceByName() {
    when(deviceClient.deviceForName(DeviceData.TEST_NAME)).thenReturn(DeviceData.newTestInstance());
    checkTestData(controller.deviceByName(DeviceData.TEST_NAME, CommandResponseData.TEST_HOST));
  }

  @Test(expected = ServiceException.class)
  public void testDeviceByNameException() {
    when(deviceClient.deviceForName(DeviceData.TEST_NAME))
        .thenThrow(new RuntimeException(TEST_ERR_MSG));
    controller.deviceByName(DeviceData.TEST_NAME, CommandResponseData.TEST_HOST);
  }

  @Test(expected = NotFoundException.class)
  public void testDeviceByNameNotFound() {
    when(deviceClient.deviceForName(DeviceData.TEST_NAME))
        .thenThrow(new javax.ws.rs.NotFoundException(TEST_ERR_MSG));
    controller.deviceByName(DeviceData.TEST_NAME, CommandResponseData.TEST_HOST);
  }

  @Test(expected = ServiceException.class) // can't make last call to the DS via issueCommand
  public void testPut() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.device(TEST_DEV_ID)).thenReturn(device);
    when(commandClient.command(TEST_CMD_ID)).thenReturn(command);
    controller.put(TEST_DEV_ID, TEST_CMD_ID, TEST_CMD_BODY);
  }

  @Test(expected = NotFoundException.class)
  public void testPutDeviceNotFound() {
    when(deviceClient.device(TEST_DEV_ID))
        .thenThrow(new javax.ws.rs.NotFoundException(TEST_ERR_MSG));
    controller.put(TEST_DEV_ID, TEST_CMD_ID, TEST_CMD_BODY);
  }

  @Test(expected = NotFoundException.class)
  public void testPutCommandNotFound() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.device(TEST_DEV_ID)).thenReturn(device);
    when(commandClient.command(TEST_CMD_ID)).thenReturn(null);
    controller.put(TEST_DEV_ID, TEST_CMD_ID, TEST_CMD_BODY);
  }

  @Test(expected = LockedException.class)
  public void testPutDeviceLocked() {
    device.setAdminState(AdminState.LOCKED);
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.device(TEST_DEV_ID)).thenReturn(device);
    when(commandClient.command(TEST_CMD_ID)).thenReturn(command);
    controller.put(TEST_DEV_ID, TEST_CMD_ID, TEST_CMD_BODY);
  }

  @Test(expected = LockedException.class)
  public void testPutNotOperational() {
    device.setOperatingState(OperatingState.DISABLED);
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.device(TEST_DEV_ID)).thenReturn(device);
    when(commandClient.command(TEST_CMD_ID)).thenReturn(command);
    controller.put(TEST_DEV_ID, TEST_CMD_ID, TEST_CMD_BODY);
  }

  @Test(expected = ServiceException.class) // can't make last call to the DS via issueCommand
  public void testGet() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.device(TEST_DEV_ID)).thenReturn(device);
    when(commandClient.command(TEST_CMD_ID)).thenReturn(command);
    controller.get(TEST_DEV_ID, TEST_CMD_ID);
  }


  @Test(expected = NotFoundException.class)
  public void testGetDeviceNotFound() {
    when(deviceClient.device(TEST_DEV_ID))
        .thenThrow(new javax.ws.rs.NotFoundException(TEST_ERR_MSG));
    controller.get(TEST_DEV_ID, TEST_CMD_ID);
  }

  @Test(expected = NotFoundException.class)
  public void testGetCommandNotFound() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.device(TEST_DEV_ID)).thenReturn(device);
    when(commandClient.command(TEST_CMD_ID)).thenReturn(null);
    controller.get(TEST_DEV_ID, TEST_CMD_ID);
  }

  @Test(expected = LockedException.class)
  public void testGetDeviceLocked() {
    device.setAdminState(AdminState.LOCKED);
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.device(TEST_DEV_ID)).thenReturn(device);
    when(commandClient.command(TEST_CMD_ID)).thenReturn(command);
    controller.get(TEST_DEV_ID, TEST_CMD_ID);
  }

  @Test
  public void testPutOpState() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateOpState(TEST_DEV_ID, OperatingState.ENABLED.toString()))
        .thenReturn(true);
    controller.putOpState(TEST_DEV_ID, OperatingState.ENABLED.toString());
  }

  @Test(expected = NotFoundException.class)
  public void testPutOpStateDeviceNotFound() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateOpState(TEST_DEV_ID, OperatingState.ENABLED.toString()))
        .thenThrow(new javax.ws.rs.NotFoundException(TEST_ERR_MSG));
    controller.putOpState(TEST_DEV_ID, OperatingState.ENABLED.toString());
  }

  @Test(expected = ServiceException.class)
  public void testPutOpStateException() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateOpState(TEST_DEV_ID, OperatingState.ENABLED.toString()))
        .thenThrow(new RuntimeException(TEST_ERR_MSG));
    controller.putOpState(TEST_DEV_ID, OperatingState.ENABLED.toString());
  }

  @Test
  public void testPutOpStateByName() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateOpStateByName(DeviceData.TEST_NAME, OperatingState.ENABLED.toString()))
        .thenReturn(true);
    controller.putOpStateByName(DeviceData.TEST_NAME, OperatingState.ENABLED.toString());
  }

  @Test(expected = NotFoundException.class)
  public void testPutOpStateByNameDeviceNotFound() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateOpStateByName(DeviceData.TEST_NAME, OperatingState.ENABLED.toString()))
        .thenThrow(new javax.ws.rs.NotFoundException(TEST_ERR_MSG));
    controller.putOpStateByName(DeviceData.TEST_NAME, OperatingState.ENABLED.toString());
  }

  @Test(expected = ServiceException.class)
  public void testPutOpStateByNameException() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateOpStateByName(DeviceData.TEST_NAME, OperatingState.ENABLED.toString()))
        .thenThrow(new RuntimeException(TEST_ERR_MSG));
    controller.putOpStateByName(DeviceData.TEST_NAME, OperatingState.ENABLED.toString());
  }

  @Test
  public void testPutAdminState() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateAdminState(TEST_DEV_ID, AdminState.UNLOCKED.toString()))
        .thenReturn(true);
    controller.putAdminState(TEST_DEV_ID, AdminState.UNLOCKED.toString());
  }

  @Test(expected = NotFoundException.class)
  public void testPutAdminStateDeviceNotFound() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateAdminState(TEST_DEV_ID, AdminState.UNLOCKED.toString()))
        .thenThrow(new javax.ws.rs.NotFoundException(TEST_ERR_MSG));
    controller.putAdminState(TEST_DEV_ID, AdminState.UNLOCKED.toString());
  }

  @Test(expected = ServiceException.class)
  public void testPutAdminStateException() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateAdminState(TEST_DEV_ID, AdminState.UNLOCKED.toString()))
        .thenThrow(new RuntimeException(TEST_ERR_MSG));
    controller.putAdminState(TEST_DEV_ID, AdminState.UNLOCKED.toString());
  }

  @Test
  public void testPutAdminStateByName() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateAdminStateByName(DeviceData.TEST_NAME, AdminState.UNLOCKED.toString()))
        .thenReturn(true);
    controller.putAdminStateByName(DeviceData.TEST_NAME, AdminState.UNLOCKED.toString());
  }

  @Test(expected = NotFoundException.class)
  public void testPutAdminStateByNameDeviceNotFound() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateAdminStateByName(DeviceData.TEST_NAME, AdminState.UNLOCKED.toString()))
        .thenThrow(new javax.ws.rs.NotFoundException(TEST_ERR_MSG));
    controller.putAdminStateByName(DeviceData.TEST_NAME, AdminState.UNLOCKED.toString());
  }

  @Test(expected = ServiceException.class)
  public void testPutAdminStateByNameException() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    when(deviceClient.updateAdminStateByName(DeviceData.TEST_NAME, AdminState.UNLOCKED.toString()))
        .thenThrow(new RuntimeException(TEST_ERR_MSG));
    controller.putAdminStateByName(DeviceData.TEST_NAME, AdminState.UNLOCKED.toString());
  }

  @Test(expected = ServiceException.class) // can't make last call to the DS via issueCommand
  public void testServiceOtherProtocolOverGEt() {
    DeviceService service = ServiceData.newTestInstance();
    service.setAddressable(AddressableData.newTestInstance());
    device.setService(service);
    service.getAddressable().setProtocol(Protocol.ZMQ);
    when(deviceClient.device(TEST_DEV_ID)).thenReturn(device);
    when(commandClient.command(TEST_CMD_ID)).thenReturn(command);
    controller.get(TEST_DEV_ID, TEST_CMD_ID);
  }

}
