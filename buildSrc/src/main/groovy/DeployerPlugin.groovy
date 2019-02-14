import com.laser.gradle.core.BasePlugin
import com.laser.gradle.core.util.TaskBuilder
import org.gradle.api.Project
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
        project.childProjects.findAll {
            // Exclude the app project if it exists.
            it.value.name != "app"
        }.each {
            // Apply the plugin used to deploy the artifacts on Maven.
            applyUploadConfiguration(it.value)
        }
    }

    void applyUploadConfiguration(Project project) {
        project.with {
            apply plugin: 'signing'
            apply plugin: 'org.jetbrains.dokka-android'
            apply plugin: 'com.github.dcendents.android-maven'

            group = GROUP
            version = VERSION_NAME

            // Add the Dokka extension.
            dokka {
                outputFormat = "html"
                skipDeprecated = true
                skipEmptyPackages = true
            }

            afterEvaluate {
                install {
                    repositories.mavenInstaller {
                        pom.project {
                            //noinspection GroovyAssignabilityCheck
                            name = POM_NAME
                            //noinspection GroovyAssignabilityCheck
                            packaging = POM_PACKAGING
                            //noinspection GroovyAssignabilityCheck
                            description = POM_DESCRIPTION

                            licenses {
                                license {
                                    name = POM_LICENCE_NAME
                                    url = POM_LICENCE_URL
                                    distribution = POM_LICENCE_DIST
                                }
                            }

                            developers {
                                developer {
                                    id = POM_DEVELOPER_ID
                                    name = POM_DEVELOPER_NAME
                                }
                                developer {
                                    id = POM_DEVELOPER2_ID
                                    name = POM_DEVELOPER2_NAME
                                }
                            }
                        }
                    }
                }

                signing {
                    required {
                        !VERSION_NAME.contains("SNAPSHOT") && gradle.taskGraph.hasTask("uploadArchives")
                    }
                    //noinspection GroovyAssignabilityCheck
                    sign configurations.archives
                }

                def sourcesJar = new TaskBuilder(project)
                        .name("sourcesJar")
                        .type(Jar)
                        .action {
                    classifier = 'sources'
                    from android.sourceSets.main.java.srcDirs
                }
                .build()

                def androidDocsTask = new TaskBuilder(project)
                        .name("androidDocsJar")
                        .type(Jar)
                        .action {
                    classifier = 'javadoc'
                    from dokka.outputDirectory
                }
                .build()
                androidDocsTask.dependsOn dokka


                artifacts {
                    archives androidDocsTask
                    archives sourcesJar
                }
            }
        }
    }
}