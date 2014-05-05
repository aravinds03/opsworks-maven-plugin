opsworks-maven-plugin
=====================

Maven plugin for deploying to AWS Opsworks

Overview
========
This plugin was created to address OpsWorks' lack of versioning for custom applications. The plugin currently has only enough functionality to meet our needs, but it could be extended to encompass the full OpsWorks feature set.

Example Usage
=============
Here is an example maven configuration which updates an OpsWorks stack's custom JSON. That JSON can then be used by a custom chef script to download the correct version of the software. To execute this, you'd run: mvn clean deploy -Dstackname=*your stack name*


            <plugin>
                <groupId>com.n3twork</groupId>
                <artifactId>opsworks-maven-plugin</artifactId>
                <version>0.0.2</version>
                <configuration>
                    <stackName>${stackname}</stackName>
                    <appName>your-app-name</appName>
                    <customJsonOverride>
                        { "your-app-name": {
                            "version": "${project.version}"
                          }
                        }
                    </customJsonOverride>
                    <command>deploy</command>
                </configuration>
                <executions>
                    <execution>
                        <phase>deploy</phase>
                        <goals>
                            <goal>update-stack</goal>
                            <goal>deploy-app</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

