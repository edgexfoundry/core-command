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

import java.io.IOException;

import org.edgexfoundry.domain.CommandResponse;
import org.edgexfoundry.domain.meta.Action;
import org.edgexfoundry.domain.meta.Command;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.Put;
import org.edgexfoundry.domain.meta.Response;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CommandResponseSerializer extends JsonSerializer<CommandResponse> {

  private static final String JSON_DESC_FLD = "description";

  @Value("${url.protocol}")
  private String urlProtocol;

  @Value("${url.cmd.path}")
  private String urlCmdPath;

  @Value("${url.device.path}")
  private String urlDevicePath;

  @Override
  public void serialize(CommandResponse cmdResp, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
    Device device = cmdResp.getDevice();
    String host = cmdResp.getHost();
    // write the device to Json
    jgen.writeStartObject();
    jgen.writeStringField("name", device.getName());
    jgen.writeStringField("id", device.getId());

    if (device.getDescription() != null)
      jgen.writeStringField(JSON_DESC_FLD, device.getDescription());
    else
      jgen.writeNullField(JSON_DESC_FLD);
    if (device.getLabels() != null) {
      jgen.writeArrayFieldStart("labels");
      for (String label : device.getLabels()) {
        jgen.writeString(label);
      }
      jgen.writeEndArray();
    } else
      jgen.writeNullField("labels");
    jgen.writeStringField("adminState", device.getAdminState().toString());
    jgen.writeStringField("operatingState", device.getOperatingState().toString());
    jgen.writeNumberField("lastConnected", device.getLastConnected());
    jgen.writeNumberField("lastReported", device.getLastReported());
    if (device.getLocation() != null)
      jgen.writeObjectField("location", device.getLocation());
    else
      jgen.writeNullField("location");
    if (device.getProfile() != null && device.getProfile().getCommands() != null
        && !device.getProfile().getCommands().isEmpty()) {
      jgen.writeArrayFieldStart("commands");
      for (Command command : device.getProfile().getCommands()) {
        serializeCommand(command, jgen, host, device.getId());
      }
      jgen.writeEndArray();
    } else {
      jgen.writeNullField("commands");
    }
    jgen.writeEndObject();
  }

  public void serializeCommand(Command command, JsonGenerator jgen, String host, String deviceId)
      throws IOException {
    jgen.writeStartObject();
    jgen.writeStringField("id", command.getId());
    jgen.writeStringField("name", command.getName());
    if (command.getGet() != null) {
      jgen.writeObjectFieldStart("get");
      serializeAction(command.getGet(), jgen, host, deviceId, command.getId(), true);
      jgen.writeEndObject();
    } else
      jgen.writeNullField("get");
    if (command.getPut() != null) {
      jgen.writeObjectFieldStart("put");
      serializeAction(command.getPut(), jgen, host, deviceId, command.getId(), false);
      jgen.writeEndObject();
    } else
      jgen.writeNullField("put");
    jgen.writeEndObject();

  }

  public void serializeAction(Action action, JsonGenerator jgen, String host, String deviceId,
      String cmdId, boolean isGet) throws IOException {
    jgen.writeStringField("url",
        urlProtocol + host + urlDevicePath + deviceId + urlCmdPath + cmdId);
    if (action instanceof Put) {
      jgen.writeArrayFieldStart("parameterNames");
      for (String param : ((Put) action).getParameterNames()) {
        jgen.writeString(param);
      }
      jgen.writeEndArray();
    }
    if (action.getResponses() != null) {
      jgen.writeArrayFieldStart("responses");
      for (Response resp : action.getResponses()) {
        serializeResponse(resp, jgen);
      }
      jgen.writeEndArray();
    } else
      jgen.writeNullField("responses");
  }

  public void serializeResponse(Response resp, JsonGenerator jgen) throws IOException {
    jgen.writeStartObject();
    if (resp.getCode() != null)
      jgen.writeStringField("code", resp.getCode());
    else
      jgen.writeNullField("code");
    if (resp.getDescription() != null)
      jgen.writeStringField(JSON_DESC_FLD, resp.getDescription());
    else
      jgen.writeNullField(JSON_DESC_FLD);
    if (resp.getExpectedValues() != null) {
      jgen.writeArrayFieldStart("expectedValues");
      for (String expected : resp.getExpectedValues()) {
        jgen.writeString(expected);
      }
      jgen.writeEndArray();
    } else
      jgen.writeNullField("expectedValues");
    jgen.writeEndObject();
  }
}
