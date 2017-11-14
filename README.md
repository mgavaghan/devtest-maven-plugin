# devtest-maven-plugin  v0.2.0
Maven plugin for custom CA DevTest extensions

**Author:** Mike Gavaghan - **Email:** mike@gavaghan.org

## Description ##
This plugin defines a very simple goal: `copy-to-hotDeploy`

This goal takes a successfully compiled target `jar` and copies it to the `hotDeploy` folder of a DevTest installation on the local machine during the `install` phase.

## Repository ##
This artifact is stored in a private plugin repository.  Please add this to your `settings.xml` file:

    <pluginRepositories>
       <pluginRepository>
          <id>private-devtest-repo</id>
          <name>Private DevTest Repository</name>
          <releases>
             <enabled>true</enabled>
             <updatePolicy>never</updatePolicy>
             <checksumPolicy>fail</checksumPolicy>
          </releases>
          <snapshots>
             <enabled>false</enabled>
          </snapshots>
          <url>http://sombrita.com:8080/maven2</url>
          <layout>default</layout>
       </pluginRepository>
    </pluginRepositories>


## Usage ##
Add this section to your `pom.xml`:

    <build>
       <plugins>
          <plugin>
             <groupId>org.gavaghan</groupId>
             <artifactId>devtest-maven-plugin</artifactId>
             <version>0.2.0</version>
             <configuration>
                <devtest-home>[fully qualified path to DevTest installation folder (not the hotDeploy folder)]
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

