
Kirchnerei &copy; 2014

# Kirchnerei Decrypt Plugin

Decrypt all properties whose values ​​are encrypted with mvn -ep "password"


## Usage

Edit the `pom.xml`:

	...
	<build>
		...
		<plugins>
			...
			<plugin>
				<groupId>kirchnerei</groupId>
				<artifactId>kirchnerei-encrypt-plugin</artifactId>
				<version>1.0</version>
				<executions>
					<execution>
						<id>decode-passwords</id>
						<phase>initialize</phase>
						<goals>
							<goal>process</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			...
		</plugins>
		...
     </build>
    ...

## Repository

Edit the `pom.xml`:

	...
	<repositories>
		<repository>
			<id>kirchnerei-repository</id>
            <name>Public Repository on GitHub</name>
            <url>https://raw.github.com/mulder3/kirchnerei-maven-repository/master</url>
            <layout>default</layout>
        </repository>
	</repositories>
	...

# Note

I was inspired by [Manuel Martins][mcmartins]. For my projects I have made ​​small adjustments.

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




[mcmartins]: https://bitbucket.org/mcmartins/maven-plugins/wiki/DecodePasswordPlugin
