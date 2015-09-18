package org.lemsml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generateDoMoClasses")
public class DomainModelMojo extends AbstractMojo {
	@Parameter(property = "generateDoMoClasses.mlName", defaultValue = "org.testml.model")
	private String mlName;

	@Parameter(property = "generateDoMoClasses.outputDir", defaultValue = "${project.build.directory}/generated-sources/LEMS")
	private File outputDir;

	@Parameter(property = "generateDoMoClasses.project", defaultValue = "${project}")
	private MavenProject project;

	public void execute() throws MojoExecutionException {
		getLog().info(MessageFormat.format("Generating domain model classes for {0} into {1}",
				mlName, outputDir.toString()));

		File foo = new File(outputDir, mlName.replace(".", "/") + "/Blah.java");
		createFile(foo);

		project.addCompileSourceRoot(foo.getParentFile().getAbsolutePath());

	}

	public void createFile(File foo) {
		foo.getParentFile().mkdirs();
		try {
			foo.createNewFile();
			getLog().info("\t" + foo.getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			PrintWriter out = new PrintWriter(foo);
			out.println("package " + mlName + ";\n\npublic class Blah{}");
			out.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}