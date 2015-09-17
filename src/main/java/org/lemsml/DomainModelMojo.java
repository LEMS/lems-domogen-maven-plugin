package org.lemsml;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Says "Hi" to the user.
 *
 */
@Mojo(name = "sayhi")
public class DomainModelMojo extends AbstractMojo {
	@Parameter(property = "sayhi.greeting", defaultValue = "Hello World!")
	private String greeting;

	@Parameter(property = "sayhi.outputDir", defaultValue = "${project.build.directory}/generated-sources/domo")
	private File outputDir;

	@Parameter(property = "sayhi.project", defaultValue = "${project}")
	private MavenProject project;


	public void execute() throws MojoExecutionException {
		getLog().info(greeting);
		getLog().info(outputDir.toString());

		File foo = new File(outputDir, "foo.java");
		foo.getParentFile().mkdirs();
		try {
			foo.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		project.addCompileSourceRoot(outputDir.getAbsolutePath());

	}
}