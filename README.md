#README

Tag dumping attributes of page, session, requests and application as JSON into console like firebug.

## INSTALL

 
### MAVEN            

```

- Add dependency in your pom.xml : 
   <dependency>
      <groupId>fr.figarocms</group>
      <artifactId>web-debug-tag</artifactId>
      <version>1.5</version>
   </dependency>

```

### FREESTYLE OR OTHER :

```

- you can download it here  https://oss.sonatype.org/ find web-debug-tag.

```

### FILTER PARAM : 1 required at least

```
    Be carefull with spring or sitemesh

- Add in your web.xml, min 1 filter.
   <context-param>
      <param-name>webdebug.excludes</param-name>
      <param-value>__spring.*,__sitemesh.*,org.apache.jasper.*,org.apache.catalina.*,org.eclipse.jetty.webapp.Context,org.eclipse.jetty.server.*,org.eclipse.jetty.servlet.*,org.eclipse.jetty.webapp.*</param-value>
   </context-param> 
  

``` 

### TOMCAT OR OTHER

```  

- Add -Ddebug.jsp=true in "APPS-OPTS" of your setenv.sh.

```

### IDE 

```

- Add -Ddebug.jsp=true in VM parameters of your Runner.

```

### USAGE
          
```
- Add in jsp file you want to debug : 
      <%@ taglib prefix="debug" uri="https://github.com/figarocms/web-debug-tag"%>
      <debug:debugModel/>  

- Launch your web application and open firebug to see an Object with :
  - request
  - session
  - application
  - page

```              
    
### LICENSE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
