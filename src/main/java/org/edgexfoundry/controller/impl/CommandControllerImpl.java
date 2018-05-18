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

package org.edgexfoundry.controller.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.edgexfoundry.controller.CommandClient;
import org.edgexfoundry.controller.CommandController;
import org.edgexfoundry.controller.DeviceClient;
import org.edgexfoundry.domain.CommandResponse;
import org.edgexfoundry.domain.meta.AdminState;
import org.edgexfoundry.domain.meta.Command;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.DeviceService;
import org.edgexfoundry.domain.meta.OperatingState;
import org.edgexfoundry.domain.meta.Protocol;
import org.edgexfoundry.exception.controller.ClientException;
import org.edgexfoundry.exception.controller.LockedException;
import org.edgexfoundry.exception.controller.NotFoundException;
import org.edgexfoundry.exception.controller.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device")

public class CommandControllerImpl implements CommandController {

  private static final org.edgexfoundry.support.logging.client.EdgeXLogger logger =
      org.edgexfoundry.support.logging.client.EdgeXLoggerFactory
          .getEdgeXLogger(CommandControllerImpl.class);

  private static final String ERR_DEVICE_MSG = "Device ";
  private static final String LOG_SETMSG_STR = " be set to ";

  @Value("${device.service.protocol}")
  private String protocol;

  @Value("${meta.db.device.url}")
  private String url;

  @Autowired
  DeviceClient deviceClient;

  @Autowired
  CommandClient commandClient;

  // TODO - possibly cache some responses so they can be more quickly
  // returned.

  /**
   * Retrieve a list of (all) devices and their command offerings. Throws ServiceException (HTTP
   * 503) for unanticipated or unknown issues encountered.
   * 
   * @param host - provided by the Spring runtime via the request header, the host address.
   * @return List of CommandResponse (containing the devices and their commands)
   */
  @RequestMapping(method = RequestMethod.GET)
  @Override
  public List<CommandResponse> devices(@RequestHeader("host") String host) {
    try {
      return deviceClient.devices().stream().map(d -> new CommandResponse(d, host))
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error getting command responses:  " + e.getMessage());
      throw new ServiceException(e);
    }
  }

  /**
   * Retrieve a device (by database generated id) and its command offerings. Throws ServiceException
   * (HTTP 503) for unanticipated or unknown issues encountered. Throws NotFoundException (HTTP 404)
   * if no device exists by the id provided.
   * 
   * @param id - the database generated id for the device
   * @param host - provided by the Spring runtime via the request header, the host address.
   * @return CommandResponse containing the device and its commands
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  @Override
  public CommandResponse device(@PathVariable String id, @RequestHeader("host") String host) {
    try {
      Device device = deviceClient.device(id);
      return new CommandResponse(device, host);
    } catch (javax.ws.rs.NotFoundException expNotFound) {
      throw new NotFoundException(Device.class.toString(), id);
    } catch (Exception e) {
      logger.error("Error getting command response:  " + e.getMessage());
      throw new ServiceException(e);
    }
  }

  /**
   * Retrieve a device (by name) and its command offerings. Throws ServiceException (HTTP 503) for
   * unanticipated or unknown issues encountered. Throws NotFoundException (HTTP 404) if no device
   * exists by the name provided.
   * 
   * @param name - the name of the device
   * @param host - provided by the Spring runtime via the request header, the host address.
   * @return CommandResponse containing the device and its commands
   */
  @RequestMapping(value = "/name/{name:.+}", method = RequestMethod.GET)
  @Override
  public CommandResponse deviceByName(@PathVariable String name,
      @RequestHeader("host") String host) {
    try {
      Device device = deviceClient.deviceForName(name);
      return new CommandResponse(device, host);
    } catch (javax.ws.rs.NotFoundException expNotFound) {
      throw new NotFoundException(Device.class.toString(), name);
    } catch (Exception e) {
      logger.error("Error getting command response:  " + e.getMessage());
      throw new ServiceException(e);
    }
  }

