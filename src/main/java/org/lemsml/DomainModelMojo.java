package org.lemsml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.lemsml.model.compiler.LEMSCompilerFrontend;
import org.lemsml.model.extended.ComponentType;
import org.lemsml.model.extended.Lems;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.StringRenderer;

@Mojo(name = "generateDoMoClasses")
public class DomainModelMojo extends AbstractMojo {
	@Parameter(property = "generateDoMoClasses.mlName", defaultValue = "org.MyML.model")
	private String mlName;

	@Parameter(property = "generateDoMoClasses.outputDir", defaultValue = "${project.build.directory}/generated-sources/LEMS")
	private File outputDir;

	@Parameter(property = "generateDoMoClasses.componentTypeDefs", defaultValue = "${project.basedir}/src/main/resources/lems/MyML.xml")
	private File componentTypeDefs;

	@Parameter(property = "generateDoMoClasses.project", defaultValue = "${project}")
	private MavenProject project;

	private Lems domainDefs;
	private File baseDir;
	private STGroup typeSTG;
	private String pkgName;

	public void execute() throws MojoExecutionException {
		getLog().info(
				MessageFormat.format(
						"Generating domain model classes for {0},\n"
								+ "\t defined in {1},\n" + "\t onto {2}",
						mlName, componentTypeDefs, outputDir));

		pkgName = "org." + mlName + ".model";
		baseDir = new File(outputDir, pkgName.replace(".", "/"));
		try {
			domainDefs = new LEMSCompilerFrontend(componentTypeDefs)
					.generateLEMSDocument();
		} catch (Throwable e) {
			throw new MojoExecutionException(e.toString());
		}
		typeSTG = parseDomainTypeSTG();

		try {
			baseDir.mkdirs();
			createBaseDefinitions();
			generateDomainClasses();
			generateObjectFactory();
		} catch (Throwable e) {
			throw new MojoExecutionException(e.toString());
		}

		project.addCompileSourceRoot(baseDir.getAbsolutePath());

	}

	private void createBaseDefinitions() throws IOException {
		String fName = mlName + ".java";
		getLog().info("\t" + fName);

		ST merged = mergeRootElement();
		dumpTemplateToFile(fName, merged);

	}

	private void generateObjectFactory() throws IOException {
		String fName = "ObjectFactory.java";

		URL stURL = getClass().getResource("/templates/obj_factory.stg");
		STGroup group = new STGroupFile(stURL, "UTF-8", '<', '>');

		ST template = group.getInstanceOf("obj_factory");
		template.add("lems", domainDefs);
		template.add("package", pkgName);
		template.add("ml_name", mlName);
		dumpTemplateToFile(fName, template);

	}

	private void generateDomainClasses() throws IOException {
		for (ComponentType ct : domainDefs.getComponentTypes()) {
			String fName = ct.getName().replace(".", "_") + ".java";
			getLog().info("\t" + fName);

			ST merged = mergeCompTypeTemplate(ct);
			dumpTemplateToFile(fName, merged);
		}
	}

	public ST mergeCompTypeTemplate(ComponentType ct) {
		ST template = typeSTG.getInstanceOf("class_file");

		template.add("type", ct);
		template.add("ml_name", mlName);
		template.add("package", pkgName);

		return template;
	}

	public ST mergeRootElement() {
		URL stURL = getClass().getResource("/templates/root_element.stg");
		STGroup group = new STGroupFile(stURL, "UTF-8", '<', '>');
		group.registerRenderer(String.class, new StringRenderer());

		ST template = group.getInstanceOf("root_element");
		template.add("lems", domainDefs);
		template.add("ml_name", mlName);
		template.add("package", pkgName);

		return template;
	}

	private STGroup parseDomainTypeSTG() {
		URL stURL = getClass().getResource("/templates/domain_type.stg");
		STGroup group = new STGroupFile(stURL, "UTF-8", '<', '>');
		group.registerRenderer(String.class, new StringRenderer());
		return group;
	}

	public void dumpTemplateToFile(String fName, ST merged) throws IOException {
		FileWriter out = new FileWriter(new File(baseDir, fName));
		out.append(merged.render());
		out.flush();
		out.close();
	}

}