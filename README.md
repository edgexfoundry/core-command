Main Author:  Jim White

Copyright 2016-17, Dell, Inc.

Command Service – conduit for other services to trigger action on devices/sensors via there managing device services.  The service provides an API to get the list of commands that can be issued for all devices or a single device.  Commands are divided into to groups for each device:  Gets and Puts.  Get commands are issued to a device/senor get a current value for a particular attribute on the device (like the current temperature offered by a thermostat sensor, or like the on/off status of a light).  Put commands are issued to a device/sensor to change the current state or status of a device or one of its attributes (like setting the speed in RPMs of a motor or setting the brightness of a dimmer light).