  /**
   * Issue the put command referenced by the command id to the device/sensor (also referenced by
   * database generated id) it is associated to via the device service. ServiceException (HTTP 503)
   * for unanticipated or unknown issues encountered. Throws NotFoundException (HTTP 404) if no
   * device exists by the id provided. Throws LockedException (HTTP 423) if the device is locked
   * (admin state).
   * 
   * @param id - the database generated id for the device to receive the put command request
   * @param commandid - the id (database generated id) of the command to issue to the device
   * @param host - provided by the Spring runtime via the request header, the host address.
   * @param body - JSON data to send with the command request
   * @return String as returned by the device/sensor via the device service.
   */
  @RequestMapping(value = "/{id}/command/{commandid}", method = RequestMethod.PUT)
  @Override
  public ResponseEntity<String> put(@PathVariable String id, @PathVariable String commandid,
      @RequestBody String body) {
    try {
      Device device = deviceClient.device(id);
      Command command = commandClient.command(commandid);
      if (command == null) {
        throw new NotFoundException(Command.class.toString(), commandid);
      }
      if (device.getAdminState() == AdminState.LOCKED) {
        logger.info("Request to device:  " + device.getName() + " blocked because it is locked");
        throw new LockedException(ERR_DEVICE_MSG + device.getName() + " is in admin locked state");
      }
      if (device.getOperatingState() == OperatingState.DISABLED) {
        logger
            .info("Put request to device:  " + device.getName() + " blocked because it is disable");
        throw new LockedException(ERR_DEVICE_MSG + device.getName() + " is in disabled op state");
      }
      String putURL = getUrl(device, id, command, false);
      logger.info("Issuing put command to: " + putURL);
      logger.info("Command message body is:  " + body);
      return issueCommand(putURL, body, true);
    } catch (NotFoundException cmdNF) {
      throw cmdNF;
    } catch (javax.ws.rs.NotFoundException expNotFound) {
      throw new NotFoundException(Device.class.toString(), id);
    } catch (LockedException eL) {
      throw eL;
    } catch (Exception e) {
      logger.error("Error calling put command:  " + e.getMessage());
      throw new ServiceException(e);
    }
  }

  /**
   * Issue the get command referenced by the command id to the device/sensor (also referenced by
   * database generated id) it is associated to via the device service. ServiceException (HTTP 503)
   * for unanticipated or unknown issues encountered. Throws NotFoundException (HTTP 404) if no
   * device exists by the id provided. Throws LockedException (HTTP 423) if the device is locked
   * (admin state).
   * 
   * @param id - the database generated id for the device to receive the put command request
   * @param commandid - the id (database generated id) of the command to issue to the device
   * @param host - provided by the Spring runtime via the request header, the host address.
   * @return String as returned by the device/sensor via the device service.
   */
  @RequestMapping(value = "/{id}/command/{commandid}", method = RequestMethod.GET)
  @Override
  public ResponseEntity<String> get(@PathVariable String id, @PathVariable String commandid) {
    try {
      Device device = deviceClient.device(id);
      Command command = commandClient.command(commandid);
      if (command == null) {
        throw new NotFoundException(Command.class.toString(), commandid);
      }
      if (device.getAdminState() == AdminState.LOCKED) {
        logger.info("Request to device:  " + device.getName() + " blocked because it is locked");
        throw new LockedException(ERR_DEVICE_MSG + device.getName() + " is in admin locked state");
      }
      String getUrl = getUrl(device, id, command, true);
      logger.info("Issuing get command to: " + getUrl);
      return issueCommand(getUrl, null, false);
    } catch (NotFoundException cmdNF) {
      throw cmdNF;
    } catch (javax.ws.rs.NotFoundException expNotFound) {
      throw new NotFoundException(Device.class.toString(), id);
    } catch (LockedException eL) {
      throw eL;
    } catch (Exception e) {
      logger.error("Error calling get command:  " + e.getMessage());
      throw new ServiceException(e);
    }
  }

  /**
   * Set the op state of the device (as referenced by the database generated id of the device) to
   * the state provided (either enabled or disabled). ServiceException (HTTP 503) for unanticipated
   * or unknown issues encountered. Throws NotFoundException (HTTP 404) if no device exists by the
   * id provided.
   * 
   * @param id - the database generated id for the device to receive the put command request
   * 
   * @param opState - either enabled or disabled as a String
   * @return - 200 HTTP Status Code indicates success
   */
  @RequestMapping(value = "/{id}/opstate/{opState}", method = RequestMethod.PUT)
  @Override
  public void putOpState(@PathVariable String id, @PathVariable String opState) {
    try {
      deviceClient.updateOpState(id, opState);
      logger.info("Requesting op state for device: " + id + LOG_SETMSG_STR + opState);
    } catch (javax.ws.rs.NotFoundException expNotFound) {
      throw new NotFoundException(Device.class.toString(), id);
    } catch (Exception e) {
      logger.error("Error calling set of op state:  " + e.getMessage());
      throw new ServiceException(e);
    }
  }

