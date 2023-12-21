# Bug description
link to sqlite-jdbc opened issue https://github.com/xerial/sqlite-jdbc/issues/1042

When I run my java code with sqlite inside RStudio, using R console(rjava), I see 
`Exception: java.lang.StackOverflowError thrown from the UncaughtExceptionHandler in thread "process reaper"`

I tried several versions of java, rjava, rtudio, sqlite and what I found:

Bug reproduces only with next conditions:
- rjava call
- ubuntu has not serial (one thread) openblas library (like libopenblas0-pthread)
and only with libraries sqlite less then 3.34 and older then 3.40.1.0
- using sqlite >3.40.1.0

I think a problem in https://github.com/xerial/sqlite-jdbc/blob/master/src/main/resources/org/sqlite/native/Linux/x86_64/libsqlitejdbc.so

I tried to increase xss for rJava (`.jinit(parameters="-Xss4m")`), because I found this: https://github.com/s-u/rJava/issues//224

see `pom.xml`
```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.40.1.0</version>
</dependency>
```
also you can see diff between stackoverflow logs (it's not exactly 3.40.*.0 versions, but still fine for our investigation):
- [with_exception.txt](with_exception.txt)
- [without_exception.txt](without_exception.txt)

you can try to find differences here...
https://github.com/xerial/sqlite-jdbc/compare/3.40.0.0...3.40.1.0

# You can reproduce it using next commands:
use to build
```bash
docker build -t girchev/debugsqlite:1.1 .
```

use to run
```bash
docker run -ti -e PASSWORD=yourpassword -p 8787:8787 girchev/debugsqlite:1.1
```

If you want to use webinterface for manually uploading jar to rstudio, remove line `CMD ["R", "-f", "debug_sqlite.R"]` from `Dockerfile`, go to
`localhost:8787` and use login/passwords : `rstudio/yourpassword`

# Possible workarounds:
1. change openblas library to serial
```dockerfile
RUN apt update -qq && \
    apt install -y libopenblas-serial-dev && \
    update-alternatives --set libblas.so.3-x86_64-linux-gnu /usr/lib/x86_64-linux-gnu/openblas-serial/libblas.so.3 && \
    update-alternatives --set liblapack.so.3-x86_64-linux-gnu /usr/lib/x86_64-linux-gnu/openblas-serial/liblapack.so.3 && \
    update-alternatives --set libopenblas.so.0-x86_64-linux-gnu /usr/lib/x86_64-linux-gnu/openblas-serial/libopenblas.so.0
```
You can check current version with `update-alternatives --query libblas.so.3-x86_64-linux-gnu`

2. use previous version of sqlite
```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.40.0.0</version>
</dependency>
```
