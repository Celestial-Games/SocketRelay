# FAQ

**If I host a community server how do i limit it from being used (abused) by people using it for something else?**

Currently the only limit is to limits how many sockets "sessions" are available. Investigations are ongoing as to how better to handle that.

**How resource heavy is the server?**

As this is a pretty new application we are still profiling it's behavior and requirements. It should be light weight as it is just a socket relay and does not do a lot of heavy lifting.

Below is the resource snapshot from the server we are running.

```console
  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND
14196 datasto+  20   0 3708916  83808  11952 S   0.3  0.7   0:01.87 java
```