  /**
   * Set the op state of the device (by name of the device) to the state provided (either enabled or
   * disabled). ServiceException (HTTP 503) for unanticipated or unknown issues encountered. Throws
   * NotFoundException (HTTP 404) if no device exists by the name provided.
   * 
   * @param name - the name of the device
   * 
   * @param opState - either enabled or disabled as a String
   * @return - 200 HTTP Status Code indicates success
   */
  @RequestMapping(value = "/name/{name}/opstate/{opState}", method = RequestMethod.PUT)
  @Override
  public void putOpStateByName(@PathVariable String name, @PathVariable String opState) {
    try {
      deviceClient.updateOpStateByName(name, opState);
      logger.info("Requesting op state for device: " + name + LOG_SETMSG_STR + opState);
    } catch (javax.ws.rs.NotFoundException expNotFound) {
      throw new NotFoundException(Device.class.toString(), name);
    } catch (Exception e) {
      logger.error("Error calling set of op state:  " + e.getMessage());
      throw new ServiceException(e);
    }
  }

  /**
   * Set the admin state of the device (as referenced by the database generated id of the device) to
   * the state provided (either locked or unlocked). ServiceException (HTTP 503) for unanticipated
   * or unknown issues encountered. Throws NotFoundException (HTTP 404) if no device exists by the
   * id provided.
   * 
   * @param id - the database generated id for the device to receive the put command request
   * 
   * @param adminState - either locked or unlocked as a String
   * @return - 200 HTTP Status Code indicates success
   */
  @RequestMapping(value = "/{id}/adminstate/{adminState}", method = RequestMethod.PUT)
  @Override
  public void putAdminState(@PathVariable String id, @PathVariable String adminState) {
    try {
      deviceClient.updateAdminState(id, adminState);
      logger.info("Requesting admin state for device: " + id + LOG_SETMSG_STR + adminState);
    } catch (javax.ws.rs.NotFoundException expNotFound) {
      throw new NotFoundException(Device.class.toString(), id);
    } catch (Exception e) {
      logger.error("Error calling set of admin state:  " + e.getMessage());
      throw new ServiceException(e);
    }
  }

  /**
   * Set the admin state of the device (by name of the device) to the state provided (either locked
   * or unlocked). ServiceException (HTTP 503) for unanticipated or unknown issues encountered.
   * Throws NotFoundException (HTTP 404) if no device exists by the name provided.
   * 
   * @param name - the name of the device
   * 
   * @param opState - either enabled or disabled as a String
   * @return - 200 HTTP Status Code indicates success
   */
  @RequestMapping(value = "/name/{name}/adminstate/{adminState}", method = RequestMethod.PUT)
  @Override
  public void putAdminStateByName(@PathVariable String name, @PathVariable String adminState) {
    try {
      deviceClient.updateAdminStateByName(name, adminState);
      logger.info("Requesting admin state for device: " + name + LOG_SETMSG_STR + adminState);
    } catch (javax.ws.rs.NotFoundException expNotFound) {
      throw new NotFoundException(Device.class.toString(), name);
    } catch (Exception e) {
      logger.error("Error calling set of admin state:  " + e.getMessage());
      throw new ServiceException(e);
    }
  }

  private String getUrl(Device device, String deviceId, Command command, boolean isGet) {
    DeviceService service = device.getService();
    if (service != null && service.getAddressable() != null) {
      // use REST for http services
      if (Protocol.HTTP == service.getAddressable().getProtocol()) {
        StringBuilder builder =
            new StringBuilder(service.getAddressable().getProtocol().toString());
        builder.append("://");
        builder.append(service.getAddressable().getAddress());
        builder.append(":");
        builder.append(service.getAddressable().getPort());
        if (isGet)
          builder.append(command.getGet().getPath());
        else
          builder.append(command.getPut().getPath());
        return builder.toString().replace("{deviceId}", deviceId);
      } else {
        // TODO - someday offer message send over bus (like 0MQ or MQTT
        // for protocol = TCP
        return "";
      }
    } else
      throw new ClientException("Device Service is not properly addressable");
  }

  private ResponseEntity<String> issueCommand(String url, String body, boolean isPut)
      throws IOException {
    URL command = new URL(url);
    HttpURLConnection con = (HttpURLConnection) command.openConnection();
    if (isPut) {
      con.setRequestMethod("PUT");
      con.setDoOutput(true);
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Content-Length", Integer.toString(body.length()));
      OutputStream os = con.getOutputStream();
      os.write(body.getBytes());
    }
    BufferedReader res = new BufferedReader(new InputStreamReader(con.getInputStream()));
    StringBuilder response = new StringBuilder();
    for (String responseLine = res.readLine(); responseLine != null; responseLine =
        res.readLine()) {
      response.append(responseLine);
    }
    res.close();
    return new ResponseEntity<>(response.toString(), HttpStatus.OK);
  }

}
