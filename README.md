# devtest-maven-plugin  v0.3.0
Maven plugin for custom CA DevTest extensions

**Author:** Mike Gavaghan - **Email:** mike@gavaghan.org

## Description ##
This plugin defines useful goals for using Maven when working with CA Technologies DevTest artifacts.

It has currently been validated against the following DevTest versions:

- DevTest 10.1
- DevTest 10.2
`

## Goal: `create-master-jar` ##
One of the challenges building a custom extension using the DevTest SDK is determining all of the appropriate dependencies to including in your build path.  Not only must you include the DevTest-specific jars, you must also reference the proper version of various open source libraries that DevTest relies upon.

This Maven goal allows you to point to a DevTest installation folder and collapse all of the dozens of jars in the `lib` folder into a single, master DevTest jar that you can use during compilation.  You would only need to add this dependency to the `pom.xml` of your extension project:

	<dependency>
		<groupId>com.ca</groupId>
		<artifactId>devtest</artifactId>
		<version>10.2</version>  <!-- or, whatever DevTest version you built for. -->
		<scope>compile</scope>
	</dependency>

You should only need to execute the installation of the DevTest master jar once for each version of DevTest.  In other words, don't include the master jar builder as part of your project - keep it as its own project to install into your local repository.

This should do the trick:


	<groupId>com.ca</groupId>
	<artifactId>devtest</artifactId>
   <!--
      TODO Replace with the version of DevTest found in 'devtest-home'.
      This will determine the verion of the artifact stored in the local
      repository
   -->         
	<version>10.2</version>
	<packaging>jar</packaging>

    <build>
      <plugins>
         <plugin>
            <groupId>org.gavaghan</groupId>
            <artifactId>devtest-maven-plugin</artifactId>
            <version>0.3.0</version>
            <configuration>
              <!-- 
                 TODO Replace with your fully qualified path to DevTest installation
                      folder
              -->
              <devtest-home>C:\Program Files\CA\DevTest\10.2</devtest-home>
              <!--
                 TODO Replace with the version of DevTest found in 'devtest-home'.
                 This will determine the verion of the artifact stored in the local
                 repository
              -->         
              <devtest-version>10.2</devtest-version>
            </configuration>
            <executions>
               <execution>
                  <goals>
                     <goal>create-master-jar</goal>
                  </goals>
               </execution>
            </executions>
         </plugin>
      </plugins>
    </build>

A complete `pom.xml` can also be found in the `devtest-jar-builder` project.

Execute this command to install the DevTest master jar artifact in your local repository (this will take several minutes to complete.

    mvn clean install


## Goal: `copy-to-hotDeploy` ##
This goal takes a successfully compiled target `jar` and copies it to the `hotDeploy` folder of a DevTest installation on the local machine during the `install` phase.

Add this section to your `pom.xml`:

    <build>
       <plugins>
          <plugin>
             <groupId>org.gavaghan</groupId>
             <artifactId>devtest-maven-plugin</artifactId>
             <version>0.3.0</version>
             <configuration>
                <!-- 
                   TODO Replace with your fully qualified path to DevTest installation folder
                   (not the hotDeploy folder) 
                -->
                <devtest-home>C:\Program Files\CA\DevTest\10.2<devtest-home>
             </configuration>
             <executions>
                <execution>
                   <goals>
                      <goal>copy-to-hotDeploy</goal>
                   </goals>
                </execution>
             </executions>
          </plugin>
       </plugins>
    </build>
    
Execute this command to build your custom extension and copy it to `[DevTestHome]/hotDeploy`:

    mvn clean install

