
# Kirchnerei Buildnumber Plugin

Generates a build number in the same way as ant.

# Usage

**pom.xml**

	<build>
        <plugins>
            <plugin>
                <groupId>kirchnerei</groupId>
                <artifactId>buildnumber-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>create</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <buildFile>build.properties</buildFile>
                    <propertyName>buildnumber</propertyName>
                    <outputDirectory>${basedir}</outputDirectory>
                    <increment>true|false</increment>
                </configuration>
            </plugin>
        </plugins>
    </build>

## Configuration

+ buildFile: The name of the property file that store the build number
+ propertyName: The name of the property
+ increment (boolean): if the value is true, then it will increment the build number, otherwise
  it use without incremented.



# License


	Copyright 2014 Kirchnerei

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

