# Generation of domain classes for LEMS-based languages

This `maven` plugin allows generating `Java` bindings for domain-specific languages built using the [LEMS](https://github.com/LEMS/LEMS) metalanguage. Such langugages are defined via collections of LEMS `ComponentType`s.

This plugin builds Java classes automatically once the project is built with e.g. `maven install`.

## Installation

Add the snippet below to `pom.xml`, substituting `<mlName>` and `<componentTypeDefs>` with relevant values for your LEMS language.

```xml
<plugin>
	<dependencies>
		<dependency>
			<groupId>org.lemsml.lems-domogen-maven-plugin</groupId>
			<artifactId>org.lemsml.lems-domogen-maven-plugin</artifactId>
			<version>0.4</version>
		</dependency>
	</dependencies>
	<groupId>org.lemsml.lems-domogen-maven-plugin</groupId>
	<artifactId>org.lemsml.lems-domogen-maven-plugin</artifactId>
	<version>0.4</version>
	<configuration>
		<mlName>myLang</mlName>
		<componentTypeDefs>${project.basedir}/src/main/resources/myLang/MyLangLemsCompTypes.xml</componentTypeDefs>
	</configuration>
	<executions>
		<execution>
			<phase>generate-sources</phase>
			<goals>
				<goal>generateDoMoClasses</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

[![Build Status](https://travis-ci.org/LEMS/lems-domogen-maven-plugin.svg?branch=master)](https://travis-ci.org/LEMS/lems-domogen-maven-plugin)

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1345750.svg)](https://doi.org/10.5281/zenodo.1345750)

