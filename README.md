# play-plugin-acl

## Overview
A plugin for playframework1.
This plugin provides following features.

- IP filtering
- Basic authentication

## How to install
Copy dist/play-acl.jar to <your app>/lib

## IP filtering setting
There are two keys for IP filtering setting.
You should add them in your application.conf.

### flect.acl.ipfilter.allow
IP addresses for allow access.
You must set this value with IPv4 format.
You can set multiple values separating by comma.
You can use subnet format.(e.g. 192.168.0.0/24)

    flect.acl.ipfilter.allow=111.111.111.111,222.222.222.222,333.333.333,0/24

### flect.acl.ipfilter.excludes
Request path prefix for excluding IP filtering.
You can set multiple values separating by comma.

    flect.acl.ipfilter.excludes=/path1,/path2

## Basic authentication setting
You should set follwing keys in your application.conf.

- flect.acl.basicAuth.username
- flect.acl.basicAuth.password

## Hack
You can use these features directly in your Controller class.
See follwing classes.

- [IPFilter](https://github.com/shunjikonishi/play-plugin-acl/blob/master/src/play/modules/flect/IPFilter.java)
- [AuthManager](https://github.com/shunjikonishi/play-plugin-acl/blob/master/src/play/modules/flect/AuthManager.java)

## License
MIT

