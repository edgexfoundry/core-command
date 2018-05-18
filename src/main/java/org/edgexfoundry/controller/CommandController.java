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

import java.util.List;

import org.edgexfoundry.domain.CommandResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface CommandController {

  /**
   * Retrieve a list of (all) devices and their command offerings. Throws ServiceException (HTTP
   * 503) for unanticipated or unknown issues encountered.
   * 
   * @param host - provided by the Spring runtime via the request header, the host address.
   * @return List of CommandResponse (containing the devices and their commands)
   */
  List<CommandResponse> devices(@RequestHeader("host") String host);

  /**
   * Retrieve a device (by database generated id) and its command offerings. Throws ServiceException
   * (HTTP 503) for unanticipated or unknown issues encountered. Throws NotFoundException (HTTP 404)
   * if no device exists by the id provided.
   * 
   * @param id - the database generated id for the device
   * @param host - provided by the Spring runtime via the request header, the host address.
   * @return CommandResponse containing the device and its commands
   */
  CommandResponse device(@PathVariable String id, @RequestHeader("host") String host);

  /**
   * Retrieve a device (by name) and its command offerings. Throws ServiceException (HTTP 503) for
   * unanticipated or unknown issues encountered. Throws NotFoundException (HTTP 404) if no device
   * exists by the name provided.
   * 
   * @param name - the name of the device
   * @param host - provided by the Spring runtime via the request header, the host address.
   * @return CommandResponse containing the device and its commands
   */
  CommandResponse deviceByName(@PathVariable String name, @RequestHeader("host") String host);

  /**
   * Issue the put command referenced by the command id to the device/sensor (also referenced by
   * database generated id) it is associated to via the device service. ServiceException (HTTP 503)
   * for unanticipated or unknown issues encountered. Throws NotFoundException (HTTP 404) if no
   * device exists by the id provided. Throws LockedException (HTTP 423) if the device is locked
   * (admin state).
   * 
   * @param id - the database generated id for the device to receive the put command request
   * @param commandid - the id (database generated id) of the command to issue to the device
   * @param body - JSON data to send with the command request
   * @return String as returned by the device/sensor via the device service.
   */
  ResponseEntity<String> put(@PathVariable String id, @PathVariable String commandid,
      @RequestBody String body);

  /**
   * Issue the get command referenced by the command id to the device/sensor (also referenced by
   * database generated id) it is associated to via the device service. ServiceException (HTTP 503)
   * for unanticipated or unknown issues encountered. Throws NotFoundException (HTTP 404) if no
   * device exists by the id provided. Throws LockedException (HTTP 423) if the device is locked
   * (admin state).
   * 
   * @param id - the database generated id for the device to receive the put command request
   * @param commandid - the id (database generated id) of the command to issue to the device
   * @return String as returned by the device/sensor via the device service.
   */
  ResponseEntity<String> get(@PathVariable String id, @PathVariable String commandid);

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
  void putOpState(@PathVariable String id, @PathVariable String opState);

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
  void putOpStateByName(@PathVariable String name, @PathVariable String opState);

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
  void putAdminState(@PathVariable String id, @PathVariable String adminState);

  /**
   * Set the admin state of the device (by name of the device) to the state provided (either locked
   * or unlocked). ServiceException (HTTP 503) for unanticipated or unknown issues encountered.
   * Throws NotFoundException (HTTP 404) if no device exists by the name provided.
   * 
   * @param name - the name of the device
   * 
   * @param adminState - either enabled or disabled as a String
   * @return - 200 HTTP Status Code indicates success
   */
  void putAdminStateByName(@PathVariable String name, @PathVariable String adminState);

}
