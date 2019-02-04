import com.laser.gradle.core.BasePlugin
import com.laser.gradle.core.util.TaskBuilder
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.jvm.tasks.Jar

/**
 * Plugin used to deploy all modules on Maven.
 * There are two possible tasks:
 * - publishModules (in the root project): it will deploy all modules
 * - uploadArchives (in the sub projects): it will deploy only the current module
 */
class DeployerPlugin extends BasePlugin {

    @Override
    void execute(Project project) {
        def subModules = project.childProjects.findAll {
            // Exclude the app project if it exists.
            it.value.name != "app"
        }.each {
            // Apply the plugin used to deploy the artifacts on Maven.
            applyUploadConfiguration(it.value)
        }

        // Wait the evaluation of the children to avoid an incomplete task graph.
        project.evaluationDependsOnChildren()

        def publishTask = new TaskBuilder(project)
                .name("deployModules")
                .description("Deploys all the modules on maven with a common version.")
                .build()

        subModules.each {
            def currentTask = it.value.tasks.findByName("uploadArchives")
            // Deploy each module after the publish task.
            publishTask.finalizedBy currentTask
        }
    }

    void applyUploadConfiguration(Project project) {
        project.with {
            apply plugin: 'maven'
            apply plugin: 'signing'
            apply plugin: 'org.jetbrains.dokka-android'

            // Add the Dokka extension.
            dokka {
                outputFormat = "html"
                skipDeprecated = true
                skipEmptyPackages = true
            }

            afterEvaluate {
                uploadArchives.repositories.mavenDeployer {
                    beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }

                    pom.groupId = GROUP
                    pom.artifactId = POM_ARTIFACT_ID
                    pom.version = VERSION_NAME

                    pom.whenConfigured {
                        it.dependencies.findAll { dependency ->
                            // Find all local dependencies.
                            dependency.groupId == rootProject.name
                        }.each { dependency ->
                            def props = rootProject.allprojects.find {
                                it.name == dependency.artifactId
                            }.getProperties()
                            // Get the original artifact id of the local dependency.
                            def artifactId = props.get("POM_ARTIFACT_ID")
                            // Change the properties of the dependency to match the maven properties.
                            dependency.setGroupId(GROUP)
                            dependency.setArtifactId(artifactId)
                            dependency.setVersion(VERSION_NAME)
                        }
                    }

                    def repoUrl = INTERNAL_RELEASE_MODE.toBoolean() ? RELEASE_REPOSITORY_URL : EXTERNAL_REPOSITORY_URL

                    repository(url: repoUrl) {
                        authentication(userName: NEXUS_USERNAME, password: NEXUS_PASSWORD)
                    }

                    pom.project {
                        name POM_NAME
                        packaging POM_PACKAGING
                        description POM_DESCRIPTION
                        url POM_URL

                        scm {
                            url POM_SCM_URL
                            connection POM_SCM_CONNECTION
                            developerConnection POM_SCM_DEV_CONNECTION
                        }

                        licenses {
                            license {
                                name POM_LICENCE_NAME
                                url POM_LICENCE_URL
                                distribution POM_LICENCE_DIST
                            }
                        }

                        developers {
                            developer {
                                id POM_DEVELOPER_ID
                                name POM_DEVELOPER_NAME
                            }
                        }
                    }
                }

                signing {
                    required { gradle.taskGraph.hasTask("uploadArchives") }
                    sign configurations.archives
                }

                def androidDocsTask = new TaskBuilder(project)
                        .name("androidDocsJar")
                        .type(Jar)
                        .action {
                    classifier = 'javadoc'
                    from dokka.outputDirectory
                }
                .build()

                androidDocsTask.dependsOn dokka

                def androidSourcesTask = new TaskBuilder(project)
                        .name("androidSourcesJar")
                        .type(Jar)
                        .action {
                    classifier = 'sources'
                    from android.sourceSets.main.java.srcDirs
                }
                .build()

                artifacts {
                    if (INTERNAL_RELEASE_MODE.toBoolean())
                        archives androidSourcesTask
                    archives androidDocsTask
                }
            }
        }
    }
}