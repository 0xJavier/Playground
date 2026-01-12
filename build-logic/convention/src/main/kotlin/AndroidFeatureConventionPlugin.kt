import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("myapp.android.library")
                apply("myapp.android.library.compose")
                apply("myapp.android.hilt")
            }

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:common"))

                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
            }
        }
    }
}
